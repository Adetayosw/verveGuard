package com.adetayo.verve_guard.controller;


import com.adetayo.verve_guard.dto.request.BlacklistMerchantRequest;
import com.adetayo.verve_guard.dto.response.BlacklistedCardResponse;
import com.adetayo.verve_guard.dto.response.BlacklistedMerchantResponse;
import com.adetayo.verve_guard.dto.response.FlaggedAttemptResponse;
import com.adetayo.verve_guard.dto.response.ApiResponse;
import com.adetayo.verve_guard.dto.response.PageResponse;
import com.adetayo.verve_guard.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/flagged-attempts")
    public ResponseEntity<ApiResponse<PageResponse<FlaggedAttemptResponse>>> getFlaggedAttempts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FlaggedAttemptResponse> result = adminService.getFlaggedAttempts(pageable);
        return ResponseEntity.ok(
                ApiResponse.success("Flagged attempts retrieved successfully", PageResponse.from(result))
        );
    }

    @GetMapping("/flagged-attempts/pending")
    public ResponseEntity<ApiResponse<Page<FlaggedAttemptResponse>>> getPendingReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FlaggedAttemptResponse> result = adminService.getPendingReviews(pageable);
        return ResponseEntity.ok(
                ApiResponse.success("Pending flagged attempts retrieved successfully", result)
        );
    }

    @GetMapping("/blacklisted-merchants")
    public ResponseEntity<ApiResponse<Page<BlacklistedMerchantResponse>>> getBlacklistedMerchants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BlacklistedMerchantResponse> result = adminService.getBlacklistedMerchants(pageable);
        return ResponseEntity.ok(
                ApiResponse.success("Blacklisted merchants retrieved successfully", result)
        );
    }

    @GetMapping("/blacklisted-cards")
    public ResponseEntity<ApiResponse<Page<BlacklistedCardResponse>>> getBlacklistedCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BlacklistedCardResponse> result = adminService.getBlacklistedCards(pageable);
        return ResponseEntity.ok(
                ApiResponse.success("Blacklisted cards retrieved successfully", result)
        );
    }

    @PostMapping("/resolve/{flaggedAttemptId}")
    public ResponseEntity<ApiResponse<Void>> resolveAttempt(
            @PathVariable Long flaggedAttemptId
    ) {
        adminService.resolveAttempt(flaggedAttemptId);
        return ResponseEntity.ok(
                ApiResponse.success("Flagged attempt resolved successfully")
        );
    }

    @PostMapping("/blacklist-merchant")
    public ResponseEntity<ApiResponse<Void>> blacklistMerchant(
            @Valid @RequestBody BlacklistMerchantRequest request,
            Authentication authentication
    ) {
        String adminUsername = authentication.getName();
        adminService.blacklistMerchant(request, adminUsername);
        return ResponseEntity.ok(
                ApiResponse.success("Merchant blacklisted successfully")
        );
    }
}
