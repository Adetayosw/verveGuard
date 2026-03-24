package com.adetayo.verve_guard.repository;


import com.adetayo.verve_guard.entity.BlacklistedMerchant;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlacklistedMerchantRepository extends JpaRepository<BlacklistedMerchant, Long> {

    boolean existsByMerchantId(String merchantId);

    Optional<BlacklistedMerchant> findByMerchantId(String merchantId);

    @NonNull
    Page<BlacklistedMerchant> findAll(Pageable pageable);
}
