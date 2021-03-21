package com.piggybank.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.piggybank.PiggyBankApplication;
import com.piggybank.model.Account;
import com.piggybank.repository.AccountRepository;
import com.piggybank.components.SessionAuthenticator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.message.AuthException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import java.util.Objects;

import static com.piggybank.model.Account.AccountType;

/**
 * Account-related application interface.
 * Base URL: /api/v1/account/
 */
@RestController
public class AccountController extends PBController<AccountRepository> {
    private static final String BASE_URL = PiggyBankApplication.BASE_URL + "account/";

    /**
     * Bean initializer constructor.
     * @param repository - Repository bean for the accounts.
     */
    public AccountController(AccountRepository repository, SessionAuthenticator authenticator) {
        super(repository, authenticator);
    }

    /**
     * Test mapping.
     * Used to see if the account endpoints are reachable.
     *
//     * @param request Request information - contains message to send back as the response.
//     * @param sessionCookieId Session cookie to validate the connection to the API.
     * @return Greeting message.
     */
    @GetMapping(BASE_URL + "test")
    public ResponseEntity<String> test(
            @RequestBody String message,
            @CookieValue(value = "session", required = false) String sessionCookieId
    ) {
        if (sessionCookieId != null) {
            try {
                authenticator.validateSession(sessionCookieId);
            } catch (FirebaseAuthException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to validate session");
            }
        }

        return ResponseEntity.ok(repository.test(message));
    }

    /**
     * todo
     * @param token
     * @param newAccount
     * @param response
     * @return
     */
    @PostMapping(BASE_URL + "create")
    public ResponseEntity<String> create(
            @RequestParam String token,
            @RequestBody Account newAccount,
            HttpServletResponse response
    ) {
        // Validate token and generate a new session.
        try {
            Cookie cookie = authenticator.generateNewSession(token);
            response.addCookie(cookie);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to create a session");
        } catch (AuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Recent sign in required");
        }

        // Attempt to create a new account.
        switch (newAccount.getType()) {
            case MERCHANT:
                if (newAccount.getBankAccount() == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Merchant account requires bank account.");
                }
            case CUSTOMER:
                return ResponseEntity.ok(repository.create(newAccount));
            default:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Account type must be specified");
        }
    }

    /**
     * todo
     * @param token
     * @param response
     * @return
     */
    @PostMapping(BASE_URL + "log-in")
    public ResponseEntity<String> login(
            @RequestParam String token,
            HttpServletResponse response
    ) {
        try {
            Cookie cookie = authenticator.generateNewSession(token);
            response.addCookie(cookie);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to create a session");
        } catch (AuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Recent sign in required");
        }

        return ResponseEntity.ok("Login successful");
    }

    /**
     * todo
     * @param sessionCookieId
     * @return
     */
    @PostMapping(BASE_URL + "log-out")
    public ResponseEntity<String> logout(
            @CookieValue(value = "session") String sessionCookieId
    ) {
        try {
            authenticator.clearSessionAndRevoke(sessionCookieId);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to create a session");
        }

        return ResponseEntity.ok("Logout successful");
    }

    /**
     * todo
     * @param username
     * @param content
     * @param sessionCookieId
     * @return
     */
    @PutMapping(BASE_URL + "update")
    public ResponseEntity<String> update(
            @RequestParam String username,
            @RequestBody Account content,
            @CookieValue(value = "session") String sessionCookieId
    ) {
        try {
            authenticator.validateSession(sessionCookieId);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to validate session");
        }

        return ResponseEntity.ok(repository.update(username, content));
    }
}
