package com.piggybank.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuthException;
import com.piggybank.components.SessionAuthenticator;
import com.piggybank.model.Account;
import com.piggybank.repository.AccountRepository;
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
import static com.piggybank.model.Account.AccountType;
import static com.piggybank.util.FirebaseEmulatorServices.*;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerTest {
    private static final String CUSTOMER_EMAIL = "user1@email.com";
    private static final String CUSTOMER_USERNAME = "user1";
    private static final String CUSTOMER_PASSWORD = "user1-pw";

    private static final String VALID_SESSION_ID = UUID.randomUUID().toString();
    private static final String INVALID_SESSION_ID = UUID.randomUUID().toString();
    private static final String VALID_TOKEN_ID = UUID.randomUUID().toString();
    private static final String INVALID_TOKEN_ID = UUID.randomUUID().toString();
    private static final String EXPIRED_TOKEN_ID = UUID.randomUUID().toString();
    private static final Cookie VALID_SESSION_COOKIE = new Cookie("session", VALID_SESSION_ID);
    private static final Cookie INVALID_SESSION_COOKIE = new Cookie("session", INVALID_SESSION_ID);

    @MockBean private AccountRepository repository;
    @MockBean private SessionAuthenticator authenticator;

    @Autowired private MockMvc mvc;

    public String jsonOf(Object object) throws JsonProcessingException {
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
            mvc.perform(get("/api/v1/account/test"))
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
            mvc.perform(get("/api/v1/account/test").content("test"))
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
            mvc.perform(get("/api/v1/account/test").cookie(VALID_SESSION_COOKIE))
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
            mvc.perform(get("/api/v1/account/test").cookie(INVALID_SESSION_COOKIE))
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
     * The create() endpoint should succeed w/ HTTP status 200 OK using a valid token ID.
     */
    @Test
    public void createSucceeds() throws Exception {
        Account account = mockAccount(AccountType.CUSTOMER);

        // Mock
        doReturn(VALID_SESSION_COOKIE).when(authenticator).generateNewSession(VALID_TOKEN_ID);
        when(repository.create(account)).thenReturn("Account created successfully!");

        // Test
        MockHttpServletRequestBuilder request = post("/api/v1/account/create")
                .param("token", VALID_TOKEN_ID)
                .contentType(MediaType.APPLICATION_JSON);
        try {
            mvc.perform(request.content(jsonOf(account)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Account created successfully!"));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, times(1)).create(account);
        verify(authenticator, times(1)).generateNewSession(VALID_TOKEN_ID);
    }

    /**
     * The create() endpoint should fail w/ HTTP status 401 UNAUTHORIZED because of an invalid token ID.
     */
    @Test
    public void createFailsInvalidToken() throws Exception {
        Account account = getFromFirestore("Accounts", CUSTOMER_EMAIL, Account.class);

        // Mock
        doThrow(FirebaseAuthException.class).when(authenticator).generateNewSession(INVALID_TOKEN_ID);

        // Test
        MockHttpServletRequestBuilder request = post("/api/v1/account/create")
                .param("token", INVALID_TOKEN_ID)
                .contentType(MediaType.APPLICATION_JSON);
        try {
            mvc.perform(request.content(jsonOf(account)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Failed to create a session"));
        } catch (Exception t) {
            fail(t);
        }

        // Verify
        verify(repository, never()).create(any());
        verify(authenticator, times(1)).generateNewSession(INVALID_TOKEN_ID);
    }

    /**
     * The create() endpoint should fail w/ HTTP status 401 UNAUTHORIZED because the token ID is expired.
     */
    @Test
    public void createFailsExpiredToken() throws Exception {
        Account account = getFromFirestore("Accounts", CUSTOMER_EMAIL, Account.class);

        // Mock
        doThrow(AuthException.class).when(authenticator).generateNewSession(EXPIRED_TOKEN_ID);

        // Test
        MockHttpServletRequestBuilder request = post("/api/v1/account/create")
                .param("token", EXPIRED_TOKEN_ID)
                .contentType(MediaType.APPLICATION_JSON);
        try {
            mvc.perform(request.content(jsonOf(account)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Recent sign in required"));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, never()).create(any());
        verify(authenticator, times(1)).generateNewSession(EXPIRED_TOKEN_ID);
    }

    /**
     * The create() endpoint should fail w/ HTTP status 400 BAD REQUEST because the type field is missing in the
     * account parameter passed in.
     */
    @Test
    public void createFailsMissingField() throws Exception {
        Account account = getFromFirestore("Accounts", "user2@email.com", Account.class);
        account.setType(null);

        // Mock
        doReturn(VALID_SESSION_COOKIE).when(authenticator).generateNewSession(VALID_TOKEN_ID);
        when(repository.create(argThat(acct -> acct.getType() == null))).thenThrow(IllegalArgumentException.class);

        // Test
        MockHttpServletRequestBuilder request = post("/api/v1/account/create")
                .param("token", VALID_TOKEN_ID)
                .contentType(MediaType.APPLICATION_JSON);
        try {
            mvc.perform(request.content(jsonOf(account))).andExpect(status().isBadRequest());
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, times(1)).create(account);
        verify(authenticator, times(1)).generateNewSession(VALID_TOKEN_ID);
    }

    /**
     * The login() endpoint should succeed w/ HTTP status 200 OK using a valid token ID.
     */
    @Test
    public void loginSucceeds() throws Exception {
        // Mock
        doReturn(VALID_SESSION_COOKIE).when(authenticator).generateNewSession(VALID_TOKEN_ID);
        when(repository.login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD)).thenReturn("Login successful!");

        // Test
        MockHttpServletRequestBuilder request = post("/api/v1/account/log-in")
                .param("email", CUSTOMER_EMAIL)
                .param("password", CUSTOMER_PASSWORD)
                .param("token", VALID_TOKEN_ID);
        try {
            mvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(content().string("Login successful!"));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, times(1)).login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        verify(authenticator, times(1)).generateNewSession(VALID_TOKEN_ID);
    }

    /**
     * The login() endpoint should fail w/ HTTP status 401 UNAUTHORIZED because the token ID is invalid.
     */
    @Test
    public void loginFailsInvalidToken() throws Exception {
        // Mock
        doThrow(FirebaseAuthException.class).when(authenticator).generateNewSession(INVALID_TOKEN_ID);

        // Test
        MockHttpServletRequestBuilder request = post("/api/v1/account/log-in")
                .param("email", CUSTOMER_EMAIL)
                .param("password", CUSTOMER_PASSWORD)
                .param("token", INVALID_TOKEN_ID);
        try {
            mvc.perform(request)
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Failed to create a session"));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, never()).login(any(), any());
        verify(authenticator, times(1)).generateNewSession(INVALID_TOKEN_ID);
    }

    /**
     * The login() endpoint should fail w/ HTTP status 401 UNAUTHORIZED because the token ID is expired.
     */
    @Test
    public void loginFailsExpiredToken() throws Exception {
        // Mock
        doThrow(AuthException.class).when(authenticator).generateNewSession(EXPIRED_TOKEN_ID);

        // Test
        MockHttpServletRequestBuilder request = post("/api/v1/account/log-in")
                .param("email", CUSTOMER_EMAIL)
                .param("password", CUSTOMER_PASSWORD)
                .param("token", EXPIRED_TOKEN_ID);
        try {
            mvc.perform(request)
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Recent sign in required"));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, never()).login(any(), any());
        verify(authenticator, times(1)).generateNewSession(EXPIRED_TOKEN_ID);
    }

    /**
     * The login() endpoint should fail w/ HTTP status 400 BAD REQUEST because the email/password credentials are
     * invalid (not found).
     */
    @Test
    public void loginFailsInvalidCredentials() throws Exception {
        String fakeEmail = "non-existent-email";
        String fakePassword = "non-existent-password";

        // Mock
        doReturn(VALID_SESSION_COOKIE).when(authenticator).generateNewSession(VALID_TOKEN_ID);
        when(repository.login(fakeEmail, fakePassword))
                .thenThrow(IllegalArgumentException.class);

        // Test
        MockHttpServletRequestBuilder request = post("/api/v1/account/log-in")
                .param("email", fakeEmail)
                .param("password", fakePassword)
                .param("token", VALID_TOKEN_ID);
        try {
            mvc.perform(request).andExpect(status().isBadRequest());
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, times(1)).login(fakeEmail, fakePassword);
        verify(authenticator, times(1)).generateNewSession(VALID_TOKEN_ID);
    }

    /**
     * The logout() endpoint should succeed w/ HTTP status 200 OK using a valid session ID.
     */
    @Test
    public void logoutSucceeds() throws FirebaseAuthException {
        // Test
        MockHttpServletRequestBuilder request = post("/api/v1/account/log-out")
                .cookie(VALID_SESSION_COOKIE);
        try {
            mvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(content().string("Logout successful!"));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(authenticator, times(1)).clearSessionAndRevoke(VALID_SESSION_ID);
    }

    /**
     * The logout() endpoint should fail w/ HTTP status 401 UNAUTHORIZED because the session ID is invalid.
     */
    @Test
    public void logoutFailsInvalidSession() throws FirebaseAuthException {
        // Mock
        doThrow(FirebaseAuthException.class).when(authenticator).clearSessionAndRevoke(INVALID_SESSION_ID);

        // Test
        try {
            mvc.perform(post("/api/v1/account/log-out").cookie(INVALID_SESSION_COOKIE))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Failed to revoke session (invalid or already revoked)"));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(authenticator, times(1)).clearSessionAndRevoke(INVALID_SESSION_ID);
    }

    /**
     * The update() endpoint should succeed w/ HTTP status 200 OK using a valid session ID and a fake account.
     */
    @Test
    public void updateSucceeds() throws Exception {
        Account account = mockAccount(AccountType.CUSTOMER);
        account.setEmail(CUSTOMER_EMAIL);

        // Mock
        doNothing().when(authenticator).validateSession(VALID_SESSION_ID);
        when(repository.update(CUSTOMER_EMAIL, account)).thenReturn("Account successfully updated!");

        // Test
        MockHttpServletRequestBuilder request = put("/api/v1/account/update")
                .param("email", CUSTOMER_EMAIL)
                .cookie(VALID_SESSION_COOKIE)
                .contentType(MediaType.APPLICATION_JSON);
        try {
            mvc.perform(request.content(jsonOf(account)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Account successfully updated!"));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, times(1)).update(CUSTOMER_EMAIL, account);
        verify(authenticator, times(1)).validateSession(VALID_SESSION_ID);
    }

    /**
     * The update() endpoint should fail w/ HTTP status 401 UNAUTHORIZED because the session ID is invalid.
     */
    @Test
    public void updateFailsInvalidSession() throws Exception {
        Account account = mockAccount(AccountType.CUSTOMER);
        account.setEmail(CUSTOMER_EMAIL);

        // Mock
        doThrow(FirebaseAuthException.class).when(authenticator).validateSession(INVALID_SESSION_ID);

        // Test
        MockHttpServletRequestBuilder request = put("/api/v1/account/update")
                .param("email", CUSTOMER_EMAIL)
                .cookie(INVALID_SESSION_COOKIE)
                .contentType(MediaType.APPLICATION_JSON);
        try {
            mvc.perform(request.content(jsonOf(account)))
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
        Account account = mockAccount(AccountType.CUSTOMER);
        account.setEmail(invalidEmail);

        // Mock
        when(repository.update(invalidEmail, account)).thenThrow(IllegalArgumentException.class);

        // Test
        MockHttpServletRequestBuilder request = put("/api/v1/account/update")
                .param("email", invalidEmail)
                .cookie(VALID_SESSION_COOKIE)
                .contentType(MediaType.APPLICATION_JSON);
        try {
            mvc.perform(request.content(jsonOf(account))).andExpect(status().isBadRequest());
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, times(1)).update(invalidEmail, account);
        verify(authenticator, times(1)).validateSession(VALID_SESSION_ID);
    }

    /**
     * The get() endpoint should succeed w/ HTTP status 200 OK using a valid session ID.
     */
    @Test
    public void getSucceeds() throws Exception {
        Account account = getFromFirestore("Accounts", CUSTOMER_EMAIL, Account.class);
        Account.filterSensitiveData(account);

        // Mock
        when(repository.get(CUSTOMER_EMAIL)).thenReturn(account);

        // Test
        MockHttpServletRequestBuilder request = get("/api/v1/account/get")
                .param("email", CUSTOMER_EMAIL)
                .cookie(VALID_SESSION_COOKIE);
        try {
            mvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(content().string(jsonOf(account)));
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
        MockHttpServletRequestBuilder request = get("/api/v1/account/get")
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
        MockHttpServletRequestBuilder request = get("/api/v1/account/get")
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

    /**
     * The usernameExists() endpoint should succeed w/ HTTP status 200 OK using a valid session ID.
     */
    @Test
    public void usernameExistsSucceeds() throws Exception {
        // Mock
        when(repository.usernameExists(CUSTOMER_USERNAME)).thenReturn(true);

        // Test
        MockHttpServletRequestBuilder request = get("/api/v1/account/usernameExists")
                .param("username", CUSTOMER_USERNAME)
                .cookie(VALID_SESSION_COOKIE);
        try {
            mvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, times(1)).usernameExists(CUSTOMER_USERNAME);
        verify(authenticator, times(1)).validateSession(VALID_SESSION_ID);
    }

    /**
     * The usernameExists() endpoint should fail w/ HTTP status 401 UNAUTHORIZED because the session ID is invalid.
     */
    @Test
    public void usernameExistsFailsInvalidSession() throws Exception {
        // Mock
        doThrow(FirebaseAuthException.class).when(authenticator).validateSession(INVALID_SESSION_ID);

        // Test
        MockHttpServletRequestBuilder request = get("/api/v1/account/usernameExists")
                .param("username", CUSTOMER_USERNAME)
                .cookie(INVALID_SESSION_COOKIE);
        try {
            mvc.perform(request)
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Failed to validate session"));
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, never()).usernameExists(any());
        verify(authenticator, times(1)).validateSession(INVALID_SESSION_ID);
    }

    /**
     * The usernameExists() endpoint should fail w/ HTTP status 500 INTERNAL SERVER ERROR because of an internal
     * issue that occurred.
     */
    @Test
    public void usernameExistsFailsInternalError() throws Exception {
        // Mock
        when(repository.usernameExists(CUSTOMER_USERNAME)).thenThrow(Exception.class);

        // Test
        MockHttpServletRequestBuilder request = get("/api/v1/account/usernameExists")
                .param("username", CUSTOMER_USERNAME)
                .cookie(VALID_SESSION_COOKIE);
        try {
            mvc.perform(request).andExpect(status().isInternalServerError());
        } catch (Exception e) {
            fail(e);
        }

        // Verify
        verify(repository, times(1)).usernameExists(CUSTOMER_USERNAME);
        verify(authenticator, times(1)).validateSession(VALID_SESSION_ID);
    }
}