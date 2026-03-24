package com.adetayo.verve_guard.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistedCardResponse {

    private Long id;
    private String cardNumberHash;
    private String cardLastFour;
    private String reason;
    private LocalDateTime blacklistedAt;
}
