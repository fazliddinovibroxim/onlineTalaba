package com.example.onlinetalaba.dto.auth;

import com.example.onlinetalaba.enums.AuthRoleName;
import com.example.onlinetalaba.enums.UserGender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Data
@Getter
@Setter
@Builder
public class UserRegisterDto {
    @NotBlank(message = "username is required")
    @Length(min = 4, max = 30)
    private String username;

    @Email(message = "email format is invalid")
    @NotBlank(message = "email is required")
    private String email;

    @NotBlank(message = "password is required")
    @Length(min = 7, max = 30)
    private String password;

    @NotNull(message = "gender is required")
    private UserGender gender;

    @NotNull(message = "role is required")
    private AuthRoleName role;
}
