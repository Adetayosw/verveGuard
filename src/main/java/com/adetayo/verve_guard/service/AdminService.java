package com.adetayo.verve_guard.service;



import com.adetayo.verve_guard.aop.Loggable;
import com.adetayo.verve_guard.enums.FlaggedResolution;
import com.adetayo.verve_guard.dto.request.BlacklistMerchantRequest;
import com.adetayo.verve_guard.dto.response.BlacklistedCardResponse;
import com.adetayo.verve_guard.dto.response.BlacklistedMerchantResponse;
import com.adetayo.verve_guard.dto.response.FlaggedAttemptResponse;
import com.adetayo.verve_guard.entity.BlacklistedMerchant;
import com.adetayo.verve_guard.entity.FlaggedAttempt;
import com.adetayo.verve_guard.exception.ResourceNotFoundException;
import com.adetayo.verve_guard.mapper.AdminMapper;
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
    private final AdminMapper adminMapper;

    @Loggable
    public Page<FlaggedAttemptResponse> getFlaggedAttempts(Pageable pageable) {
        return flaggedAttemptRepository.findAll(pageable)
                .map(adminMapper::toFlaggedAttemptResponse);
    }

    @Loggable
    public Page<FlaggedAttemptResponse> getPendingReviews(Pageable pageable) {
        return flaggedAttemptRepository.findByResolution(FlaggedResolution.PENDING_REVIEW, pageable)
                .map(adminMapper::toFlaggedAttemptResponse);
    }

    @Loggable
    public Page<BlacklistedMerchantResponse> getBlacklistedMerchants(Pageable pageable) {
        return blacklistedMerchantRepository.findAll(pageable)
                .map(adminMapper::toBlacklistedMerchantResponse);
    }

    @Loggable
    public Page<BlacklistedCardResponse> getBlacklistedCards(Pageable pageable) {
        return blacklistedCardRepository.findAll(pageable)
                .map(adminMapper::toBlacklistedCardResponse);
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
}
