package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.dto.ResponseApi;
import com.example.onlinetalaba.dto.auth.GoogleLoginRequest;
import com.example.onlinetalaba.dto.auth.ResponseLoginDto;
import com.example.onlinetalaba.dto.auth.UserRegisterDto;
import com.example.onlinetalaba.service.AuthService;
import com.example.onlinetalaba.service.GoogleAuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;

    @PostMapping("/register")
    public ResponseEntity<ResponseApi> register(@RequestBody @Valid UserRegisterDto dto) {
        return ResponseEntity.ok(authService.register(dto));
    }

    @GetMapping("/check-valid-user")
    public ResponseEntity<String> checkValidUser (@RequestParam String usernameOrEmail) {
        return ResponseEntity.ok(authService.checkValidUser(usernameOrEmail));
    }

    @PostMapping("/activate")
    public ResponseEntity<ResponseApi> activate(@RequestParam String email,
                                                @RequestParam String code) {
        return ResponseEntity.ok(authService.activate(email, code));
    }

    @PostMapping("/resend-code")
    public ResponseEntity<ResponseApi> resendVerificationCode(@RequestParam String email) {
        ResponseApi response = authService.resendCode(email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseLoginDto> login(@RequestParam String password,
                                                  @RequestParam String username) {
        ResponseLoginDto login = authService.login(password, username);
        return ResponseEntity.ok(login);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ResponseApi> resetPassword(@RequestParam @Email String email) {
        ResponseApi responseApi = authService.resetPassword(email);
        return ResponseEntity.status(responseApi.getCode()).body(responseApi);
    }

    @PostMapping("/confirm-reset-password")
    public ResponseEntity<ResponseApi> resetPasswordActivation(
            @RequestParam String email,
            @RequestParam String code,
            @RequestParam String password
    ) {
        ResponseApi responseApi =
                authService.resetPasswordActivation(email, code, password);

        return ResponseEntity.status(responseApi.getCode()).body(responseApi);
    }

    @PostMapping("/login/oauth2/google")
    public ResponseEntity<ResponseLoginDto> loginGoogleOauth2(@RequestBody GoogleLoginRequest request) {
        ResponseLoginDto response = googleAuthService.loginGoogleOauth2(request.getIdToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/firebase/google")
    public ResponseEntity<ResponseLoginDto> loginGoogleFirebase(@RequestParam String idToken) {
        ResponseLoginDto response = googleAuthService.loginGoogleFirebase(idToken);
        return ResponseEntity.ok(response);
    }
}