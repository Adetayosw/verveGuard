package com.adetayo.verve_guard.controller;


import com.adetayo.verve_guard.dto.request.TransactionRequest;
import com.adetayo.verve_guard.dto.response.TransactionResponse;
import com.adetayo.verve_guard.response.ApiResponse;
import com.adetayo.verve_guard.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<TransactionResponse>> checkTransaction(
            @Valid @RequestBody TransactionRequest request
    ) {
        ApiResponse<TransactionResponse> response = transactionService.checkTransaction(request);
        // success=true → 200 OK, success=false → 400 Bad Request
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }
}
