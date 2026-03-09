package com.example.onlinetalaba.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String fullName;
    @NotBlank(message = "username is required")
    private String username;
    @Length(min = 7, max = 14)
    private String phoneNumber;
    private String address;
    @Email
    @NotBlank(message = "email is required")
    private String email;
    @Length(min = 8, max = 14)
    private String password;
}
