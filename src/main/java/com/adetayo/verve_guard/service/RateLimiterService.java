package com.adetayo.verve_guard.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class RateLimiterService {


    private final int ipMaxRequests;
    private final long ipWindowMillis;
    private final int ipMultiCardMaxCards;
    private final int cardMaxRequests;
    private final long cardWindowMillis;

    private final ConcurrentMap<String, RequestTracker> ipRequestMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, CardVelocityTracker> cardRequestMap = new ConcurrentHashMap<>();

    private final Set<String> permanentlyBlockedIps =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final ConcurrentMap<String, Set<String>> ipToCardsMap = new ConcurrentHashMap<>();

    public RateLimiterService(
            @Value("${fraud.ip.rate-limit.max-requests}") int ipMaxRequests,
            @Value("${fraud.ip.rate-limit.window-seconds}") long ipWindowSeconds,
            @Value("${fraud.ip.multi-card.max-cards}") int ipMultiCardMaxCards,
            @Value("${fraud.card.velocity.max-requests}") int cardMaxRequests,
            @Value("${fraud.card.velocity.window-minutes}") long cardWindowMinutes
    ) {
        this.ipMaxRequests = ipMaxRequests;
        this.ipWindowMillis = ipWindowSeconds * 1000;
        this.ipMultiCardMaxCards = ipMultiCardMaxCards;
        this.cardMaxRequests = cardMaxRequests;
        this.cardWindowMillis = cardWindowMinutes * 60 * 1000;
    }


    public boolean isIpBlocked(String ipAddress, String cardNumberHash) {
        long now = Instant.now().toEpochMilli();

        // If already permanently blocked
        if (permanentlyBlockedIps.contains(ipAddress)) {
            return true;
        }

        // Track rate for this IP
        RequestTracker tracker = ipRequestMap.compute(ipAddress, (ip, existing) -> {
            if (existing == null || isWindowExpired(existing.getWindowStartMillis(), now, ipWindowMillis)) {
                return new RequestTracker(1, now);
            } else {
                existing.increment();
                return existing;
            }
        });

        // Rate limit exceeded
        if (tracker.getRequestCount() > ipMaxRequests) {
            return true;
        }

        // Track distinct card hashes per IP
        Set<String> cardsForIp = ipToCardsMap.computeIfAbsent(
                ipAddress,
                k -> Collections.newSetFromMap(new ConcurrentHashMap<>())
        );  
        cardsForIp.add(cardNumberHash);

        if (cardsForIp.size() > ipMultiCardMaxCards) {
            permanentlyBlockedIps.add(ipAddress);
            return true;
        }

        return false;
    }


    public boolean isCardVelocityExceeded(String cardNumberHash) {
        long now = Instant.now().toEpochMilli();

        CardVelocityTracker tracker = cardRequestMap.compute(cardNumberHash, (card, existing) -> {
            if (existing == null || isWindowExpired(existing.getWindowStartMillis(), now, cardWindowMillis)) {
                return new CardVelocityTracker(1, now);
            } else {
                existing.increment();
                return existing;
            }
        });

        return tracker.getRequestCount() > cardMaxRequests;
    }

    /**
     * Purge stale entries from in-memory tracking structures.
     * Runs once per minute.
     */
    @Scheduled(fixedRate = 60000)
    public void purgeStaleEntries() {
        long now = Instant.now().toEpochMilli();

        // Clean IP trackers
        ipRequestMap.entrySet().removeIf(entry ->
                isWindowExpired(entry.getValue().getWindowStartMillis(), now, ipWindowMillis)
        );

        // Clean card trackers
        cardRequestMap.entrySet().removeIf(entry ->
                isWindowExpired(entry.getValue().getWindowStartMillis(), now, cardWindowMillis)
        );

        // Clean IP->cards map where IP is no longer tracked and not permanently blocked
        ipToCardsMap.keySet().removeIf(ip ->
                !ipRequestMap.containsKey(ip) && !permanentlyBlockedIps.contains(ip)
        );
    }

    private boolean isWindowExpired(long windowStartMillis, long nowMillis, long windowDurationMillis) {
        return (nowMillis - windowStartMillis) > windowDurationMillis;
    }

    // For Actuator health indicator
    public int getTrackedIpCount() {
        return ipRequestMap.size();
    }

    public int getTrackedCardCount() {
        return cardRequestMap.size();
    }


    private static class RequestTracker {
        private int requestCount;
        private final long windowStartMillis;

        RequestTracker(int count, long windowStartMillis) {
            this.requestCount = count;
            this.windowStartMillis = windowStartMillis;
        }

        void increment() {
            this.requestCount++;
        }

        int getRequestCount() {
            return requestCount;
        }

        long getWindowStartMillis() {
            return windowStartMillis;
        }
    }

    private static class CardVelocityTracker {
        private int requestCount;
        private final long windowStartMillis;

        CardVelocityTracker(int count, long windowStartMillis) {
            this.requestCount = count;
            this.windowStartMillis = windowStartMillis;
        }

        void increment() {
            this.requestCount++;
        }

        int getRequestCount() {
            return requestCount;
        }

        long getWindowStartMillis() {
            return windowStartMillis;
        }
    }
}
