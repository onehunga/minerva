package de.fallstudie.minerva.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
public class BackendApplication {
	static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
}
