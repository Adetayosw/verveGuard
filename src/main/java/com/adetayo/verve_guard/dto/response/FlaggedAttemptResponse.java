package com.adetayo.verve_guard.dto.response;


import com.adetayo.verve_guard.enums.FlaggedResolution;
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
public class FlaggedAttemptResponse {

    private Long id;
    private String cardNumberHash;
    private String cardLastFour;
    private String merchantId;
    private BigDecimal amount;
    private String ipAddress;
    private String fraudSignal;
    private String fraudMessage;
    private FlaggedResolution resolution;
    private LocalDateTime createdAt;
}
