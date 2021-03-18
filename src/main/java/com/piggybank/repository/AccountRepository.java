package com.piggybank.repository;

import com.piggybank.model.Account;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * Interface for database interactions for accounts.
 */
@Repository
public class AccountRepository extends PBRepository {

    /**
     * Initializes the collection reference to the value at the specified property location
     * in application.yml.
     *
     * @param env Environment containing properties.
     */
    public AccountRepository(Environment env) {
        super(Objects.requireNonNull(env.getProperty("firebase.database.labels.accounts")));
    }

    /**
     * Tests the AccountController interface.
     *
     * @return Result of query.
     */
    public String test(String message) {
        return "Success! Here is your message: " + message;
    }

    /**
     * todo
     * @param account
     * @return
     */
    @NonNull
    public String create(Account newAccount) {
        return "filler";
    }

    /**
     * todo
     * @param username
     * @param password
     * @return
     */
    @NonNull
    public String login(String username, String password) {
        return "filler";
    }
}
