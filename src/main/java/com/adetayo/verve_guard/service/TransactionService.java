package com.adetayo.verve_guard.service;


import com.adetayo.verve_guard.enums.FlaggedResolution;
import com.adetayo.verve_guard.enums.FraudSignal;
import com.adetayo.verve_guard.enums.TransactionStatus;
import com.adetayo.verve_guard.dto.request.TransactionRequest;
import com.adetayo.verve_guard.dto.response.TransactionResponse;
import com.adetayo.verve_guard.entity.BlacklistedCard;
import com.adetayo.verve_guard.entity.BlacklistedMerchant;
import com.adetayo.verve_guard.entity.CardSpendingProfile;
import com.adetayo.verve_guard.entity.FlaggedAttempt;
import com.adetayo.verve_guard.entity.MerchantCategory;
import com.adetayo.verve_guard.exception.FraudDetectionException;
import com.adetayo.verve_guard.entity.TransactionLog;
import com.adetayo.verve_guard.repository.BlacklistedCardRepository;
import com.adetayo.verve_guard.repository.BlacklistedMerchantRepository;
import com.adetayo.verve_guard.repository.CardSpendingProfileRepository;
import com.adetayo.verve_guard.repository.FlaggedAttemptRepository;
import com.adetayo.verve_guard.repository.MerchantCategoryRepository;
import com.adetayo.verve_guard.repository.TransactionLogRepository;
import com.adetayo.verve_guard.response.ApiResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final RateLimiterService rateLimiterService;
    private final BlacklistedMerchantRepository blacklistedMerchantRepository;
    private final BlacklistedCardRepository blacklistedCardRepository;
    private final CardSpendingProfileRepository cardSpendingProfileRepository;
    private final FlaggedAttemptRepository flaggedAttemptRepository;
    private final TransactionLogRepository transactionLogRepository;
    private final MerchantCategoryRepository merchantCategoryRepository;


    @Value("${fraud.amount.anomaly.multiplier}")
    private double amountAnomalyMultiplier;

    @Value("${fraud.repeat.offender.max-flags}")
    private int repeatOffenderMaxFlags;

    @Value("${fraud.repeat.offender.window-days}")
    private int repeatOffenderWindowDays;

    @Transactional
    public ApiResponse<TransactionResponse> checkTransaction(TransactionRequest request) {
        // Hash card immediately; never persist or log raw number
        String cardHash = hashCardNumber(request.getCardNumber());
        String cardLastFour = extractLastFour(request.getCardNumber());
        BigDecimal amount = request.getAmount();
        String merchantId = request.getMerchantId();
        String ipAddress = request.getIpAddress();

        // 1. BLACKLISTED CARD
        if (blacklistedCardRepository.existsByCardNumberHash(cardHash)) {
            TransactionResponse response = basicResponse(TransactionStatus.BLOCKED, cardLastFour, amount, merchantId);
            logTransaction(cardHash, cardLastFour, merchantId, amount, ipAddress,
                    TransactionStatus.BLOCKED, FraudSignal.CARD_BLACKLISTED.getMessage());
            createFlaggedAttempt(cardHash, cardLastFour, merchantId, amount, ipAddress,
                    FraudSignal.CARD_BLACKLISTED, FlaggedResolution.PENDING_REVIEW);
            return ApiResponse.failure(FraudSignal.CARD_BLACKLISTED.getMessage(), response);
        }

        // 2. BLACKLISTED MERCHANT
        Optional<BlacklistedMerchant> blacklistedMerchantOpt = blacklistedMerchantRepository.findByMerchantId(merchantId);
        if (blacklistedMerchantOpt.isPresent()) {
            TransactionResponse response = basicResponse(TransactionStatus.BLOCKED, cardLastFour, amount, merchantId);
            logTransaction(cardHash, cardLastFour, merchantId, amount, ipAddress,
                    TransactionStatus.BLOCKED, FraudSignal.BLACKLISTED_MERCHANT.getMessage());
            createFlaggedAttempt(cardHash, cardLastFour, merchantId, amount, ipAddress,
                    FraudSignal.BLACKLISTED_MERCHANT, FlaggedResolution.PENDING_REVIEW);
            return ApiResponse.failure(FraudSignal.BLACKLISTED_MERCHANT.getMessage(), response);
        }

        // 3. IP RATE LIMITING
        boolean ipBlocked = rateLimiterService.isIpBlocked(ipAddress, cardHash);
        if (ipBlocked) {
            TransactionResponse response = basicResponse(TransactionStatus.BLOCKED, cardLastFour, amount, merchantId);
            logTransaction(cardHash, cardLastFour, merchantId, amount, ipAddress,
                    TransactionStatus.BLOCKED, FraudSignal.RATE_LIMIT_EXCEEDED.getMessage());
            createFlaggedAttempt(cardHash, cardLastFour, merchantId, amount, ipAddress,
                    FraudSignal.RATE_LIMIT_EXCEEDED, FlaggedResolution.PENDING_REVIEW);
            return ApiResponse.failure(FraudSignal.RATE_LIMIT_EXCEEDED.getMessage(), response);
        }

        // 4. CARD VELOCITY
        boolean cardVelocityExceeded = rateLimiterService.isCardVelocityExceeded(cardHash);
        if (cardVelocityExceeded) {
            BlacklistedCard blacklistedCard = BlacklistedCard.builder()
                    .cardNumberHash(cardHash)
                    .cardLastFour(cardLastFour)
                    .reason("VELOCITY_EXCEEDED")
                    .blacklistedAt(LocalDateTime.now())
                    .build();
            blacklistedCardRepository.save(blacklistedCard);

            TransactionResponse response = basicResponse(TransactionStatus.BLOCKED, cardLastFour, amount, merchantId);
            logTransaction(cardHash, cardLastFour, merchantId, amount, ipAddress,
                    TransactionStatus.BLOCKED, FraudSignal.CARD_VELOCITY_EXCEEDED.getMessage());
            createFlaggedAttempt(cardHash, cardLastFour, merchantId, amount, ipAddress,
                    FraudSignal.CARD_VELOCITY_EXCEEDED, FlaggedResolution.PENDING_REVIEW);
            return ApiResponse.failure(FraudSignal.CARD_VELOCITY_EXCEEDED.getMessage(), response);
        }

        // 5. MERCHANT CATEGORY LIMIT
        MerchantCategory category = null;
        if (blacklistedMerchantOpt.isPresent() ) {
            String categoryName = blacklistedMerchantOpt.get().getMerchantCategory();
            category = merchantCategoryRepository.findByCategoryName(categoryName).orElse(null);
        }
        if (category != null && amount.compareTo(category.getMaxAmount()) > 0) {
            // Block due to category limit
            TransactionResponse response = TransactionResponse.builder()
                    .status(TransactionStatus.BLOCKED)
                    .cardLastFour(cardLastFour)
                    .amount(amount)
                    .merchantId(merchantId)
                    .merchantCategory(category.getCategoryName())
                    .categoryLimit(category.getMaxAmount())
                    .upgradeMessage("Upgrade to STANDARD or PREMIUM to process higher amounts.")
                    .build();

            logTransaction(cardHash, cardLastFour, merchantId, amount, ipAddress,
                    TransactionStatus.BLOCKED, FraudSignal.MERCHANT_CATEGORY_LIMIT_EXCEEDED.getMessage());
            createFlaggedAttempt(cardHash, cardLastFour, merchantId, amount, ipAddress,
                    FraudSignal.MERCHANT_CATEGORY_LIMIT_EXCEEDED, FlaggedResolution.PENDING_REVIEW);

            return ApiResponse.failure(FraudSignal.MERCHANT_CATEGORY_LIMIT_EXCEEDED.getMessage(), response);
        }

        // 6. BEHAVIOURAL AMOUNT ANOMALY
        Optional<CardSpendingProfile> profileOpt = cardSpendingProfileRepository.findByCardNumberHash(cardHash);

        TransactionStatus interimStatus;
        FraudSignal anomalySignal =null;
        FlaggedResolution anomalyResolution =null;

        if (profileOpt.isEmpty()) {
            // No profile exists: create profile and APPROVE
            CardSpendingProfile profile = CardSpendingProfile.builder()
                    .cardNumberHash(cardHash)
                    .avgTransactionAmount(amount)
                    .maxSeenAmount(amount)
                    .transactionCount(1)
                    .lastUpdated(LocalDateTime.now())
                    .build();
            cardSpendingProfileRepository.save(profile);

            interimStatus = TransactionStatus.APPROVED;
        } else {
            CardSpendingProfile profile = profileOpt.get();
            BigDecimal avg = profile.getAvgTransactionAmount();
            if (avg == null || avg.compareTo(BigDecimal.ZERO) <= 0) {
                profile.setAvgTransactionAmount(amount);
                profile.setMaxSeenAmount(amount);
                profile.setTransactionCount(1);
                profile.setLastUpdated(LocalDateTime.now());
                cardSpendingProfileRepository.save(profile);
                interimStatus = TransactionStatus.APPROVED;
            } else {
                BigDecimal threshold = avg.multiply(BigDecimal.valueOf(amountAnomalyMultiplier));
                BigDecimal extremeThreshold = avg.multiply(BigDecimal.TEN);

                if (amount.compareTo(threshold) <= 0) {
                    // APPROVE, update profile
                    int currentCount = profile.getTransactionCount() == null ? 0 : profile.getTransactionCount();
                    int newCount = currentCount + 1;

                    BigDecimal totalBefore = avg.multiply(BigDecimal.valueOf(currentCount));
                    BigDecimal newAvg = totalBefore.add(amount)
                            .divide(BigDecimal.valueOf(newCount), 2, RoundingMode.HALF_UP);

                    profile.setAvgTransactionAmount(newAvg);
                    if (profile.getMaxSeenAmount() == null || amount.compareTo(profile.getMaxSeenAmount()) > 0) {
                        profile.setMaxSeenAmount(amount);
                    }
                    profile.setTransactionCount(newCount);
                    profile.setLastUpdated(LocalDateTime.now());
                    cardSpendingProfileRepository.save(profile);

                    interimStatus = TransactionStatus.APPROVED;
                } else if (amount.compareTo(extremeThreshold) <= 0) {
                    interimStatus = TransactionStatus.FLAGGED;
                    anomalySignal = FraudSignal.AMOUNT_ANOMALY;
                    anomalyResolution = null; // resolution = null for moderate anomaly
                } else {
                    // BLOCKED (extreme anomaly) – do NOT update profile
                    interimStatus = TransactionStatus.BLOCKED;
                    anomalySignal = FraudSignal.AMOUNT_ANOMALY;
                    anomalyResolution = FlaggedResolution.PENDING_REVIEW;
                }
            }
        }

        // 7. REPEAT OFFENDER PATTERN
        if (interimStatus != TransactionStatus.BLOCKED) {
            LocalDateTime windowStart = LocalDateTime.now().minusDays(repeatOffenderWindowDays);
            long flagCount = flaggedAttemptRepository
                    .countByCardNumberHashAndCreatedAtAfter(cardHash, windowStart);

            if (flagCount >= repeatOffenderMaxFlags) {
                // Blacklist card as repeat offender
                BlacklistedCard repeatOffenderCard = BlacklistedCard.builder()
                        .cardNumberHash(cardHash)
                        .cardLastFour(cardLastFour)
                        .reason("REPEAT_OFFENDER")
                        .blacklistedAt(LocalDateTime.now())
                        .build();
                blacklistedCardRepository.save(repeatOffenderCard);

                TransactionResponse response = basicResponse(TransactionStatus.BLOCKED, cardLastFour, amount, merchantId);
                logTransaction(cardHash, cardLastFour, merchantId, amount, ipAddress,
                        TransactionStatus.BLOCKED, FraudSignal.REPEAT_OFFENDER.getMessage());
                createFlaggedAttempt(cardHash, cardLastFour, merchantId, amount, ipAddress,
                        FraudSignal.REPEAT_OFFENDER, FlaggedResolution.PENDING_REVIEW);
                return ApiResponse.failure(FraudSignal.REPEAT_OFFENDER.getMessage(), response);
            }
        }

        // Final response based on interim status
        if (interimStatus == TransactionStatus.APPROVED) {
            TransactionResponse response = basicResponse(TransactionStatus.APPROVED, cardLastFour, amount, merchantId);
            logTransaction(cardHash, cardLastFour, merchantId, amount, ipAddress,
                    TransactionStatus.APPROVED, null);
            return ApiResponse.success("Transaction approved successfully.", response);
        }

        if (interimStatus == TransactionStatus.FLAGGED) {
            // FLAGGED due to anomaly
            TransactionResponse response = basicResponse(TransactionStatus.FLAGGED, cardLastFour, amount, merchantId);
            logTransaction(cardHash, cardLastFour, merchantId, amount, ipAddress,
                    TransactionStatus.FLAGGED, anomalySignal.getMessage());
            createFlaggedAttempt(cardHash, cardLastFour, merchantId, amount, ipAddress,
                    anomalySignal, anomalyResolution);
            return ApiResponse.failure(anomalySignal.getMessage(), response);
        }

        // BLOCKED due to extreme anomaly
        TransactionResponse response = basicResponse(TransactionStatus.BLOCKED, cardLastFour, amount, merchantId);
        logTransaction(cardHash, cardLastFour, merchantId, amount, ipAddress,
                TransactionStatus.BLOCKED, anomalySignal.getMessage());
        createFlaggedAttempt(cardHash, cardLastFour, merchantId, amount, ipAddress,
                anomalySignal, anomalyResolution);
        return ApiResponse.failure(anomalySignal.getMessage(), response);
    }

    private TransactionResponse basicResponse(TransactionStatus status,
                                              String cardLastFour,
                                              BigDecimal amount,
                                              String merchantId) {
        return TransactionResponse.builder()
                .status(status)
                .cardLastFour(cardLastFour)
                .amount(amount)
                .merchantId(merchantId)
                .build();
    }

    private void logTransaction(String cardHash,
                                String cardLastFour,
                                String merchantId,
                                BigDecimal amount,
                                String ipAddress,
                                TransactionStatus status,
                                String fraudReason) {
        TransactionLog log = TransactionLog.builder()
                .cardNumberHash(cardHash)
                .cardLastFour(cardLastFour)
                .merchantId(merchantId)
                .amount(amount)
                .ipAddress(ipAddress)
                .status(status)
                .fraudReason(fraudReason)
                .build();
        transactionLogRepository.save(log);
    }

    private void createFlaggedAttempt(String cardHash,
                                      String cardLastFour,
                                      String merchantId,
                                      BigDecimal amount,
                                      String ipAddress,
                                      FraudSignal signal,
                                      FlaggedResolution resolution) {
        FlaggedAttempt attempt = FlaggedAttempt.builder()
                .cardNumberHash(cardHash)
                .cardLastFour(cardLastFour)
                .merchantId(merchantId)
                .amount(amount)
                .ipAddress(ipAddress)
                .fraudSignal(signal.name())
                .fraudMessage(signal.getMessage())
                .resolution(resolution)
                .createdAt(LocalDateTime.now())
                .build();
        flaggedAttemptRepository.save(attempt);
    }

    private String hashCardNumber(String cardNumber) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(cardNumber.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new FraudDetectionException("Unable to hash card number");
        }
    }

    private String extractLastFour(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        return cardNumber.substring(cardNumber.length() - 4);
    }
}
