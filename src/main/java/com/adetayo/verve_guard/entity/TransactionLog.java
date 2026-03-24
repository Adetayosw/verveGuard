package com.adetayo.verve_guard.entity;


import com.adetayo.verve_guard.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionLog {

    private Long id;
    private String cardNumberHash;
    private String cardLastFour;
    private String merchantId;
    private BigDecimal amount;
    private String ipAddress;
    private TransactionStatus status;
    private String fraudReason;
    private LocalDateTime createdAt;
}
