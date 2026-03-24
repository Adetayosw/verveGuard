package com.adetayo.verve_guard.repository;


import com.adetayo.verve_guard.entity.MerchantCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantCategoryRepository extends JpaRepository<MerchantCategory, Long> {

    Optional<MerchantCategory> findByCategoryName(String categoryName);
}
