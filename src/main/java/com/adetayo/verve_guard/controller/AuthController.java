package com.adetayo.verve_guard.controller;


import com.adetayo.verve_guard.dto.request.LoginRequest;
import com.adetayo.verve_guard.dto.response.LoginResponse;
import com.adetayo.verve_guard.response.ApiResponse;
import com.adetayo.verve_guard.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            ApiResponse<LoginResponse> response =
                    ApiResponse.error("Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        System.out.println("Userdetails: " + userDetails);
        String token = jwtUtil.generateToken(userDetails);
        System.out.println("token: " + token);

        LoginResponse loginResponse = LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(userDetails.getUsername())
                .expiresIn(jwtUtil.getJwtExpirationInMs())
                .build();

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", loginResponse)
        );
    }
}