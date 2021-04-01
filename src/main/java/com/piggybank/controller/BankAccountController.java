package com.piggybank.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.piggybank.PiggyBankApplication;
import com.piggybank.model.Account;
import com.piggybank.model.BankAccount;
import com.piggybank.repository.BankAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.message.AuthException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Bank account-related application interface.
 * Base URL: /api/v1/bank/
 */
@RestController
public class BankAccountController extends PBController<BankAccountRepository> {
    private static final String BASE_URL = PiggyBankApplication.BASE_URL + "bank/";

    /**
     * Test mapping.
     * Used to see if the bank account endpoints are reachable.
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
     * Type: PUT
     * Path: /api/v1/bank/update
     * Params:  email - Email of the account to update.
     * Body: BankAccount model
     *
     * Takes in a bank account serialized object and updates the corresponding account's bank account (based on
     * the email parameter) using the data. If an account with the email is not found, an error will be returned.
     *
     * Example:
     *   curl -X PUT URL/api/v1/bank/update?email=user@email.com
     *        -H 'Content-Type:application/json'
     *        -d '{
     *              "accountNumber": 1234,
     *              "routingNumber": 5678,
     *              "nameOnAccount": "A Random Name"
     *            }'
     *
     * @param email Email of an existing account of whom the bank account is owned by.
     * @param content Account object containing initial fields. Type field is required
     * @param sessionCookieId Session cookie to validate the connection to the API.
     * @return - If all parameters are valid, an HTTP response w/ status 200 OK containing a success message.
     *           If the email parameter is invalid, an HTTP response w/ status 400 BAD REQUEST.
     *           If the session cookie is invalid, an HTTP response w/ status 401 UNAUTHORIZED.
     */
    @PutMapping(BASE_URL + "update")
    public ResponseEntity<?> update(
            @RequestParam String email,
            @RequestBody BankAccount content,
            @CookieValue(value = "session") String sessionCookieId
    ) {
        try {
            authenticator.validateSession(sessionCookieId);
            return ResponseEntity.ok(repository.update(email, content));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to validate session");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Type: DELETE
     * Path: /api/v1/bank/remove
     * Params:  email - Email of the account to update.
     *
     * Takes an email to find an account and then removes the bank account from that account.
     * If no account with that email is found, an error will be returned.
     *
     * Example:
     *   curl -X DELETE URL/api/v1/bank/remove?email=user@email.com
     *
     * @param email Email of an existing account of whom the bank account is owned by.
     * @param sessionCookieId Session cookie to validate the connection to the API.
     * @return - If all parameters are valid, an HTTP response w/ status 200 OK containing a success message.
     *           If the email parameter is invalid, an HTTP response w/ status 400 BAD REQUEST.
     *           If the session cookie is invalid, an HTTP response w/ status 401 UNAUTHORIZED.
     */
    @DeleteMapping(BASE_URL + "remove")
    public ResponseEntity<?> remove(
            @RequestParam String email,
            @CookieValue(value = "session") String sessionCookieId
    ) {
        try {
            authenticator.validateSession(sessionCookieId);
            return ResponseEntity.ok(repository.remove(email));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to validate session");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Type: GET
     * Path: /api/v1/bank/get
     * Params:  email - Email of the account to update.
     *
     * Takes an email to find an account and then returns the bank account from that account.
     * If no account with that email is found, an error will be returned.
     *
     * Example:
     *   curl -X GET URL/api/v1/bank/get?email=user@email.com
     *
     * @param email Email of an existing account of whom the bank account is owned by.
     * @param sessionCookieId Session cookie to validate the connection to the API.
     * @return - If all parameters are valid, an HTTP response w/ status 200 OK containing the bank account.
     *           If the email parameter is invalid, an HTTP response w/ status 400 BAD REQUEST.
     *           If the session cookie is invalid, an HTTP response w/ status 401 UNAUTHORIZED.
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
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
