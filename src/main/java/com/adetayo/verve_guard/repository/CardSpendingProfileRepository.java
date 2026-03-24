package com.adetayo.verve_guard.repository;

import com.adetayo.verve_guard.entity.CardSpendingProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardSpendingProfileRepository extends JpaRepository<CardSpendingProfile, Long> {

    Optional<CardSpendingProfile> findByCardNumberHash(String cardNumberHash);
}
