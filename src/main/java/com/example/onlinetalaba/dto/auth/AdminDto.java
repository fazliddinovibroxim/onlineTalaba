package com.example.onlinetalaba.dto.auth;

import com.example.onlinetalaba.enums.AppRoleName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminDto {
    @NotNull
    private String fullName;
    @NotNull(message = "username is required")
    private String username;
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "Noto'g'ri email manzil"
    )
    private String email;
    @NotBlank(message = "Password required")
    private String password;
    @NotBlank(message = "Telefon raqam bo'sh bo'lmasin")
    @Pattern(
            regexp = "^\\+998(?:50|77|33|90|91|93|94|95|97|98|99)\\d{7}$",
            message = "Phone number must be like +998931234567"
    )
    private String phoneNumber;
    @NotNull
    private String address;
    private String passportNumber;
    private String pinfl;
    private AppRoleName roleType;
}