package com.adetayo.verve_guard.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterServiceTest {


    private RateLimiterService newRateLimiter(
            int ipMaxRequests,
            long ipWindowSeconds,
            int ipMultiCardMaxCards,
            int cardMaxRequests,
            long cardWindowMinutes
    ) {
        return new RateLimiterService(
                ipMaxRequests,
                ipWindowSeconds,
                ipMultiCardMaxCards,
                cardMaxRequests,
                cardWindowMinutes
        );
    }

    @Test
    void isIpBlocked_whenWithinRateLimitAndFewCards_shouldNotBlock() {
        RateLimiterService rateLimiter = newRateLimiter(
                5,   // ipMaxRequests
                60,  // ipWindowSeconds
                3,   // ipMultiCardMaxCards
                10,  // cardMaxRequests
                10   // cardWindowMinutes
        );

        String ip = "10.0.0.1";
        String cardHash = "cardHash1";

        boolean blocked1 = rateLimiter.isIpBlocked(ip, cardHash);
        boolean blocked2 = rateLimiter.isIpBlocked(ip, cardHash);

        assertThat(blocked1).isFalse();
        assertThat(blocked2).isFalse();
    }

    @Test
    void isIpBlocked_whenRequestCountExceedsMax_shouldBlock() {
        RateLimiterService rateLimiter = newRateLimiter(
                3,   // ipMaxRequests
                60,  // ipWindowSeconds
                10,  // ipMultiCardMaxCards (not relevant here)
                10,
                10
        );

        String ip = "10.0.0.2";
        String cardHash = "cardHash1";

        // First 3 requests: allowed
        assertThat(rateLimiter.isIpBlocked(ip, cardHash)).isFalse();
        assertThat(rateLimiter.isIpBlocked(ip, cardHash)).isFalse();
        assertThat(rateLimiter.isIpBlocked(ip, cardHash)).isFalse();
        // 4th request: exceeds ipMaxRequests -> blocked
        assertThat(rateLimiter.isIpBlocked(ip, cardHash)).isTrue();
    }

    @Test
    void isIpBlocked_whenMultipleDistinctCardsExceedLimit_shouldPermanentlyBlockIp() {
        RateLimiterService rateLimiter = newRateLimiter(
                100, // ipMaxRequests: very high so rate-limit doesn't trigger
                60,
                2,   // ipMultiCardMaxCards: allow up to 2 cards, 3rd should block
                10,
                10
        );

        String ip = "10.0.0.3";

        // First card
        assertThat(rateLimiter.isIpBlocked(ip, "card1")).isFalse();
        // Second card
        assertThat(rateLimiter.isIpBlocked(ip, "card2")).isFalse();
        // Third distinct card => should trigger permanent block
        boolean blockedOnThirdCard = rateLimiter.isIpBlocked(ip, "card3");
        assertThat(blockedOnThirdCard).isTrue();

        // Subsequent calls from same IP should be blocked regardless of card
        assertThat(rateLimiter.isIpBlocked(ip, "card4")).isTrue();
        assertThat(rateLimiter.isIpBlocked(ip, "card1")).isTrue();
    }

    @Test
    void isCardVelocityExceeded_whenWithinLimit_shouldNotBlock() {
        RateLimiterService rateLimiter = newRateLimiter(
                5,
                60,
                3,
                3,   // cardMaxRequests
                10
        );

        String cardHash = "cardHashVelocity1";

        // 3 requests: equal to cardMaxRequests -> not exceeded
        assertThat(rateLimiter.isCardVelocityExceeded(cardHash)).isFalse();
        assertThat(rateLimiter.isCardVelocityExceeded(cardHash)).isFalse();
        assertThat(rateLimiter.isCardVelocityExceeded(cardHash)).isFalse();
    }

    @Test
    void isCardVelocityExceeded_whenExceedsLimit_shouldBlock() {
        RateLimiterService rateLimiter = newRateLimiter(
                5,
                60,
                3,
                3,   // cardMaxRequests
                10
        );

        String cardHash = "cardHashVelocity2";

        // 3 requests: within limit
        rateLimiter.isCardVelocityExceeded(cardHash);
        rateLimiter.isCardVelocityExceeded(cardHash);
        rateLimiter.isCardVelocityExceeded(cardHash);

        // 4th request: exceeds cardMaxRequests -> true
        boolean exceeded = rateLimiter.isCardVelocityExceeded(cardHash);
        assertThat(exceeded).isTrue();
    }

    @Test
    void purgeStaleEntries_shouldRemoveExpiredIpAndCardTrackers() throws InterruptedException {
        // set very small windows so entries expire quickly
        RateLimiterService rateLimiter = newRateLimiter(
                5,
                0,    // ipWindowSeconds = 0 -> immediate expiry
                3,
                5,
                0     // cardWindowMinutes = 0 -> immediate expiry
        );

        String ip = "10.0.0.4";
        String cardHash = "cardHashPurge";

        // Create some entries
        rateLimiter.isIpBlocked(ip, cardHash);
        rateLimiter.isCardVelocityExceeded(cardHash);

        assertThat(rateLimiter.getTrackedIpCount()).isGreaterThan(0);
        assertThat(rateLimiter.getTrackedCardCount()).isGreaterThan(0);

        // Purge (since window durations are effectively 0, everything should be stale)
        rateLimiter.purgeStaleEntries();

        assertThat(rateLimiter.getTrackedIpCount()).isEqualTo(0);
        assertThat(rateLimiter.getTrackedCardCount()).isEqualTo(0);
    }
}