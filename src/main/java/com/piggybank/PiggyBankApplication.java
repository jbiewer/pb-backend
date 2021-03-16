package com.piggybank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application class that holds the global context of the program.
 * All global-scope (static) variables that relate to the program as a whole will be in here, as well as the entry point
 * to the program (main).
 */
@SpringBootApplication
public class PiggyBankApplication {
    public static final String BASE_URL = "/api/v1/";

    public static void main(String[] args) {
        SpringApplication.run(PiggyBankApplication.class, args);
    }
}
