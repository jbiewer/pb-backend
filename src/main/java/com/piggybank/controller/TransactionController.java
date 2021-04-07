package com.piggybank.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.piggybank.PiggyBankApplication;
import com.piggybank.model.Account;
import com.piggybank.model.BankAccount;
import com.piggybank.model.Transaction;
import com.piggybank.model.Account.AccountType;
import com.piggybank.model.Transaction.TransactionType;
import com.piggybank.repository.TransactionRepository;
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
public class TransactionController extends PBController<TransactionRepository> {
    private static final String BASE_URL = PiggyBankApplication.BASE_URL + "transaction/";

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

    @PostMapping(BASE_URL + "bank")
    public ResponseEntity<?> requestBankTransaction(
        @RequestBody Transaction bankTxn, 
        @CookieValue(value = "session") String sessionCookieId
    ) {
        try {
            authenticator.validateSession(sessionCookieId);
            return ResponseEntity.ok(repository.bankTxn(bankTxn));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to validate session");
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        
    }

    @PostMapping(BASE_URL + "peer")
    public ResponseEntity<?> requestPeerTransaction(
        @RequestBody Transaction bankTxn, 
        @CookieValue(value = "session") String sessionCookieId
    ) {
        try  {
            authenticator.validateSession(sessionCookieId);
            return ResponseEntity.ok(repository.peerTxn(bankTxn));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to validate session");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        
    }

    @GetMapping(BASE_URL + "getSingleTransaction")
    public ResponseEntity<?> getSingleTransaction(
        @RequestParam String txnId, 
        @CookieValue(value = "session") String sessionCookieId
    ) {
        try {
            authenticator.validateSession(sessionCookieId);
            return ResponseEntity.ok(repository.getTxn(txnId));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to validate session");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping(BASE_URL + "getAllFromUser")
    public ResponseEntity<?> getAllTransactionsFromUser(
        @RequestParam String email, 
        @CookieValue(value = "session") String sessionCookieId
    ) {
        try {
            authenticator.validateSession(sessionCookieId);
            return ResponseEntity.ok(repository.getAllTxnFromUser(email));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to validate session");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}