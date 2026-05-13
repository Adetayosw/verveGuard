package com.adetayo.verve_guard.mapper.mapperImpl;

import com.adetayo.verve_guard.dto.response.*;
import com.adetayo.verve_guard.entity.*;
import com.adetayo.verve_guard.mapper.AdminMapper;
import org.springframework.stereotype.Component;

@Component
public class AdminMapperImpl implements AdminMapper {

    @Override
    public FlaggedAttemptResponse toFlaggedAttemptResponse(FlaggedAttempt attempt) {
        return FlaggedAttemptResponse.builder()
                .id(attempt.getId())
                .cardNumberHash(attempt.getCardNumberHash())
                .cardLastFour(attempt.getCardLastFour())
                .merchantId(attempt.getMerchantId())
                .amount(attempt.getAmount())
                .ipAddress(attempt.getIpAddress())
                .fraudSignal(attempt.getFraudSignal())
                .fraudMessage(attempt.getFraudMessage())
                .resolution(attempt.getResolution())
                .createdAt(attempt.getCreatedAt())
                .build();
    }

    @Override
    public BlacklistedMerchantResponse toBlacklistedMerchantResponse(BlacklistedMerchant merchant) {
        return BlacklistedMerchantResponse.builder()
                .id(merchant.getId())
                .merchantId(merchant.getMerchantId())
                .merchantCategory(merchant.getMerchantCategory())
                .reason(merchant.getReason())
                .blacklistedBy(merchant.getBlacklistedBy())
                .blacklistedAt(merchant.getBlacklistedAt())
                .build();
    }

    @Override
    public BlacklistedCardResponse toBlacklistedCardResponse(BlacklistedCard card) {
        return BlacklistedCardResponse.builder()
                .id(card.getId())
                .cardNumberHash(card.getCardNumberHash())
                .cardLastFour(card.getCardLastFour())
                .reason(card.getReason())
                .blacklistedAt(card.getBlacklistedAt())
                .build();
    }
}