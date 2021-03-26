package com.piggybank.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.piggybank.PiggyBankApplication;
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
     * Body: Account object.
     *
     * Takes in an account serialized object and uploads it to the repository.
     * If an account with the username already exists, an error will be returned in the result. If no account type
     * is specified, and error will be returned as well.
     *
     * Example:
     *   curl -X POST URL/api/v1/account/create?token={token}
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
     * @param response - Does not need to be specified, will be returned automatically, contains cookie
     * @return - If all parameters are valid, an HTTP response w/ status 200 OK containing a success message.
     *           If the newAccount parameter is invalid, an HTTP response w/ status 400 BAD REQUEST.
     *           If the token parameter is invalid, an HTTP response w/ status 401 UNAUTHORIZED.
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
     * Type: POST
     * Path: /api/v1/account/log-in
     *
     * Takes in email and password along with the token returned 
     * from Firebase's signInWithEmailAndPassword method. If valid token and 
     * account exists, returns cookie and allows user to log in. Returns error if
     * no such account is found, or if token invalid
     * 
     *
     * Example:
     *   curl -X POST URL/api/v1/account/log-in?email={email}&password={password}&token={token}
     *
     * @param email - email of user attempting to log in
     * @param password - password of user attempting to log in
     * @param token - token from user attempting to log in
     * @param response - Does not need to be specified, will be returned automatically, contains cookie
     * @return - If all parameters are valid, an HTTP response w/ status 200 OK containing a success message.
     *           If the email or password parameters are invalid, an HTTP response w/ status 400 BAD REQUEST.
     *           If the token parameter is invalid, an HTTP response w/ status 401 UNAUTHORIZED.
     */
    @PostMapping(BASE_URL + "log-in")
    public ResponseEntity<?> login(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String token,
            HttpServletResponse response
    ) {
        try {
            //create cookie from token, sent back in the HttpServletResponse object
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
     * Type: POST
     * Path: /api/v1/account/log-out
     *
     * Takes in session cookie and, if valid, clears the user's session
     * and revokes cookie. If not a valid cookie or has already been revoked, returns error
     * 
     *
     * Example:
     *   curl -X POST URL/api/v1/account/log-out
     *        -H '{
     *              'Content-Type': 'application/json', 
     *              'Cookie': {sessionCookieId}
     *             }'
     *
     * @param sessionCookieId - user's session cookie ID
     * @return - If all parameters are valid, an HTTP response w/ status 200 OK containing a success message.
     *           If the session ID is invalid, an HTTP response w/ status 401 UNAUTHORIZED.
     */
    @PostMapping(BASE_URL + "log-out")
    public ResponseEntity<?> logout(
            @CookieValue(value = "session") String sessionCookieId
    ) {
        try {
            authenticator.clearSessionAndRevoke(sessionCookieId);
            return ResponseEntity.ok("Logout successful!");
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Failed to revoke session (invalid or already revoked)");
        }

    }

    /**
     * Type: PUT
     * Path: /api/v1/account/update
     * Body: new Account object.
     *
     * Takes in an account serialized object and an email address and updates
     * the account linked to the email address with every non-null field in the 
     * new Account object. Email must be linked to a valid acccount, and cookie must be valid
     * 
     * Example:
     *   curl -X POST URL/api/v1/account/update?token={token}
     *        -H '{
     *              'Content-Type':'application/json',
     *              'Cookie': {sessionCookieId}
     *            }'
     *        -d '{
     *              "username": "abcde",
     *              "password": "null",
     *              "email": "email@email.com",
     *              "type": "CUSTOMER",
     *                  ...
     *            }'
     *
     * @param email - email of account to be updated
     * @param content - Account object with updated fields 
     * @param sessionCookieId - cookie associated with account/session
     * @return - If all parameters are valid, an HTTP response w/ status 200 OK containing a success message.
     *           If the email or content parameters are invalid, an HTTP response w/ status 400 BAD REQUEST.
     *           If the session ID is invalid, an HTTP response w/ status 401 UNAUTHORIZED.
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
     * Type: GET
     * Path: /api/v1/account/get
     *
     * Takes in account email and session cookie; if both valid, returns account object
     * associated with that email. Sensitive information such as password and list
     * of transactions are excluded (set to null) from the returned Account object. 
     * 
     *
     * Example:
     *   curl -X POST URL/api/v1/account/get?email={email}
     *        -H '{
     *              'Content-Type': 'application/json', 
     *              'Cookie': {sessionCookieId}
     *             }'
     *
     * @param email - email of desired account
     * @param sessionCookieId - cookie associated with account/session
     * @return - If all parameters are valid, an HTTP response w/ status 200 OK containing the account requested.
     *           If the email parameter is invalid, an HTTP response w/ status 400 BAD REQUEST.
     *           If the session ID is invalid, an HTTP response w/ status 401 UNAUTHORIZED.
     */
    @GetMapping(BASE_URL + "get")
    public ResponseEntity<?> get(
            @RequestParam String email,
            @CookieValue(value = "session") String sessionCookieId
    ) {
        try {
            authenticator.validateSession(sessionCookieId);
            return ResponseEntity.ok(repository.get(email));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to validate session");
        } catch (Throwable t) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(t.getMessage());
        }
    }

    /**
     * Type: GET
     * Path: /api/v1/account/usernameExists
     *
     * Takes in username as parameter and checks if an account
     * with that username already exists. Returns a boolean indicating this. 
     *
     * Example:
     *   curl -X POST URL/api/v1/account/usernameExists?username={username}
     *
     * @param username - username we want to check 
     * @return - If all parameters are valid, an HTTP response w/ status 200 OK containing true if an account with
     *           that username exists, false if it doesn't.
     *           If an internal error occurs, an HTTP response w/ status 500 INTERNAL SERVER ERROR.
     *           If the session ID is invalid, an HTTP response w/ status 401 UNAUTHORIZED.
     */
    @GetMapping(BASE_URL + "usernameExists")
    public ResponseEntity<?> usernameExists(
            @RequestParam String username,
            @CookieValue(value = "session") String sessionCookieId
    ) {
        try {
            authenticator.validateSession(sessionCookieId);
            return ResponseEntity.ok(repository.usernameExists(username));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to validate session");
        } catch (Throwable t) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(t.getMessage());
        }
    }
}
