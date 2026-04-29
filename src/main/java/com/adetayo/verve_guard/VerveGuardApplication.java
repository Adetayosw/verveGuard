package com.adetayo.verve_guard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.github.cdimascio.dotenv.Dotenv;

@EnableScheduling
@SpringBootApplication
public class VerveGuardApplication {

	public static void main(String[] args) {

		Dotenv dotenv = Dotenv.configure().load();

		dotenv.entries().forEach(entry -> {
			System.setProperty(entry.getKey(), entry.getValue());
		});

		SpringApplication.run(VerveGuardApplication.class, args);
	}

}
