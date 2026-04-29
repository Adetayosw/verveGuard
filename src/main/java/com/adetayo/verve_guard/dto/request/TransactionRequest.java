package com.adetayo.verve_guard.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotBlank(message = "Card number is required")
    private String cardNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Merchant ID is required")
    private String merchantId;

    @Pattern(
            regexp = "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.|$)){4}$",
            message = "Invalid IPv4 address"
    )
    @NotBlank(message = "IP address is required")
    private String ipAddress;
}