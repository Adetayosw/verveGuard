package com.adetayo.verve_guard.service;

import com.adetayo.verve_guard.dto.request.BlacklistMerchantRequest;
import com.adetayo.verve_guard.dto.response.BlacklistedCardResponse;
import com.adetayo.verve_guard.dto.response.BlacklistedMerchantResponse;
import com.adetayo.verve_guard.dto.response.FlaggedAttemptResponse;
import com.adetayo.verve_guard.entity.BlacklistedCard;
import com.adetayo.verve_guard.entity.BlacklistedMerchant;
import com.adetayo.verve_guard.entity.FlaggedAttempt;
import com.adetayo.verve_guard.enums.FlaggedResolution;
import com.adetayo.verve_guard.enums.MerchantCategoryEnum;
import com.adetayo.verve_guard.exception.ResourceNotFoundException;
import com.adetayo.verve_guard.mapper.AdminMapper;
import com.adetayo.verve_guard.repository.BlacklistedCardRepository;
import com.adetayo.verve_guard.repository.BlacklistedMerchantRepository;
import com.adetayo.verve_guard.repository.FlaggedAttemptRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private FlaggedAttemptRepository flaggedAttemptRepository;
    @Mock
    private BlacklistedMerchantRepository blacklistedMerchantRepository;
    @Mock
    private BlacklistedCardRepository blacklistedCardRepository;

    @Mock
    private AdminMapper adminMapper;

    @InjectMocks
    private AdminService adminService;

    @Test
    void getFlaggedAttempts_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);

        FlaggedAttempt entity = FlaggedAttempt.builder()
                .id(1L)
                .cardNumberHash("hash")
                .cardLastFour("1234")
                .merchantId("M123")
                .amount(BigDecimal.valueOf(1000))
                .ipAddress("127.0.0.1")
                .fraudSignal("AMOUNT_ANOMALY")
                .fraudMessage("Anomaly detected")
                .resolution(FlaggedResolution.PENDING_REVIEW)
                .createdAt(LocalDateTime.now())
                .build();

        Page<FlaggedAttempt> page = new PageImpl<>(Collections.singletonList(entity), pageable, 1);
        when(flaggedAttemptRepository.findAll(pageable)).thenReturn(page);

        when(adminMapper.toFlaggedAttemptResponse(any()))
                .thenAnswer(invocation -> {
                    FlaggedAttempt a = invocation.getArgument(0);
                    return FlaggedAttemptResponse.builder()
                            .id(a.getId())
                            .cardNumberHash(a.getCardNumberHash())
                            .cardLastFour(a.getCardLastFour())
                            .merchantId(a.getMerchantId())
                            .amount(a.getAmount())
                            .ipAddress(a.getIpAddress())
                            .fraudSignal(a.getFraudSignal())
                            .fraudMessage(a.getFraudMessage())
                            .resolution(a.getResolution())
                            .createdAt(a.getCreatedAt())
                            .build();
                });

        Page<FlaggedAttemptResponse> result = adminService.getFlaggedAttempts(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        FlaggedAttemptResponse resp = result.getContent().get(0);
        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getCardNumberHash()).isEqualTo("hash");
        assertThat(resp.getCardLastFour()).isEqualTo("1234");
        assertThat(resp.getMerchantId()).isEqualTo("M123");
        assertThat(resp.getAmount()).isEqualByComparingTo("1000");
        assertThat(resp.getFraudSignal()).isEqualTo("AMOUNT_ANOMALY");
        assertThat(resp.getResolution()).isEqualTo(FlaggedResolution.PENDING_REVIEW);
    }

    @Disabled
    void getPendingReviews_shouldQueryPendingAndMap() {
        Pageable pageable = PageRequest.of(0, 10);

        FlaggedAttempt entity = FlaggedAttempt.builder()
                .id(2L)
                .cardNumberHash("hash2")
                .cardLastFour("5678")
                .merchantId("M456")
                .amount(BigDecimal.valueOf(500))
                .ipAddress("10.0.0.1")
                .fraudSignal("CARD_BLACKLISTED")
                .fraudMessage("Card blacklisted")
                .resolution(FlaggedResolution.PENDING_REVIEW)
                .createdAt(LocalDateTime.now())
                .build();

        Page<FlaggedAttempt> page = new PageImpl<>(Collections.singletonList(entity), pageable, 1);
        when(flaggedAttemptRepository.findByResolution(FlaggedResolution.PENDING_REVIEW, pageable))
                .thenReturn(page);



        Page<FlaggedAttemptResponse> result = adminService.getPendingReviews(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        FlaggedAttemptResponse resp = result.getContent().get(0);
        assertThat(resp.getId()).isEqualTo(2L);
        assertThat(resp.getResolution()).isEqualTo(FlaggedResolution.PENDING_REVIEW);

        verify(flaggedAttemptRepository).findByResolution(FlaggedResolution.PENDING_REVIEW, pageable);
    }

    @Test
    void getBlacklistedMerchants_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);

        BlacklistedMerchant merchant = BlacklistedMerchant.builder()
                .id(1L)
                .merchantId("M123")
                .merchantCategory("BASIC")
                .reason("Fraud")
                .blacklistedBy("admin")
                .blacklistedAt(LocalDateTime.now())
                .build();

        Page<BlacklistedMerchant> page = new PageImpl<>(Collections.singletonList(merchant), pageable, 1);
        when(blacklistedMerchantRepository.findAll(pageable)).thenReturn(page);

        when(adminMapper.toBlacklistedMerchantResponse(any()))
                .thenAnswer(invocation -> {
                    BlacklistedMerchant m = invocation.getArgument(0);
                    return BlacklistedMerchantResponse.builder()
                            .id(m.getId())
                            .merchantId(m.getMerchantId())
                            .merchantCategory(m.getMerchantCategory())
                            .reason(m.getReason())
                            .blacklistedBy(m.getBlacklistedBy())
                            .blacklistedAt(m.getBlacklistedAt())
                            .build();
                });

        Page<BlacklistedMerchantResponse> result = adminService.getBlacklistedMerchants(pageable);


        assertThat(result.getTotalElements()).isEqualTo(1);
        BlacklistedMerchantResponse resp = result.getContent().get(0);
        assertThat(resp.getMerchantId()).isEqualTo("M123");
        assertThat(resp.getMerchantCategory()).isEqualTo("BASIC");
        assertThat(resp.getReason()).isEqualTo("Fraud");
        assertThat(resp.getBlacklistedBy()).isEqualTo("admin");
    }

    @Test
    void getBlacklistedCards_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);

        BlacklistedCard card = BlacklistedCard.builder()
                .id(1L)
                .cardNumberHash("hash")
                .cardLastFour("1234")
                .reason("VELOCITY_EXCEEDED")
                .blacklistedAt(LocalDateTime.now())
                .build();

        Page<BlacklistedCard> page = new PageImpl<>(Collections.singletonList(card), pageable, 1);
        when(blacklistedCardRepository.findAll(pageable)).thenReturn(page);

        when(adminMapper.toBlacklistedCardResponse(any()))
                .thenAnswer(invocation -> {
                    BlacklistedCard c = invocation.getArgument(0);
                    return BlacklistedCardResponse.builder()
                            .id(c.getId())
                            .cardNumberHash(c.getCardNumberHash())
                            .cardLastFour(c.getCardLastFour())
                            .reason(c.getReason())
                            .blacklistedAt(c.getBlacklistedAt())
                            .build();
                });

        Page<BlacklistedCardResponse> result = adminService.getBlacklistedCards(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        BlacklistedCardResponse resp = result.getContent().get(0);
        assertThat(resp.getCardNumberHash()).isEqualTo("hash");
        assertThat(resp.getCardLastFour()).isEqualTo("1234");
        assertThat(resp.getReason()).isEqualTo("VELOCITY_EXCEEDED");
    }

    @Test
    void resolveAttempt_whenFound_shouldSetResolutionAndSave() {
        FlaggedAttempt attempt = FlaggedAttempt.builder()
                .id(10L)
                .resolution(FlaggedResolution.PENDING_REVIEW)
                .build();

        when(flaggedAttemptRepository.findById(10L)).thenReturn(Optional.of(attempt));

        adminService.resolveAttempt(10L);

        assertThat(attempt.getResolution()).isEqualTo(FlaggedResolution.ADMIN_RESOLVED);
        verify(flaggedAttemptRepository).save(attempt);
    }

    @Test
    void resolveAttempt_whenNotFound_shouldThrow() {
        when(flaggedAttemptRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.resolveAttempt(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Flagged attempt not found");

        verify(flaggedAttemptRepository, never()).save(any());
    }

    @Test
    void blacklistMerchant_shouldBuildAndSaveEntity() {
        BlacklistMerchantRequest request = new BlacklistMerchantRequest();
        request.setMerchantId("M999");
        request.setMerchantCategory(MerchantCategoryEnum.BASIC);
        request.setReason("Suspected fraud");

        String adminUsername = "adminUser";

        adminService.blacklistMerchant(request, adminUsername);

        ArgumentCaptor<BlacklistedMerchant> captor = ArgumentCaptor.forClass(BlacklistedMerchant.class);
        verify(blacklistedMerchantRepository).save(captor.capture());

        BlacklistedMerchant saved = captor.getValue();
        assertThat(saved.getMerchantId()).isEqualTo("M999");
        assertThat(saved.getMerchantCategory()).isEqualTo("BASIC");
        assertThat(saved.getReason()).isEqualTo("Suspected fraud");
        assertThat(saved.getBlacklistedBy()).isEqualTo("adminUser");
        assertThat(saved.getBlacklistedAt()).isNotNull();
    }
}