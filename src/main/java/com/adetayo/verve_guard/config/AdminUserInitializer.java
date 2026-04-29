package com.adetayo.verve_guard.config;

import com.adetayo.verve_guard.entity.User;
import com.adetayo.verve_guard.enums.Role;
import com.adetayo.verve_guard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.email}")
    private String adminEmail;

    @Override
    public void run(String... args) {
        if (adminUsername == null || adminUsername.isBlank()) {
            return;
        }

        boolean exists = userRepository.findByUsername(adminUsername).isPresent();
        if (exists) {
            return;
        }

        User admin = User.builder()
                .username(adminUsername)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ROLE_ADMIN)
                .build();

        userRepository.save(admin);
    }
}
