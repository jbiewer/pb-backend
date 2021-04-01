package com.piggybank.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuthException;
import com.piggybank.components.SessionAuthenticator;
import com.piggybank.model.Account;
import com.piggybank.model.BankAccount;
import com.piggybank.repository.AccountRepository;
import com.piggybank.repository.BankAccountRepository;
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

import javax.security.auth.message.AuthException;
import javax.servlet.http.Cookie;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static com.piggybank.mocks.MockModels.mockAccount;
import static com.piggybank.mocks.MockModels.mockBankAccount;
import static com.piggybank.model.Account.AccountType;
import static com.piggybank.util.FirebaseEmulatorServices.*;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit-testing suite for all endpoints in the AccountController class.
 * Runs a spring application and uses a mock MVC provided by Spring to make mock HTTP requests to the controller
 * endpoints. Mockito is used to inject mock objects to be used in the application context.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class BankAccountControllerTest {
    private static final String CUSTOMER_EMAIL = "user1@email.com";
    private static final String CUSTOMER_USERNAME = "user1";
    private static final String CUSTOMER_PASSWORD = "user1-pw";

    private static final String VALID_SESSION_ID = UUID.randomUUID().toString();
    private static final String INVALID_SESSION_ID = UUID.randomUUID().toString();
    private static final Cookie VALID_SESSION_COOKIE = new Cookie("session", VALID_SESSION_ID);
    private static final Cookie INVALID_SESSION_COOKIE = new Cookie("session", INVALID_SESSION_ID);

    @MockBean private BankAccountRepository repository;
    @MockBean private SessionAuthenticator authenticator;

    @Autowired private MockMvc mvc;

    /**
     * Given an object, serializes it into a JSON string.
     *
     * @param object Object to serialize.
     * @return JSON string formatted version of the object.
     * @throws JsonProcessingException When an exception occurs trying to serialize the object.
     */
    private String jsonOf(Object object) throws JsonProcessingException {
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
            mvc.perform(get("/api/v1/bank/test"))
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
            mvc.perform(get("/api/v1/bank/test").content("test"))
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
            mvc.perform(get("/api/v1/bank/test").cookie(VALID_SESSION_COOKIE))
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
            mvc.perform(get("/api/v1/bank/test").cookie(INVALID_SESSION_COOKIE))
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
     * The update() endpoint should succeed w/ HTTP status 200 OK using a valid session ID and a fake bank account.
     */
    @Test
    public void updateSucceeds() throws Exception {
        BankAccount bank = mockBankAccount();

        // Mock
        doNothing().when(authenticator).validateSession(VALID_SESSION_ID);
        when(repository.update(CUSTOMER_EMAIL, bank)).thenReturn("Bank account successfully updated!");

        // Test
        MockHttpServletRequestBuilder request = put("/api/v1/bank/update")
                .param("email", CUSTOMER_EMAIL)
                .cookie(VALID_SESSION_COOKIE)
                .contentType(MediaType.APPLICATION_JSON);
        try {
            mvc.perform(request.content(jsonOf(bank)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Bank account successfully updated!"));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, times(1)).update(CUSTOMER_EMAIL, bank);
        verify(authenticator, times(1)).validateSession(VALID_SESSION_ID);
    }

    /**
     * The update() endpoint should fail w/ HTTP status 401 UNAUTHORIZED because the session ID is invalid.
     */
    @Test
    public void updateFailsInvalidSession() throws Exception {
        BankAccount bank = mockBankAccount();

        // Mock
        doThrow(FirebaseAuthException.class).when(authenticator).validateSession(INVALID_SESSION_ID);

        // Test
        MockHttpServletRequestBuilder request = put("/api/v1/bank/update")
                .param("email", CUSTOMER_EMAIL)
                .cookie(INVALID_SESSION_COOKIE)
                .contentType(MediaType.APPLICATION_JSON);
        try {
            mvc.perform(request.content(jsonOf(bank)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Failed to validate session"));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, never()).update(any(), any());
        verify(authenticator, times(1)).validateSession(INVALID_SESSION_ID);
    }

    /**
     * The update() endpoint should fail w/ HTTP status 400 BAD REQUEST because the email is invalid (not found).
     */
    @Test
    public void updateFailsEmailNotFound() throws Exception {
        String invalidEmail = "not-a-valid-email";
        BankAccount bank = mockBankAccount();

        // Mock
        when(repository.update(invalidEmail, bank)).thenThrow(IllegalArgumentException.class);

        // Test
        MockHttpServletRequestBuilder request = put("/api/v1/bank/update")
                .param("email", invalidEmail)
                .cookie(VALID_SESSION_COOKIE)
                .contentType(MediaType.APPLICATION_JSON);
        try {
            mvc.perform(request.content(jsonOf(bank))).andExpect(status().isBadRequest());
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, times(1)).update(invalidEmail, bank);
        verify(authenticator, times(1)).validateSession(VALID_SESSION_ID);
    }

    /**
     * The remove() endpoint should succeed w/ HTTP status 200 OK using a valid session ID.
     */
    @Test
    public void removeSucceeds() throws Exception {
        // Mock
        doNothing().when(authenticator).validateSession(VALID_SESSION_ID);
        when(repository.remove(CUSTOMER_EMAIL)).thenReturn("Bank account successfully removed!");

        // Test
        MockHttpServletRequestBuilder request = delete("/api/v1/bank/remove")
                .param("email", CUSTOMER_EMAIL)
                .cookie(VALID_SESSION_COOKIE);
        try {
            mvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(content().string("Bank account successfully removed!"));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, times(1)).remove(CUSTOMER_EMAIL);
        verify(authenticator, times(1)).validateSession(VALID_SESSION_ID);
    }

    /**
     * The remove() endpoint should fail w/ HTTP status 401 UNAUTHORIZED because the session ID is invalid.
     */
    @Test
    public void removeFailsInvalidSession() throws Exception {
        // Mock
        doThrow(FirebaseAuthException.class).when(authenticator).validateSession(INVALID_SESSION_ID);

        // Test
        MockHttpServletRequestBuilder request = delete("/api/v1/bank/remove")
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
        verify(repository, never()).update(any(), any());
        verify(authenticator, times(1)).validateSession(INVALID_SESSION_ID);
    }

    /**
     * The remove() endpoint should fail w/ HTTP status 400 BAD REQUEST because the email is invalid (not found).
     */
    @Test
    public void removeFailsEmailNotFound() throws Exception {
        String invalidEmail = "not-a-valid-email";

        // Mock
        when(repository.remove(invalidEmail)).thenThrow(IllegalArgumentException.class);

        // Test
        MockHttpServletRequestBuilder request = delete("/api/v1/bank/remove")
                .param("email", invalidEmail)
                .cookie(VALID_SESSION_COOKIE);
        try {
            mvc.perform(request).andExpect(status().isBadRequest());
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, times(1)).remove(invalidEmail);
        verify(authenticator, times(1)).validateSession(VALID_SESSION_ID);
    }

    /**
     * The get() endpoint should succeed w/ HTTP status 200 OK using a valid session ID.
     */
    @Test
    public void getSucceeds() throws Exception {
        BankAccount bank = getFromFirestore("Accounts", CUSTOMER_EMAIL, Account.class).getBankAccount();

        // Mock
        when(repository.get(CUSTOMER_EMAIL)).thenReturn(bank);

        // Test
        MockHttpServletRequestBuilder request = get("/api/v1/bank/get")
                .param("email", CUSTOMER_EMAIL)
                .cookie(VALID_SESSION_COOKIE);
        try {
            mvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(content().string(jsonOf(bank)));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, times(1)).get(CUSTOMER_EMAIL);
        verify(authenticator, times(1)).validateSession(VALID_SESSION_ID);
    }

    /**
     * The get() endpoint should fail w/ HTTP status 401 UNAUTHORIZED because the session ID is invalid.
     */
    @Test
    public void getFailsInvalidSession() throws Exception {
        // Mock
        doThrow(FirebaseAuthException.class).when(authenticator).validateSession(INVALID_SESSION_ID);

        // Test
        MockHttpServletRequestBuilder request = get("/api/v1/bank/get")
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
        verify(repository, never()).get(any());
        verify(authenticator, times(1)).validateSession(INVALID_SESSION_ID);
    }

    /**
     * The get() endpoint should fail w/ HTTP status 400 BAD REQUEST because the email is invalid (not found).
     */
    @Test
    public void getFailsEmailNotFound() throws Exception {
        String invalidEmail = "invalid-email";

        // Mock
        when(repository.get(invalidEmail)).thenThrow(IllegalArgumentException.class);

        // Test
        MockHttpServletRequestBuilder request = get("/api/v1/bank/get")
                .param("email", invalidEmail)
                .cookie(VALID_SESSION_COOKIE);
        try {
            mvc.perform(request).andExpect(status().isBadRequest());
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, times(1)).get(invalidEmail);
        verify(authenticator, times(1)).validateSession(VALID_SESSION_ID);
    }
}