package com.fintrack.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Classe principal da aplicação FinTrack API.
 * Gestão de finanças pessoais — portfólio profissional Java/Spring.
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class FintrackApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FintrackApiApplication.class, args);
    }
}
