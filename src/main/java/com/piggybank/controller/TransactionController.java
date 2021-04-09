package com.piggybank.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.piggybank.PiggyBankApplication;
import com.piggybank.model.Transaction;
import com.piggybank.repository.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * Type: POST
     * Path: /api/v1/transaction/bank
     * Body: Bank transaction.
     *
     * Given a bank transaction, processes it by transferring the amount specified
     *
     * Example:
     *   curl -X POST URL/api/v1/transaction/bank
     *        -H '{
     *              'Content-Type: application/json',
     *              'Cookie: {sessionCookieId}'
     *            }'
     *        -d '{
     *              "transactorEmail": "user1@email.com",
     *              "amount": 12345,     // $123.45
     *              "type": "BANK"
     *            }'
     *
     * @param bankTxn Transaction representing an account transferring funds to their bank.
     * @param sessionCookieId - cookie associated with account/session
     * @return - If all parameters are valid, an HTTP response w/ status 200 OK containing a success message.
     *           If the 'bankTxn' is invalid, an HTTP response w/ status 400 BAD REQUEST.
     *           If the session ID is invalid, an HTTP response w/ status 401 UNAUTHORIZED.
     */
    @PostMapping(BASE_URL + "bank")
    public ResponseEntity<?> requestBankTransaction(
        @RequestBody Transaction bankTxn, 
        @CookieValue(value = "session") String sessionCookieId
    ) {
        try {
            authenticator.validateSession(sessionCookieId);
            return ResponseEntity.ok(repository.processBankTxn(bankTxn));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to validate session");
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Type: POST
     * Path: /api/v1/transaction/peer
     * Body: Peer transaction.
     *
     * Given a peer transaction, processes it by transferring the amount specified in the account represented
     * by 'transactorEmail' to the account represented by 'recipientEmail'.
     *
     * Example:
     *   curl -X POST URL/api/v1/transaction/peer
     *        -H '{
     *              'Content-Type: application/json',
     *              'Cookie: {sessionCookieId}'
     *            }'
     *        -d '{
     *              "transactorEmail": "user1@email.com",
     *              "recipientEmail": "user2@email.com",
     *              "amount": 12345,     // $123.45
     *              "type": "PEER"
     *            }'
     *
     * @param peerTxn Transaction representing an account transferring funds to another account.
     * @param sessionCookieId - cookie associated with account/session
     * @return - If all parameters are valid, an HTTP response w/ status 200 OK containing a success message.
     *           If the 'peerTxn' is invalid, an HTTP response w/ status 400 BAD REQUEST.
     *           If the session ID is invalid, an HTTP response w/ status 401 UNAUTHORIZED.
     */
    @PostMapping(BASE_URL + "peer")
    public ResponseEntity<?> requestPeerTransaction(
        @RequestBody Transaction peerTxn,
        @CookieValue(value = "session") String sessionCookieId
    ) {
        try  {
            authenticator.validateSession(sessionCookieId);
            return ResponseEntity.ok(repository.processPeerTxn(peerTxn));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to validate session");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        
    }

    /**
     * Type: GET
     * Path: /api/v1/transaction/getSingleTransaction
     * Param: txnId -- ID of the transaction to retrieve.
     *
     * Given a transaction ID, retrieves the transaction w/ that ID from Firestore.
     *
     * Example:
     *   curl -X GET URL/api/v1/transaction/getSingleTransaction?txnId={transactionId}
     *        -H 'Cookie: {sessionCookieId}'
     *
     * @param txnId ID of the transaction to retrieve.
     * @param sessionCookieId - cookie associated with account/session
     * @return - If all parameters are valid, an HTTP response w/ status 200 OK containing a success message.
     *           If the 'txnId' is invalid, an HTTP response w/ status 400 BAD REQUEST.
     *           If the session ID is invalid, an HTTP response w/ status 401 UNAUTHORIZED.
     */
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

    /**
     * Type: GET
     * Path: /api/v1/transaction/getAllFromUser
     * Param: email -- Email of the account to retrieve all transactions from.
     *
     * Given an email, retrieves all transactions that are owned by the account represented by that
     * email. An account "owns" a transaction if the transaction ID is in the account's transactionIds
     * list.
     *
     * Example:
     *   curl -X GET URL/api/v1/transaction/getAllFromUser?email=user1@email.com
     *        -H 'Cookie: {sessionCookieId}'
     *
     * @param email Email of the account to retrieve all transactions from.
     * @param sessionCookieId - cookie associated with account/session
     * @return - If all parameters are valid, an HTTP response w/ status 200 OK containing a success message.
     *           If the email is invalid, an HTTP response w/ status 400 BAD REQUEST.
     *           If the session ID is invalid, an HTTP response w/ status 401 UNAUTHORIZED.
     */
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