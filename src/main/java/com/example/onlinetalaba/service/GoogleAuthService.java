package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.auth.ResponseLoginDto;
import com.example.onlinetalaba.entity.LogProgress;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.enums.AppRoleName;
import com.example.onlinetalaba.enums.AuthProvider;
import com.example.onlinetalaba.handler.BadRequestException;
import com.example.onlinetalaba.handler.ErrorCodes;
import com.example.onlinetalaba.handler.ErrorMessageException;
import com.example.onlinetalaba.handler.NotFoundException;
import com.example.onlinetalaba.repository.LogProgressRepository;
import com.example.onlinetalaba.repository.RoleRepository;
import com.example.onlinetalaba.repository.UserRepository;
import com.example.onlinetalaba.security.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final FirebaseAuthService firebaseAuthService;
    private final GoogleIdTokenVerifier verifier;
    private final LogProgressRepository logProgressRepository;

    public ResponseLoginDto loginGoogleOauth2(String idToken) {

        GoogleIdToken token;
        try {
            token = verifier.verify(idToken);
            logProgressRepository.save(new LogProgress(idToken, "GOOGLE_AUTH", "TAKEN ToKeN"));
        } catch (Exception e) {
            throw new BadRequestException("Invalid Google token");
        }

        if (token == null) {
            throw new BadRequestException("Google token is invalid");
        }

        GoogleIdToken.Payload payload = token.getPayload();

        if (!payload.getEmailVerified()) {
            throw new BadRequestException("Google email not verified");
        }
        String email = payload.getEmail();
        String fullName = (String) payload.get("name");

        User user = userRepository.findByEmail(email);

        if (user == null) {
            user = createUser(email, fullName);
        }

        String jwt = jwtService.generateToken(user.getEmail());

        return new ResponseLoginDto(
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                jwt,
                user.getRoles().getAppRoleName()
        );
    }

    private User createUser(String email, String fullName) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(email);
        user.setFullName(fullName);
        user.setProvider(AuthProvider.GOOGLE);
        user.setEnabled(true);
        user.setRoles(
                roleRepository.findByAppRoleName(AppRoleName.STUDENT)
                        .orElseThrow(() -> new NotFoundException("ROLE_USER not found"))
        );
        return userRepository.save(user);
    }
    public ResponseLoginDto loginGoogleFirebase(String idToken) {
        try {
            log.info("Google Firebase login attempt with token: {}", idToken);

            String email = firebaseAuthService.emailIfValidOrNull(idToken);

            if (email == null) {
                log.warn("Invalid Firebase token");
                throw new ErrorMessageException("INVALID_FIREBASE_TOKEN", ErrorCodes.BadRequest);
            }

            User user = userRepository.findByEmailAndIsDeletedFalse(email)
                    .orElseThrow(() -> new ErrorMessageException("USER_NOT_FOUND", ErrorCodes.NotFound));

            String token = jwtService.generateToken(user.getUsername());
            log.info("JWT token generated for Firebase user: {}", email);

            return new ResponseLoginDto(
                    user.getFullName(),
                    user.getUsername(),
                    user.getEmail(),
                    token,
                    user.getRoles().getAppRoleName()
            );
        } catch (Exception e) {
            log.error("Google Firebase login failed: {}", e.getMessage(), e);
            throw e;
        }
    }
}
