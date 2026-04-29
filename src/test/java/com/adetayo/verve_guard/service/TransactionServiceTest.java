package com.adetayo.verve_guard.service;

import com.adetayo.verve_guard.dto.request.TransactionRequest;
import com.adetayo.verve_guard.dto.response.ApiResponse;
import com.adetayo.verve_guard.dto.response.TransactionResponse;
import com.adetayo.verve_guard.entity.*;
import com.adetayo.verve_guard.enums.FlaggedResolution;
import com.adetayo.verve_guard.enums.FraudSignal;
import com.adetayo.verve_guard.enums.TransactionStatus;
import com.adetayo.verve_guard.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private RateLimiterService rateLimiterService;
    @Mock
    private BlacklistedMerchantRepository blacklistedMerchantRepository;
    @Mock
    private BlacklistedCardRepository blacklistedCardRepository;
    @Mock
    private CardSpendingProfileRepository cardSpendingProfileRepository;
    @Mock
    private FlaggedAttemptRepository flaggedAttemptRepository;
    @Mock
    private TransactionLogRepository transactionLogRepository;
    @Mock
    private MerchantCategoryRepository merchantCategoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setupConfig() {
        // configure @Value fields
        ReflectionTestUtils.setField(transactionService, "amountAnomalyMultiplier", 3.0d);
        ReflectionTestUtils.setField(transactionService, "repeatOffenderMaxFlags", 3);
        ReflectionTestUtils.setField(transactionService, "repeatOffenderWindowDays", 7);
    }

    private TransactionRequest buildRequest(BigDecimal amount) {
        return TransactionRequest.builder()
                .cardNumber("5061234567890123")
                .amount(amount)
                .merchantId("M123")
                .ipAddress("127.0.0.1")
                .build();
    }

    @Test
    void checkTransaction_whenCardIsBlacklisted_shouldBlockAndCreateFlag() {
        TransactionRequest request = buildRequest(BigDecimal.valueOf(1000));

        when(blacklistedCardRepository.existsByCardNumberHash(anyString())).thenReturn(true);

        ApiResponse<TransactionResponse> response = transactionService.checkTransaction(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData().getStatus()).isEqualTo(TransactionStatus.BLOCKED);
        assertThat(response.getData().getCardLastFour()).isEqualTo("0123");

        verify(transactionLogRepository).save(any(TransactionLog.class));
        verify(flaggedAttemptRepository).save(any(FlaggedAttempt.class));

        verifyNoInteractions(blacklistedMerchantRepository,
                cardSpendingProfileRepository,
                merchantCategoryRepository,
                rateLimiterService);
    }

    @Test
    void checkTransaction_whenMerchantIsBlacklisted_shouldBlockAndCreateFlag() {
        TransactionRequest request = buildRequest(BigDecimal.valueOf(1000));

        when(blacklistedCardRepository.existsByCardNumberHash(anyString())).thenReturn(false);

        BlacklistedMerchant bm = BlacklistedMerchant.builder()
                .merchantId("M123")
                .merchantCategory("SUPERMARKET")
                .build();
        when(blacklistedMerchantRepository.findByMerchantId("M123"))
                .thenReturn(Optional.of(bm));

        ApiResponse<TransactionResponse> response = transactionService.checkTransaction(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData().getStatus()).isEqualTo(TransactionStatus.BLOCKED);
        assertThat(response.getData().getMerchantId()).isEqualTo("M123");

        verify(transactionLogRepository).save(any(TransactionLog.class));
        verify(flaggedAttemptRepository).save(any(FlaggedAttempt.class));

        verifyNoInteractions(rateLimiterService,
                cardSpendingProfileRepository,
                merchantCategoryRepository);
    }

    @Test
    void checkTransaction_whenIpIsBlocked_shouldBlockAndCreateFlag() {
        TransactionRequest request = buildRequest(BigDecimal.valueOf(1000));

        when(blacklistedCardRepository.existsByCardNumberHash(anyString())).thenReturn(false);
        when(blacklistedMerchantRepository.findByMerchantId("M123")).thenReturn(Optional.empty());
        when(rateLimiterService.isIpBlocked(eq("127.0.0.1"), anyString())).thenReturn(true);

        ApiResponse<TransactionResponse> response = transactionService.checkTransaction(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData().getStatus()).isEqualTo(TransactionStatus.BLOCKED);

        verify(transactionLogRepository).save(any(TransactionLog.class));
        verify(flaggedAttemptRepository).save(any(FlaggedAttempt.class));
        verify(rateLimiterService, never()).isCardVelocityExceeded(anyString());
        verifyNoInteractions(cardSpendingProfileRepository, merchantCategoryRepository);
    }

    @Test
    void checkTransaction_whenCardVelocityExceeded_shouldBlacklistCardAndBlock() {
        TransactionRequest request = buildRequest(BigDecimal.valueOf(1000));

        when(blacklistedCardRepository.existsByCardNumberHash(anyString())).thenReturn(false);
        when(blacklistedMerchantRepository.findByMerchantId("M123")).thenReturn(Optional.empty());
        when(rateLimiterService.isIpBlocked(eq("127.0.0.1"), anyString())).thenReturn(false);
        when(rateLimiterService.isCardVelocityExceeded(anyString())).thenReturn(true);

        ApiResponse<TransactionResponse> response = transactionService.checkTransaction(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData().getStatus()).isEqualTo(TransactionStatus.BLOCKED);

        // card should be blacklisted
        ArgumentCaptor<BlacklistedCard> cardCaptor = ArgumentCaptor.forClass(BlacklistedCard.class);
        verify(blacklistedCardRepository).save(cardCaptor.capture());
        assertThat(cardCaptor.getValue().getReason()).isEqualTo("VELOCITY_EXCEEDED");

        verify(transactionLogRepository).save(any(TransactionLog.class));
        verify(flaggedAttemptRepository).save(any(FlaggedAttempt.class));
        verifyNoInteractions(cardSpendingProfileRepository, merchantCategoryRepository);
    }


    @Test
    void checkTransaction_whenNewCardProfile_shouldCreateProfileAndApprove() {
        TransactionRequest request = buildRequest(BigDecimal.valueOf(1000));

        when(blacklistedCardRepository.existsByCardNumberHash(anyString())).thenReturn(false);
        when(blacklistedMerchantRepository.findByMerchantId("M123")).thenReturn(Optional.empty());
        when(rateLimiterService.isIpBlocked(anyString(), anyString())).thenReturn(false);
        when(rateLimiterService.isCardVelocityExceeded(anyString())).thenReturn(false);
        when(cardSpendingProfileRepository.findByCardNumberHash(anyString()))
                .thenReturn(Optional.empty());
        when(flaggedAttemptRepository.countByCardNumberHashAndCreatedAtAfter(anyString(), any(LocalDateTime.class)))
                .thenReturn(0L);

        ApiResponse<TransactionResponse> response = transactionService.checkTransaction(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getStatus()).isEqualTo(TransactionStatus.APPROVED);

        // new profile saved
        verify(cardSpendingProfileRepository).save(any(CardSpendingProfile.class));
        verify(transactionLogRepository).save(any(TransactionLog.class));
        verify(flaggedAttemptRepository, never()).save(any());
    }

    @Test
    void checkTransaction_whenAmountWithinThreshold_shouldApproveAndUpdateProfile() {
        TransactionRequest request = buildRequest(BigDecimal.valueOf(900)); // <= avg*multiplier=1000*3

        when(blacklistedCardRepository.existsByCardNumberHash(anyString())).thenReturn(false);
        when(blacklistedMerchantRepository.findByMerchantId("M123")).thenReturn(Optional.empty());
        when(rateLimiterService.isIpBlocked(anyString(), anyString())).thenReturn(false);
        when(rateLimiterService.isCardVelocityExceeded(anyString())).thenReturn(false);

        CardSpendingProfile profile = CardSpendingProfile.builder()
                .cardNumberHash("somehash")
                .avgTransactionAmount(BigDecimal.valueOf(1000))
                .maxSeenAmount(BigDecimal.valueOf(1200))
                .transactionCount(5)
                .lastUpdated(LocalDateTime.now().minusDays(1))
                .build();

        when(cardSpendingProfileRepository.findByCardNumberHash(anyString()))
                .thenReturn(Optional.of(profile));
        when(flaggedAttemptRepository.countByCardNumberHashAndCreatedAtAfter(anyString(), any(LocalDateTime.class)))
                .thenReturn(0L);

        ApiResponse<TransactionResponse> response = transactionService.checkTransaction(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getStatus()).isEqualTo(TransactionStatus.APPROVED);

        verify(cardSpendingProfileRepository).save(any(CardSpendingProfile.class));
        verify(transactionLogRepository).save(any(TransactionLog.class));
        verify(flaggedAttemptRepository, never()).save(any());
    }

    @Test
    void checkTransaction_whenModerateAmountAnomaly_shouldFlagWithoutResolution() {
        // avg = 100, multiplier=3 -> threshold = 300
        // extremeThreshold = avg*10 = 1000
        // choose amount = 500 ( >300 and <=1000 ) => FLAGGED
        TransactionRequest request = buildRequest(BigDecimal.valueOf(500));

        when(blacklistedCardRepository.existsByCardNumberHash(anyString())).thenReturn(false);
        when(blacklistedMerchantRepository.findByMerchantId("M123")).thenReturn(Optional.empty());
        when(rateLimiterService.isIpBlocked(anyString(), anyString())).thenReturn(false);
        when(rateLimiterService.isCardVelocityExceeded(anyString())).thenReturn(false);

        CardSpendingProfile profile = CardSpendingProfile.builder()
                .cardNumberHash("somehash")
                .avgTransactionAmount(BigDecimal.valueOf(100))
                .maxSeenAmount(BigDecimal.valueOf(200))
                .transactionCount(5)
                .lastUpdated(LocalDateTime.now().minusDays(1))
                .build();

        when(cardSpendingProfileRepository.findByCardNumberHash(anyString()))
                .thenReturn(Optional.of(profile));
        when(flaggedAttemptRepository.countByCardNumberHashAndCreatedAtAfter(anyString(), any(LocalDateTime.class)))
                .thenReturn(0L);

        ApiResponse<TransactionResponse> response = transactionService.checkTransaction(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData().getStatus()).isEqualTo(TransactionStatus.FLAGGED);

        ArgumentCaptor<FlaggedAttempt> flaggedCaptor = ArgumentCaptor.forClass(FlaggedAttempt.class);
        verify(flaggedAttemptRepository).save(flaggedCaptor.capture());
        FlaggedAttempt savedAttempt = flaggedCaptor.getValue();
        assertThat(savedAttempt.getFraudSignal()).isEqualTo(FraudSignal.AMOUNT_ANOMALY.name());
        assertThat(savedAttempt.getResolution()).isNull(); // moderate anomaly => null resolution

        verify(transactionLogRepository).save(any(TransactionLog.class));
    }

    @Test
    void checkTransaction_whenExtremeAmountAnomaly_shouldBlockAndCreateFlagWithPendingReview() {
        // avg = 100, multiplier=3, extremeThreshold=1000
        // amount = 1500 (> 1000) => BLOCKED
        TransactionRequest request = buildRequest(BigDecimal.valueOf(1500));

        when(blacklistedCardRepository.existsByCardNumberHash(anyString())).thenReturn(false);
        when(blacklistedMerchantRepository.findByMerchantId("M123")).thenReturn(Optional.empty());
        when(rateLimiterService.isIpBlocked(anyString(), anyString())).thenReturn(false);
        when(rateLimiterService.isCardVelocityExceeded(anyString())).thenReturn(false);

        CardSpendingProfile profile = CardSpendingProfile.builder()
                .cardNumberHash("somehash")
                .avgTransactionAmount(BigDecimal.valueOf(100))
                .maxSeenAmount(BigDecimal.valueOf(200))
                .transactionCount(5)
                .lastUpdated(LocalDateTime.now().minusDays(1))
                .build();

        when(cardSpendingProfileRepository.findByCardNumberHash(anyString()))
                .thenReturn(Optional.of(profile));

        ApiResponse<TransactionResponse> response = transactionService.checkTransaction(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData().getStatus()).isEqualTo(TransactionStatus.BLOCKED);

        ArgumentCaptor<FlaggedAttempt> flaggedCaptor = ArgumentCaptor.forClass(FlaggedAttempt.class);
        verify(flaggedAttemptRepository).save(flaggedCaptor.capture());
        FlaggedAttempt savedAttempt = flaggedCaptor.getValue();
        assertThat(savedAttempt.getFraudSignal()).isEqualTo(FraudSignal.AMOUNT_ANOMALY.name());
        assertThat(savedAttempt.getResolution()).isEqualTo(FlaggedResolution.PENDING_REVIEW);

        verify(cardSpendingProfileRepository, never()).save(any(CardSpendingProfile.class));
        verify(transactionLogRepository).save(any(TransactionLog.class));
    }

    @Test
    void checkTransaction_whenRepeatOffender_shouldBlacklistAndBlock() {
        TransactionRequest request = buildRequest(BigDecimal.valueOf(100));

        when(blacklistedCardRepository.existsByCardNumberHash(anyString())).thenReturn(false);
        when(blacklistedMerchantRepository.findByMerchantId("M123")).thenReturn(Optional.empty());
        when(rateLimiterService.isIpBlocked(anyString(), anyString())).thenReturn(false);
        when(rateLimiterService.isCardVelocityExceeded(anyString())).thenReturn(false);

        CardSpendingProfile profile = CardSpendingProfile.builder()
                .cardNumberHash("somehash")
                .avgTransactionAmount(BigDecimal.valueOf(50))
                .maxSeenAmount(BigDecimal.valueOf(100))
                .transactionCount(3)
                .lastUpdated(LocalDateTime.now().minusDays(1))
                .build();

        when(cardSpendingProfileRepository.findByCardNumberHash(anyString()))
                .thenReturn(Optional.of(profile));

        // flag count >= repeatOffenderMaxFlags (which is 3)
        when(flaggedAttemptRepository.countByCardNumberHashAndCreatedAtAfter(anyString(), any(LocalDateTime.class)))
                .thenReturn(3L);

        ApiResponse<TransactionResponse> response = transactionService.checkTransaction(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData().getStatus()).isEqualTo(TransactionStatus.BLOCKED);

        ArgumentCaptor<BlacklistedCard> cardCaptor = ArgumentCaptor.forClass(BlacklistedCard.class);
        verify(blacklistedCardRepository).save(cardCaptor.capture());
        assertThat(cardCaptor.getValue().getReason()).isEqualTo("REPEAT_OFFENDER");

        verify(transactionLogRepository).save(any(TransactionLog.class));
        verify(flaggedAttemptRepository).save(any(FlaggedAttempt.class));
    }
}