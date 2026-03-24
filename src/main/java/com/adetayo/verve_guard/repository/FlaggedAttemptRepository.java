package com.adetayo.verve_guard.repository;


import com.adetayo.verve_guard.entity.FlaggedAttempt;
import com.adetayo.verve_guard.enums.FlaggedResolution;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface FlaggedAttemptRepository extends JpaRepository<FlaggedAttempt, Long> {

    @NonNull
    Page<FlaggedAttempt> findAll(Pageable pageable);

    Page<FlaggedAttempt> findByResolution(FlaggedResolution resolution, Pageable pageable);

    long countByCardNumberHashAndCreatedAtAfter(String cardNumberHash, LocalDateTime createdAtAfter);
}