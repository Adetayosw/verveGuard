package com.adetayo.verve_guard.entity;

import com.adetayo.verve_guard.enums.FlaggedResolution;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "flagged_attempts")
public class FlaggedAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number_hash", nullable = false)
    private String cardNumberHash; // must not be null

    @Column(name = "card_last_four")
    private String cardLastFour;

    @Column(name = "merchant_id", nullable = false)
    private String merchantId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "fraud_signal", nullable = false)
    private String fraudSignal;

    @Column(name = "fraud_message", nullable = false)
    private String fraudMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "resolution")
    private FlaggedResolution resolution;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}