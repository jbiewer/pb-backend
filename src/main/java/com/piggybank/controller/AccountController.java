package com.piggybank.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.piggybank.PiggyBankApplication;
import com.piggybank.model.Account;
import com.piggybank.repository.AccountRepository;
import com.piggybank.components.SessionAuthenticator;
import com.piggybank.util.Request;
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

    private final SessionAuthenticator authenticator;

    /**
     * Bean initializer constructor.
     * @param repository - Repository bean for the accounts.
     */
    public AccountController(AccountRepository repository, SessionAuthenticator authenticator) {
        super(repository);
        this.authenticator = authenticator;
    }

    /**
     * Test mapping.
     * Used to see if the account endpoints are reachable.
     *
     * @param request Request information - contains message to send back as the response.
     * @param sessionCookie Session cookie to validate the connection to the API.
     * @return Greeting message.
     */
    @GetMapping(BASE_URL + "test")
    @ResponseBody
    public ResponseEntity<String> test(
            @RequestBody Request<String> request,
            @CookieValue(value = "session") String sessionCookie
    ) {
        try {
            authenticator.validateSession(sessionCookie);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to validate session");
        }
        return ResponseEntity.ok(repository.test(request.getData()));
    }

    /**
     * todo
     * @param request
     * @return
     */
    @PostMapping(BASE_URL + "create")
    @ResponseBody
    public ResponseEntity<String> create(
            @RequestBody Request<Account> request,
            HttpServletResponse response
    ) {
        try {
            Cookie cookie = authenticator.generateNewSession(request.getToken());
            response.addCookie(cookie);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to create a session");
        } catch (AuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Recent sign in required");
        }
        return ResponseEntity.ok(repository.create(request.getData()));
    }

    /**
     * todo
     * @param username
     * @param password
     * @param request
     * @param response
     * @return
     */
    @PostMapping(BASE_URL + "log-in")
    @ResponseBody
    public ResponseEntity<String> login(
            @RequestParam String username,
            @RequestParam String password,
            @RequestBody Request<Void> request,
            HttpServletResponse response
    ) {
        try {
            Cookie cookie = authenticator.generateNewSession(request.getToken());
            response.addCookie(cookie);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to create a session");
        } catch (AuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Recent sign in required");
        }
        return ResponseEntity.ok(repository.login(username, password));
    }
}
