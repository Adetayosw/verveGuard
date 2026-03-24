package com.adetayo.verve_guard.dto.response;


import com.adetayo.verve_guard.enums.MerchantCategoryEnum;
import com.adetayo.verve_guard.enums.TransactionStatus;
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
public class TransactionResponse {

    private TransactionStatus status;
    private String cardLastFour;
    private BigDecimal amount;
    private String merchantId;

    private MerchantCategoryEnum merchantCategory;
    private BigDecimal categoryLimit;
    private String upgradeMessage;
}
