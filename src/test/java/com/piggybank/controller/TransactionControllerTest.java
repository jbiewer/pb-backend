package com.piggybank.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuthException;
import com.piggybank.components.SessionAuthenticator;
import com.piggybank.model.Account;
import com.piggybank.model.BankAccount;
import com.piggybank.model.Transaction;
import com.piggybank.repository.BankAccountRepository;
import com.piggybank.repository.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.servlet.http.Cookie;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static com.piggybank.mocks.MockModels.*;
import static com.piggybank.util.FirebaseEmulatorServices.*;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit-testing suite for all endpoints in the TransactionController class.
 * Runs a spring application and uses a mock MVC provided by Spring to make mock HTTP requests to the controller
 * endpoints. Mockito is used to inject mock objects to be used in the application context.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class TransactionControllerTest {
    private static final String CUSTOMER_EMAIL = "user1@email.com";
    private static final String MERCHANT_EMAIL = "user2@email.com";

    private static final String VALID_TRANSACTION_ID = "tx-id0";
    private static final String INVALID_TRANSACTION_ID = "invalid-txn-id";
    private static final Transaction VALID_TRANSACTION = new ObjectMapper().convertValue(
            new HashMap<>() {{ put("id", VALID_TRANSACTION_ID); }},
            Transaction.class
    );

    private static final String VALID_SESSION_ID = UUID.randomUUID().toString();
    private static final String INVALID_SESSION_ID = UUID.randomUUID().toString();
    private static final Cookie VALID_SESSION_COOKIE = new Cookie("session", VALID_SESSION_ID);
    private static final Cookie INVALID_SESSION_COOKIE = new Cookie("session", INVALID_SESSION_ID);

    @MockBean private TransactionRepository repository;
    @MockBean private SessionAuthenticator authenticator;

    @Autowired private MockMvc mvc;

    /**
     * Given an object, serializes it into a JSON string.
     *
     * @param object Object to serialize.
     * @return JSON string formatted version of the object.
     * @throws JsonProcessingException When an exception occurs trying to serialize the object.
     */
    private static String jsonOf(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }

    /**
     * Load Firestore with fake data before each test.
     */
    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException, ExecutionException, InterruptedException {
        URI uri = requireNonNull(ClassLoader.getSystemResource("collections")).toURI();
        loadFirestoreDocuments(new File(uri));
    }

    /**
     * Clear Firestore of fake data after each test.
     */
    @AfterEach
    public void afterEach() throws IOException, InterruptedException {
        clearFirestoreDocuments();
    }

    /**
     * The test() endpoint should succeed w/ HTTP status 200 OK w/out a message (message is null).
     */
    @Test
    public void testSucceedsWithoutMessage() throws FirebaseAuthException {
        // Mock
        when(repository.test(isNull())).thenReturn("Success! No message supplied");

        // Test
        try {
            mvc.perform(get("/api/v1/transaction/test"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Success! No message supplied"));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, times(1)).test(isNull());
        verify(authenticator, never()).validateSession(any());
    }

    /**
     * The test() endpoint should succeed w/ HTTP status 200 OK with a message specified.
     */
    @Test
    public void testSucceedsWithMessage() throws FirebaseAuthException {
        // Mock
        when(repository.test("test")).thenReturn("Success! Here is your message: test");

        // Test
        try {
            mvc.perform(get("/api/v1/transaction/test").content("test"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Success! Here is your message: test"));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, times(1)).test("test");
        verify(authenticator, never()).validateSession(any());
    }

    /**
     * The test() endpoint should succeed w/ HTTP status 200 OK with a valid session ID (message is null).
     */
    @Test
    public void testSucceedsWithValidSession() throws FirebaseAuthException {
        // Mock
        when(repository.test(isNull())).thenReturn("Success! No message supplied");

        // Test
        try {
            mvc.perform(get("/api/v1/transaction/test").cookie(VALID_SESSION_COOKIE))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Success! No message supplied"));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, times(1)).test(isNull());
        verify(authenticator, times(1)).validateSession(VALID_SESSION_ID);
    }

    /**
     * The test() endpoint should fail with HTTP status 401 UNAUTHORIZED when an invalid session ID is
     * passed in.
     */
    @Test
    public void testFailsWithInvalidSession() throws FirebaseAuthException {
        // Mock
        doThrow(FirebaseAuthException.class).when(authenticator).validateSession(INVALID_SESSION_ID);

        // Test
        try {
            mvc.perform(get("/api/v1/transaction/test").cookie(INVALID_SESSION_COOKIE))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Failed to validate session"));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, never()).test(any());
        verify(authenticator, times(1)).validateSession(INVALID_SESSION_ID);
    }

    /**
     * The requestBankTransaction() endpoint should succeed w/ HTTP status 200 OK using a valid session ID.
     */
    @Test
    public void requestBankTransactionSucceeds() throws Exception {
        Transaction txn = mockBankTransaction(CUSTOMER_EMAIL);

        // Mock
        doNothing().when(authenticator).validateSession(VALID_SESSION_ID);
        when(repository.processBankTxn(txn)).thenReturn("Transaction successful!");

        // Test
        MockHttpServletRequestBuilder request = post("/api/v1/transaction/bank")
                .cookie(VALID_SESSION_COOKIE)
                .contentType(MediaType.APPLICATION_JSON);
        try {
            mvc.perform(request.content(jsonOf(txn)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Transaction successful!"));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, times(1)).processBankTxn(txn);
        verify(authenticator, times(1)).validateSession(VALID_SESSION_ID);
    }

    /**
     * The requestBankTransaction() endpoint should fail w/ HTTP status 401 UNAUTHORIZED because the session ID is invalid.
     */
    @Test
    public void requestBankTransactionFailsInvalidSession() throws Exception {
        Transaction txn = mockBankTransaction(CUSTOMER_EMAIL);

        // Mock
        doThrow(FirebaseAuthException.class).when(authenticator).validateSession(INVALID_SESSION_ID);

        // Test
        MockHttpServletRequestBuilder request = post("/api/v1/transaction/bank")
                .cookie(INVALID_SESSION_COOKIE)
                .contentType(MediaType.APPLICATION_JSON);
        try {
            mvc.perform(request.content(jsonOf(txn)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Failed to validate session"));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, never()).processBankTxn(any());
        verify(authenticator, times(1)).validateSession(INVALID_SESSION_ID);
    }

    /**
     * The requestBankTransaction() endpoint should fail w/ HTTP status 400 BAD REQUEST because the email is invalid (not found).
     */
    @Test
    public void requestBankTransactionFailsEmailNotFound() throws Exception {
        String invalidEmail = "not-a-valid-email";
        Transaction txn = mockBankTransaction(invalidEmail);

        // Mock
        doThrow(FirebaseAuthException.class).when(authenticator).validateSession(INVALID_SESSION_ID);
        when(repository.processBankTxn(txn)).thenThrow(IllegalArgumentException.class);

        // Test
        MockHttpServletRequestBuilder request = post("/api/v1/transaction/bank")
                .cookie(VALID_SESSION_COOKIE)
                .contentType(MediaType.APPLICATION_JSON);
        try {
            mvc.perform(request.content(jsonOf(txn))).andExpect(status().isBadRequest());
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, times(1)).processBankTxn(txn);
        verify(authenticator, times(1)).validateSession(VALID_SESSION_ID);
    }

    /**
     * The requestPeerTransaction() endpoint should succeed w/ HTTP status 200 OK using a valid session ID.
     */
    @Test
    public void requestPeerTransactionSucceeds() throws Exception {
        Transaction txn = mockPeerTransaction(MERCHANT_EMAIL, CUSTOMER_EMAIL);

        // Mock
        doNothing().when(authenticator).validateSession(VALID_SESSION_ID);
        when(repository.processPeerTxn(txn)).thenReturn("Transaction successful!");

        // Test
        MockHttpServletRequestBuilder request = post("/api/v1/transaction/peer")
                .cookie(VALID_SESSION_COOKIE)
                .contentType(MediaType.APPLICATION_JSON);
        try {
            mvc.perform(request.content(jsonOf(txn)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Transaction successful!"));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, times(1)).processPeerTxn(txn);
        verify(authenticator, times(1)).validateSession(VALID_SESSION_ID);
    }

    /**
     * The requestPeerTransaction() endpoint should fail w/ HTTP status 401 UNAUTHORIZED because the session ID is invalid.
     */
    @Test
    public void requestPeerTransactionFailsInvalidSession() throws Exception {
        Transaction txn = mockPeerTransaction(MERCHANT_EMAIL, CUSTOMER_EMAIL);

        // Mock
        doThrow(FirebaseAuthException.class).when(authenticator).validateSession(INVALID_SESSION_ID);

        // Test
        MockHttpServletRequestBuilder request = post("/api/v1/transaction/peer")
                .cookie(INVALID_SESSION_COOKIE)
                .contentType(MediaType.APPLICATION_JSON);
        try {
            mvc.perform(request.content(jsonOf(txn)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Failed to validate session"));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, never()).processPeerTxn(any());
        verify(authenticator, times(1)).validateSession(INVALID_SESSION_ID);
    }

    /**
     * The requestBankTransaction() endpoint should fail w/ HTTP status 400 BAD REQUEST because the email is invalid (not found).
     */
    @Test
    public void requestPeerTransactionFailsEmailNotFound() throws Exception {
        String invalidEmail = "not-a-valid-email";
        Transaction txn = mockPeerTransaction(invalidEmail, CUSTOMER_EMAIL);

        // Mock
        doThrow(FirebaseAuthException.class).when(authenticator).validateSession(INVALID_SESSION_ID);
        when(repository.processPeerTxn(txn)).thenThrow(IllegalArgumentException.class);

        // Test
        MockHttpServletRequestBuilder request = post("/api/v1/transaction/peer")
                .cookie(VALID_SESSION_COOKIE)
                .contentType(MediaType.APPLICATION_JSON);
        try {
            mvc.perform(request.content(jsonOf(txn))).andExpect(status().isBadRequest());
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, times(1)).processPeerTxn(txn);
        verify(authenticator, times(1)).validateSession(VALID_SESSION_ID);
    }

    /**
     * The getSingleTransaction() endpoint should succeed w/ HTTP status 200 OK using a valid session ID.
     */
    @Test
    public void getSingleTransactionSucceeds() throws Exception {
        // Mock
        doNothing().when(authenticator).validateSession(VALID_SESSION_ID);
        when(repository.getTxn(VALID_TRANSACTION_ID)).thenReturn(VALID_TRANSACTION);

        // Test
        MockHttpServletRequestBuilder request = get("/api/v1/transaction/getSingleTransaction")
                .param("txnId", VALID_TRANSACTION_ID)
                .cookie(VALID_SESSION_COOKIE);
        try {
            mvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(content().json(jsonOf(VALID_TRANSACTION)));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, times(1)).getTxn(VALID_TRANSACTION_ID);
        verify(authenticator, times(1)).validateSession(VALID_SESSION_ID);
    }

    /**
     * The getSingleTransaction() endpoint should fail w/ HTTP status 401 UNAUTHORIZED because the session ID is invalid.
     */
    @Test
    public void getSingleTransactionFailsInvalidSession() throws Exception {
        // Mock
        doThrow(FirebaseAuthException.class).when(authenticator).validateSession(INVALID_SESSION_ID);

        // Test
        MockHttpServletRequestBuilder request = get("/api/v1/transaction/getSingleTransaction")
                .param("txnId", VALID_TRANSACTION_ID)
                .cookie(INVALID_SESSION_COOKIE);
        try {
            mvc.perform(request)
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Failed to validate session"));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, never()).getTxn(any());
        verify(authenticator, times(1)).validateSession(INVALID_SESSION_ID);
    }

    /**
     * The getSingleTransaction() endpoint should fail w/ HTTP status 400 BAD REQUEST because the email is invalid (not found).
     */
    @Test
    public void getSingleTransactionFailsTransactionNotFound() throws Exception {
        // Mock
        doThrow(FirebaseAuthException.class).when(authenticator).validateSession(INVALID_SESSION_ID);
        when(repository.getTxn(INVALID_TRANSACTION_ID)).thenThrow(IllegalArgumentException.class);

        // Test
        MockHttpServletRequestBuilder request = get("/api/v1/transaction/getSingleTransaction")
                .param("txnId", INVALID_TRANSACTION_ID)
                .cookie(VALID_SESSION_COOKIE);
        try {
            mvc.perform(request).andExpect(status().isBadRequest());
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, times(1)).getTxn(INVALID_TRANSACTION_ID);
        verify(authenticator, times(1)).validateSession(VALID_SESSION_ID);
    }

    /**
     * The getAllTransactionsFromUser() endpoint should succeed w/ HTTP status 200 OK using a valid session ID.
     */
    @Test
    public void getAllTransactionsFromUserSucceeds() throws Exception {
        // Mock
        doNothing().when(authenticator).validateSession(VALID_SESSION_ID);
        when(repository.getAllTxnFromUser(CUSTOMER_EMAIL)).thenReturn(new ArrayList<>());

        // Test
        MockHttpServletRequestBuilder request = get("/api/v1/transaction/getAllFromUser")
                .param("email", CUSTOMER_EMAIL)
                .cookie(VALID_SESSION_COOKIE);
        try {
            mvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(content().json(jsonOf(new ArrayList<>())));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, times(1)).getAllTxnFromUser(CUSTOMER_EMAIL);
        verify(authenticator, times(1)).validateSession(VALID_SESSION_ID);
    }

    /**
     * The getAllTransactionsFromUser() endpoint should fail w/ HTTP status 401 UNAUTHORIZED because the session ID is invalid.
     */
    @Test
    public void getAllTransactionsFromUserFailsInvalidSession() throws Exception {
        // Mock
        doThrow(FirebaseAuthException.class).when(authenticator).validateSession(INVALID_SESSION_ID);

        // Test
        MockHttpServletRequestBuilder request = get("/api/v1/transaction/getAllFromUser")
                .param("email", CUSTOMER_EMAIL)
                .cookie(INVALID_SESSION_COOKIE);
        try {
            mvc.perform(request)
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Failed to validate session"));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, never()).getAllTxnFromUser(any());
        verify(authenticator, times(1)).validateSession(INVALID_SESSION_ID);
    }

    /**
     * The getAllTransactionsFromUser() endpoint should fail w/ HTTP status 400 BAD REQUEST because the email is invalid
     * (not found).
     */
    @Test
    public void getAllTransactionsFromUserFailsTransactionNotFound() throws Exception {
        // Mock
        doThrow(FirebaseAuthException.class).when(authenticator).validateSession(INVALID_SESSION_ID);
        when(repository.getAllTxnFromUser(CUSTOMER_EMAIL)).thenThrow(IllegalArgumentException.class);

        // Test
        MockHttpServletRequestBuilder request = get("/api/v1/transaction/getAllFromUser")
                .param("email", CUSTOMER_EMAIL)
                .cookie(VALID_SESSION_COOKIE);
        try {
            mvc.perform(request).andExpect(status().isBadRequest());
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, times(1)).getAllTxnFromUser(CUSTOMER_EMAIL);
        verify(authenticator, times(1)).validateSession(VALID_SESSION_ID);
    }
}