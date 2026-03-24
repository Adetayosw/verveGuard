package com.adetayo.verve_guard.repository;


import com.adetayo.verve_guard.entity.BlacklistedCard;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistedCardRepository extends JpaRepository<BlacklistedCard, Integer> {

    boolean existsByCardNumberHash(String cardNumberHash);

    @NonNull
    Page<BlacklistedCard> findAll(Pageable pageable);
}
