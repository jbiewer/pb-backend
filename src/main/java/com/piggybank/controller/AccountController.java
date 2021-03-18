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
//     * @param sessionCookie Session cookie to validate the connection to the API.
     * @return Greeting message.
     */
    @GetMapping(BASE_URL + "test")
    public ResponseEntity<String> test(
            @RequestBody String message,
            @CookieValue(value = "session", required = false) String sessionCookie
    ) {
        if (sessionCookie != null) {
            try {
                authenticator.validateSession(sessionCookie);
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
     * @param sessionCookie
     * @return
     */
    @PostMapping(BASE_URL + "log-out")
    public ResponseEntity<String> logout(
            @CookieValue(value = "session") String sessionCookie
    ) {
        try {
            authenticator.clearSessionAndRevoke(sessionCookie);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to create a session");
        }
        return ResponseEntity.ok("Logout successful");
    }

}
