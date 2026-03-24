package com.adetayo.verve_guard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class VerveGuardApplication {

	public static void main(String[] args) {
		SpringApplication.run(VerveGuardApplication.class, args);
	}

}
