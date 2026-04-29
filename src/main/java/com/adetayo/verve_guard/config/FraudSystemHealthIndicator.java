package com.adetayo.verve_guard.config;

import com.adetayo.verve_guard.repository.BlacklistedCardRepository;
import com.adetayo.verve_guard.repository.BlacklistedMerchantRepository;
import com.adetayo.verve_guard.service.RateLimiterService;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FraudSystemHealthIndicator implements HealthIndicator {

    private final BlacklistedMerchantRepository blacklistedMerchantRepository;
    private final BlacklistedCardRepository blacklistedCardRepository;
    private final RateLimiterService rateLimiterService;

    @Override
    public Health health() {
        try {
            long blacklistedMerchants = blacklistedMerchantRepository.count();
            long blacklistedCards = blacklistedCardRepository.count();
            int trackedIpCount = rateLimiterService.getTrackedIpCount();
            int trackedCardCount = rateLimiterService.getTrackedCardCount();

            return Health.up()
                    .withDetail("blacklistedMerchantsCount", blacklistedMerchants)
                    .withDetail("blacklistedCardsCount", blacklistedCards)
                    .withDetail("rateLimiterTrackedIpCount", trackedIpCount)
                    .withDetail("rateLimiterTrackedCardCount", trackedCardCount)
                    .build();
        } catch (Exception ex) {
            return Health.down()
                    .withDetail("error", ex.getMessage())
                    .build();
        }
    }
}