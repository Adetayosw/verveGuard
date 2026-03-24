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
public class BlacklistedMerchantResponse {

    private Long id;
    private String merchantId;
    private String merchantCategory;
    private String reason;
    private String blacklistedBy;
    private LocalDateTime blacklistedAt;
}
