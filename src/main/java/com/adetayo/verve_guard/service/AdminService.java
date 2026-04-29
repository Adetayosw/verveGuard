package com.adetayo.verve_guard.service;



import com.adetayo.verve_guard.aop.Loggable;
import com.adetayo.verve_guard.enums.FlaggedResolution;
import com.adetayo.verve_guard.dto.request.BlacklistMerchantRequest;
import com.adetayo.verve_guard.dto.response.BlacklistedCardResponse;
import com.adetayo.verve_guard.dto.response.BlacklistedMerchantResponse;
import com.adetayo.verve_guard.dto.response.FlaggedAttemptResponse;
import com.adetayo.verve_guard.entity.BlacklistedCard;
import com.adetayo.verve_guard.entity.BlacklistedMerchant;
import com.adetayo.verve_guard.entity.FlaggedAttempt;
import com.adetayo.verve_guard.exception.ResourceNotFoundException;
import com.adetayo.verve_guard.repository.BlacklistedCardRepository;
import com.adetayo.verve_guard.repository.BlacklistedMerchantRepository;
import com.adetayo.verve_guard.repository.FlaggedAttemptRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final FlaggedAttemptRepository flaggedAttemptRepository;
    private final BlacklistedMerchantRepository blacklistedMerchantRepository;
    private final BlacklistedCardRepository blacklistedCardRepository;


    @Loggable
    public Page<FlaggedAttemptResponse> getFlaggedAttempts(Pageable pageable) {
        return flaggedAttemptRepository.findAll(pageable)
                .map(this::toFlaggedAttemptResponse);
    }

    @Loggable
    public Page<FlaggedAttemptResponse> getPendingReviews(Pageable pageable) {
        return flaggedAttemptRepository.findByResolution(FlaggedResolution.PENDING_REVIEW, pageable)
                .map(this::toFlaggedAttemptResponse);
    }

    @Loggable
    public Page<BlacklistedMerchantResponse> getBlacklistedMerchants(Pageable pageable) {
        return blacklistedMerchantRepository.findAll(pageable)
                .map(this::toBlacklistedMerchantResponse);
    }

    @Loggable
    public Page<BlacklistedCardResponse> getBlacklistedCards(Pageable pageable) {
        return blacklistedCardRepository.findAll(pageable)
                .map(this::toBlacklistedCardResponse);
    }

    @Loggable
    @Transactional
    public void resolveAttempt(Long flaggedAttemptId) {
        FlaggedAttempt attempt = flaggedAttemptRepository.findById(flaggedAttemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Flagged attempt not found"));

        attempt.setResolution(FlaggedResolution.ADMIN_RESOLVED);
        flaggedAttemptRepository.save(attempt);
    }

    @Loggable
    @Transactional
    public void blacklistMerchant(BlacklistMerchantRequest request, String adminUsername) {
        BlacklistedMerchant merchant = BlacklistedMerchant.builder()
                .merchantId(request.getMerchantId())
                .merchantCategory(request.getMerchantCategory().name())
                .reason(request.getReason())
                .blacklistedBy(adminUsername)
                .blacklistedAt(LocalDateTime.now())
                .build();

        blacklistedMerchantRepository.save(merchant);
    }

    // Mapping helpers (could be moved to MapStruct mappers later)

    private FlaggedAttemptResponse toFlaggedAttemptResponse(FlaggedAttempt attempt) {
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

    private BlacklistedMerchantResponse toBlacklistedMerchantResponse(BlacklistedMerchant merchant) {
        return BlacklistedMerchantResponse.builder()
                .id(merchant.getId())
                .merchantId(merchant.getMerchantId())
                .merchantCategory(merchant.getMerchantCategory())
                .reason(merchant.getReason())
                .blacklistedBy(merchant.getBlacklistedBy())
                .blacklistedAt(merchant.getBlacklistedAt())
                .build();
    }

    private BlacklistedCardResponse toBlacklistedCardResponse(BlacklistedCard card) {
        return BlacklistedCardResponse.builder()
                .id(card.getId())
                .cardNumberHash(card.getCardNumberHash())
                .cardLastFour(card.getCardLastFour())
                .reason(card.getReason())
                .blacklistedAt(card.getBlacklistedAt())
                .build();
    }
}
