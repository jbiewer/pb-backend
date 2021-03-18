package com.piggybank.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.SessionCookieOptions;
import com.piggybank.PiggyBankApplication;
import com.piggybank.repository.AccountRepository;
import com.piggybank.util.Request;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

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
    public AccountController(AccountRepository repository) {
        super(repository);
    }

    /**
     * Test mapping.
     * Used to see if the account endpoints are reachable.
     *
     * @return Greeting message.
     */
    @GetMapping(BASE_URL + "test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok(repository.test());
    }

    /**
     * todo
     * @param request
     * @return
     */
    @PostMapping(BASE_URL + "create")
    @ResponseBody
    public ResponseEntity<String> create() {
        return null;
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
        String token = request.getToken();
        long expiration = TimeUnit.DAYS.toMillis(3);

        // Attempt to generate a session cookie.
        String sessionCookie;
        try {
            SessionCookieOptions options = SessionCookieOptions.builder()
                    .setExpiresIn(expiration)
                    .build();
            sessionCookie = FirebaseAuth.getInstance().createSessionCookie(token, options);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to create a session");
        }

        // Configure cookie and respond with OK.
        Cookie cookie = new Cookie("session", sessionCookie);
        cookie.setMaxAge((int) expiration);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        return ResponseEntity.ok(repository.login(username, password));
    }
}
