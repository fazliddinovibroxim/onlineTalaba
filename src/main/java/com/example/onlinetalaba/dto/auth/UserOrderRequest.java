package com.example.onlinetalaba.dto.auth;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserOrderRequest {

    @NotNull(message = "productName is required")
    private String productName;

    @NotNull(message = "orderDescription is required")
    private String orderDescription;

    @NotNull(message = "price is required")
    private BigDecimal price;

    private LocalDate deadline;

    @NotNull(message = "phoneNumber is required")
    @Pattern(
            regexp = "^\\+998(?:50|77|33|90|91|93|94|95|97|98|99)\\d{7}$",
            message = "Phone number must be like +998931234567"
    )
    private String phoneNumber;
}
