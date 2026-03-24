package com.adetayo.verve_guard.repository;


import com.adetayo.verve_guard.entity.CustomerCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerCardRepository extends JpaRepository<CustomerCard, Long> {

    Optional<CustomerCard> findByCardNumberHash(String cardNumberHash);
}