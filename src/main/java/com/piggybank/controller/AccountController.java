package com.piggybank.controller;

import com.piggybank.PiggyBankApplication;
import com.piggybank.repository.AccountRepository;
import com.piggybank.util.Result;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * Account-related application interface.
 * Base URL: /api/v1/account/
 */
@RestController
public class AccountController {
    private static final String BASE_URL = PiggyBankApplication.BASE_URL + "account/";

    private final AccountRepository repository;

    /**
     * Bean initializer constructor.
     * @param repository - Repository bean for the accounts.
     */
    public AccountController(AccountRepository repository) {
        this.repository = repository;
    }

    /**
     * Test mapping.
     * Used to see if the account endpoints are reachable.
     *
     * @return Greeting message.
     */
    @GetMapping(BASE_URL + "test")
    public Result<String> test() {
        return repository.test();
    }
}
