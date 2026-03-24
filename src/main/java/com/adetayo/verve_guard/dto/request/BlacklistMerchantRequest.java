package com.adetayo.verve_guard.dto.request;


import com.adetayo.verve_guard.enums.MerchantCategoryEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistMerchantRequest {

    @NotBlank(message = "Merchant ID is required")
    private String merchantId;

    @NotBlank(message = "Reason is required")
    private String reason;

    @NotBlank(message = "Merchant category is required")
    private MerchantCategoryEnum merchantCategory;
}