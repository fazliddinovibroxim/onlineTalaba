package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.ResponseApi;
import com.example.onlinetalaba.dto.auth.RedisPendingUser;
import com.example.onlinetalaba.dto.auth.RedisResetPassword;
import com.example.onlinetalaba.dto.auth.ResponseLoginDto;
import com.example.onlinetalaba.dto.auth.UserRegisterDto;
import com.example.onlinetalaba.entity.LogProgress;
import com.example.onlinetalaba.entity.Role;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.enums.AppRoleName;
import com.example.onlinetalaba.enums.AuthRoleName;
import com.example.onlinetalaba.handler.NotFoundException;
import com.example.onlinetalaba.repository.LogProgressRepository;
import com.example.onlinetalaba.repository.RoleRepository;
import com.example.onlinetalaba.repository.UserRepository;
import com.example.onlinetalaba.security.JwtService;
import com.example.onlinetalaba.util.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtService jwtService;
    private final LogProgressRepository logProgressRepository;
    private final EmailService emailService;

    private static final long EXPIRE_MINUTES = 10;
    private static final long RESET_EXPIRE_MINUTES = 5;

    @Transactional
    public ResponseApi register(UserRegisterDto dto) {
        String email = normalizeEmail(dto.getEmail());

        if (userRepository.existsByEmail(email)) {
            return new ResponseApi(409, "Email already exists");
        }

        String code = generateCode();

        RedisPendingUser pendingUser = new RedisPendingUser(
                dto.getUsername(),
                email,
                passwordEncoder.encode(dto.getPassword()),
                code,
                dto.getGender(),
                dto.getRole()
        );

        redisTemplate.opsForValue().set(
                pendingKey(email),
                pendingUser,
                EXPIRE_MINUTES,
                TimeUnit.MINUTES
        );

        log.info("REDIS SAVE KEY: {}", pendingKey(email));

        boolean sent = emailService.sendVerificationCode(email, code);
        if (!sent) {
            redisTemplate.delete(pendingKey(email));
            return new ResponseApi(500, "Failed to send verification email");
        }

        return new ResponseApi(200, "Verification code sent to your email");
    }

    @Transactional
    public ResponseApi activate(String email, String code) {
        String normalizedEmail = normalizeEmail(email);

        RedisPendingUser pendingUser =
                (RedisPendingUser) redisTemplate.opsForValue().get(pendingKey(normalizedEmail));

        if (pendingUser == null) {
            return new ResponseApi(404, "No pending registration found");
        }

        if (!pendingUser.getEmailCode().equals(code)) {
            return new ResponseApi(400, "Invalid verification code");
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            redisTemplate.delete(pendingKey(normalizedEmail));
            return new ResponseApi(409, "User already activated");
        }

        User user = new User();
        user.setUsername(pendingUser.getUsername());
        user.setEmail(pendingUser.getEmail());
        user.setPassword(pendingUser.getPassword());
        user.setGender(pendingUser.getUserGender());
        user.setEnabled(true);
        user.setRoles(getRoleByName(pendingUser.getRole()));

        userRepository.save(user);
        redisTemplate.delete(pendingKey(normalizedEmail));

        return new ResponseApi(200, "Account successfully activated");
    }

    public ResponseLoginDto login(String password, String username) {
        try {
            User principal = (User) authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            ).getPrincipal();

            String token = jwtService.generateToken(principal.getUsername());

            logProgressRepository.save(
                    new LogProgress(principal.getUsername() + " login", "TIZIMGA KIRILDI", "Nothing")
            );

            return new ResponseLoginDto(
                    principal.getFullName(),
                    principal.getUsername(),
                    principal.getEmail(),
                    token,
                    principal.getRoles().getAppRoleName()
            );
        } catch (Exception e) {
            logProgressRepository.save(
                    new LogProgress(username + " failed login", "KIRISHDA XATOLIK", e.getMessage())
            );
            throw new BadCredentialsException("Email yoki parol noto'g'ri");
        }
    }

    public ResponseApi resendCode(String email) {
        String normalizedEmail = normalizeEmail(email);

        RedisPendingUser pendingUser =
                (RedisPendingUser) redisTemplate.opsForValue().get(pendingKey(normalizedEmail));

        if (pendingUser == null) {
            return new ResponseApi(404, "No pending registration found");
        }

        String newCode = generateCode();
        pendingUser.setEmailCode(newCode);

        redisTemplate.opsForValue().set(
                pendingKey(normalizedEmail),
                pendingUser,
                EXPIRE_MINUTES,
                TimeUnit.MINUTES
        );

        boolean sent = emailService.sendVerificationCode(normalizedEmail, newCode);
        if (!sent) {
            return new ResponseApi(500, "Failed to resend verification email");
        }

        return new ResponseApi(200, "New verification code sent");
    }

//    @Override
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        User user = userRepository.findByEmail(normalizeEmail(email));
//        if (user == null) {
//            throw new UsernameNotFoundException("User not found");
//        }
//        return user;
//    }

    public ResponseApi resetPassword(String email) {
        String normalizedEmail = normalizeEmail(email);

        User user = userRepository.findByEmail(normalizedEmail);
        if (user == null) {
            return new ResponseApi(404, "User not found");
        }

        String code = generateCode();

        RedisResetPassword resetData = new RedisResetPassword(normalizedEmail, code);

        redisTemplate.opsForValue().set(
                resetKey(normalizedEmail),
                resetData,
                RESET_EXPIRE_MINUTES,
                TimeUnit.MINUTES
        );

        boolean sent = emailService.sendResetPasswordCode(normalizedEmail, code);
        if (!sent) {
            redisTemplate.delete(resetKey(normalizedEmail));
            return new ResponseApi(500, "Failed to send reset email");
        }

        return new ResponseApi(200, "Reset code sent to your email");
    }

    public Role getRoleByName(AuthRoleName roleCheck) {

        AppRoleName roleName;

        if (roleCheck == AuthRoleName.STUDENT) {
            roleName = AppRoleName.STUDENT;
        }else if (roleCheck == AuthRoleName.TEACHER) {
            roleName = AppRoleName.TEACHER;
        }else{
            roleName = AppRoleName.USER;
        }
        return roleRepository.findByAppRoleName(roleName)
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleName));
    }

    @Transactional
    public ResponseApi resetPasswordActivation(String email, String code, String newPassword) {
        String normalizedEmail = normalizeEmail(email);

        RedisResetPassword resetData =
                (RedisResetPassword) redisTemplate.opsForValue().get(resetKey(normalizedEmail));

        if (resetData == null) {
            return new ResponseApi(400, "Reset session expired");
        }

        if (!resetData.getCode().equals(code)) {
            return new ResponseApi(400, "Invalid reset code");
        }

        User user = userRepository.findByEmail(normalizedEmail);
        if (user == null) {
            return new ResponseApi(404, "User not found");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redisTemplate.delete(resetKey(normalizedEmail));

        return new ResponseApi(200, "Password successfully changed");
    }

    private String pendingKey(String email) {
        return "pending:email:" + email;
    }

    private String resetKey(String email) {
        return "reset:email:" + email;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateCode() {
        return String.valueOf(new SecureRandom().nextInt(10000, 99999));
    }
}
