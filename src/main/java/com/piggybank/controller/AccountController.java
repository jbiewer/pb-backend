package com.piggybank.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.piggybank.PiggyBankApplication;
import com.piggybank.components.SessionAuthenticator;
import com.piggybank.model.Account;
import com.piggybank.repository.AccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.message.AuthException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Account-related application interface.
 * Base URL: /api/v1/account/
 */
@RestController
public class AccountController extends PBController<AccountRepository> {
    private static final String BASE_URL = PiggyBankApplication.BASE_URL + "account/";

//    /**
//     * Bean initializer constructor.
//     * @param repository - Repository bean for the accounts.
//     */
//    public AccountController(AccountRepository repository, SessionAuthenticator authenticator) {
//        super(repository, authenticator);
//    }

    /**
     * Test mapping.
     * Used to see if the account endpoints are reachable.
     *
     * @param message Request information - contains message to send back as the response.
     * @param sessionCookieId Session cookie to validate the connection to the API.
     * @return Greeting message.
     */
    @GetMapping(BASE_URL + "test")
    public ResponseEntity<?> test(
            @RequestBody(required = false) String message,
            @CookieValue(value = "session", required = false) String sessionCookieId
    ) {
        try {
            if (sessionCookieId != null) {
                authenticator.validateSession(sessionCookieId);
            }
            return ResponseEntity.ok(repository.test(message));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to validate session");
        }
    }

    /**
     * Type: POST
     * Path: /api/v1/account/create
     * Body: Customer object.
     *
     * Takes in an account serialized object and uploads it to the repository.
     * If an account with the username already exists, an error will be returned in the result. If no account type
     * is specified, and error will be returned as well.
     *
     * Example:
     *   curl -X POST URL/api/v1/account/customer/create?token={token}
     *        -H 'Content-Type:application/json'
     *        -d '{
     *              "username": "abcde",
     *              "password": "kj3h6jh5kj6g54kk7hk6hj7",
     *              "email": "email@email.com",
     *              "type": "CUSTOMER",
     *                  ...
     *            }'
     *
     * @param token - Token created by firebase authentication system
     * @param newAccount - Account object containing initial fields. Type field is required
     * @param response
     * @return
     */
    @PostMapping(BASE_URL + "create")
    public ResponseEntity<?> create(
            @RequestParam String token,
            @RequestBody Account newAccount,
            HttpServletResponse response
    ) {
        // Validate token and generate a new session.
        try {
            Cookie cookie = authenticator.generateNewSession(token);
            response.addCookie(cookie);
            return ResponseEntity.ok(repository.create(newAccount));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to create a session");
        } catch (AuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Recent sign in required");
        } catch (Throwable t) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(t.getMessage());
        }
    }

    /**
     * todo
     * @param token
     * @param response
     * @return
     */
    @PostMapping(BASE_URL + "log-in")
    public ResponseEntity<?> login(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String token,
            HttpServletResponse response
    ) {
        try {
            response.addCookie(authenticator.generateNewSession(token));
            return ResponseEntity.ok(repository.login(email, password));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to create a session");
        } catch (AuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Recent sign in required");
        } catch (Throwable t) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(t.getMessage());
        }
    }

    /**
     * todo
     * @param sessionCookieId
     * @return
     */
    @PostMapping(BASE_URL + "log-out")
    public ResponseEntity<?> logout(
            @CookieValue(value = "session") String sessionCookieId
    ) {
        try {
            authenticator.clearSessionAndRevoke(sessionCookieId);
            return ResponseEntity.ok("Logout successful");
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to create a session");
        }

    }

    /**
     * todo
     * @param email
     * @param content
     * @param sessionCookieId
     * @return
     */
    @PutMapping(BASE_URL + "update")
    public ResponseEntity<?> update(
            @RequestParam String email,
            @RequestBody Account content,
            @CookieValue(value = "session") String sessionCookieId
    ) {
        try {
            authenticator.validateSession(sessionCookieId);
            return ResponseEntity.ok(repository.update(email, content));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to validate session");
        } catch (Throwable t) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(t.getMessage());
        }
    }

    /**
     * todo
     * @param email
     * @param sessionCookieId
     * @return
     */
    @GetMapping(BASE_URL + "get")
    public ResponseEntity<?> get(
            @RequestParam String email,
            @CookieValue(value = "session") String sessionCookieId
    ) {
        try {
            authenticator.validateSession(sessionCookieId);
            Account account = repository.get(email);
            return ResponseEntity.ok(account);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to validate session");
        } catch (Throwable t) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(t.getMessage());
        }
    }

    /**
     * 
     * @param email
     * @return
     */
    @GetMapping(BASE_URL + "usernameExists")
    public ResponseEntity<?> emailExists(@RequestParam String username) {
        try {
            return ResponseEntity.ok(repository.usernameExists(username));
        } catch(Throwable t) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(t.getMessage());
        }
    }
}
