package com.piggybank.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.piggybank.components.SessionAuthenticator;
import com.piggybank.mocks.MockAccountRepository;
import com.piggybank.mocks.MockSessionAuthenticator;
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

import javax.servlet.http.Cookie;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import static com.piggybank.mocks.MockAccountRepository.*;
import static com.piggybank.mocks.MockModels.mockAccount;
import static com.piggybank.mocks.MockSessionAuthenticator.*;
import static com.piggybank.model.Account.AccountType;
import static com.piggybank.util.FirebaseEmulatorServices.*;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerTest {

    @MockBean
    private AccountRepository repository;
    @MockBean
    private SessionAuthenticator authenticator;

    @Autowired
    private MockMvc mvc;

    public String jsonOf(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }

    /**
     * todo
     */
    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException, ExecutionException, InterruptedException {
        URI uri = requireNonNull(ClassLoader.getSystemResource("collections")).toURI();
        generateFirestoreData(new File(uri));

        MockAccountRepository.reset(repository);
        MockSessionAuthenticator.reset(authenticator);
    }

    /**
     * todo
     */
    @AfterEach
    public void afterEach() throws IOException, InterruptedException {
        clearFirestoreDocuments();
    }

    /**
     * todo
     */
    @Test
    public void testSucceedsWithoutMessage() {
        try {
            mvc.perform(get("/api/v1/account/test"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Success! No message supplied"));
            verify(repository, times(1)).test(null);
            verify(authenticator, never()).validateSession(any());
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * todo
     */
    @Test
    public void testSucceedsWithMessage() {
        try {
            mvc.perform(get("/api/v1/account/test").content("test"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Success! Here is your message: test"));

            verify(repository, times(1)).test(anyString());
            verify(authenticator, never()).validateSession(any());
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * todo
     */
    @Test
    public void testSucceedsWithValidSession() {
        try {
            mvc.perform(get("/api/v1/account/test").cookie(new Cookie("session", VALID_SESSION_ID)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Success! No message supplied"));

            verify(repository, times(1)).test(null);
            verify(authenticator, times(1)).validateSession(VALID_SESSION_ID);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * todo
     */
    @Test
    public void testFailsWithInvalidSession() {
        String sessionCookieId = "not-valid";
        try {
            mvc.perform(get("/api/v1/account/test").cookie(new Cookie("session", sessionCookieId)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Failed to validate session"));

            verify(repository, never()).test(any());
            verify(authenticator, times(1)).validateSession(sessionCookieId);
        } catch (Exception e) {
            fail(e);
        }
    }

    /**
     * todo
     */
    @Test
    public void createSucceeds() {
        MockHttpServletRequestBuilder request = post("/api/v1/account/create")
                .param("token", VALID_TOKEN_ID)
                .contentType(MediaType.APPLICATION_JSON);

        try {
            Account account = getFromFirestore("Accounts", VALID_CUSTOMER_EMAIL, Account.class);
            mvc.perform(request.content(jsonOf(account)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Account created successfully!"));

            verify(repository, times(1)).create(any());
            verify(authenticator, times(1)).generateNewSession(VALID_TOKEN_ID);
        } catch (Throwable t) {
            fail(t);
        }
    }

    /**
     * todo
     */
    @Test
    public void createFailsInvalidToken() {
        String invalidTokenId = "invalid-token-id";
        MockHttpServletRequestBuilder request = post("/api/v1/account/create")
                .param("token", invalidTokenId)
                .contentType(MediaType.APPLICATION_JSON);

        try {
            Account account = getFromFirestore("Accounts", VALID_CUSTOMER_EMAIL, Account.class);
            mvc.perform(request.content(jsonOf(account)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Failed to create a session"));

            verify(repository, never()).create(any());
            verify(authenticator, times(1)).generateNewSession(invalidTokenId);
        } catch (Throwable t) {
            fail(t);
        }
    }

    /**
     * todo
     */
    @Test
    public void createFailsExpiredToken() {
        MockHttpServletRequestBuilder request = post("/api/v1/account/create")
                .param("token", EXPIRED_TOKEN_ID)
                .contentType(MediaType.APPLICATION_JSON);

        try {
            Account account = getFromFirestore("Accounts", "user1@email.com", Account.class);
            mvc.perform(request.content(jsonOf(account)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Recent sign in required"));

            verify(repository, never()).create(any());
            verify(authenticator, times(1)).generateNewSession(EXPIRED_TOKEN_ID);
        } catch (Throwable t) {
            fail(t);
        }
    }

    /**
     * todo
     */
    @Test
    public void createFailsMissingField() {
        MockHttpServletRequestBuilder request = post("/api/v1/account/create")
                .param("token", VALID_TOKEN_ID)
                .contentType(MediaType.APPLICATION_JSON);

        try {
            Account account = getFromFirestore("Accounts", "user2@email.com", Account.class);
            account.setType(null);
            account.setBankAccount(null);
            account.setEmail(null);
            account.setPassword(null);
            mvc.perform(request.content(jsonOf(account))).andExpect(status().isBadRequest());

            verify(repository, times(1)).create(account);
            verify(authenticator, times(1)).generateNewSession(VALID_TOKEN_ID);
        } catch (Throwable t) {
            fail(t);
        }
    }

    /**
     * todo
     */
    @Test
    public void loginSucceeds() {
        MockHttpServletRequestBuilder request = post("/api/v1/account/log-in")
                .param("email", VALID_CUSTOMER_EMAIL)
                .param("password", VALID_CUSTOMER_PASSWORD)
                .param("token", VALID_TOKEN_ID);

        try {
            mvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(content().string("Login successful!"));

            verify(repository, times(1)).login(VALID_CUSTOMER_EMAIL, VALID_CUSTOMER_PASSWORD);
            verify(authenticator, times(1)).generateNewSession(VALID_TOKEN_ID);
        } catch (Throwable t) {
            fail(t);
        }
    }

    /**
     * todo
     */
    @Test
    public void loginFailsInvalidToken() {
        String invalidTokenId = "invalid-token-id";
        MockHttpServletRequestBuilder request = post("/api/v1/account/log-in")
                .param("email", VALID_CUSTOMER_EMAIL)
                .param("password", VALID_CUSTOMER_PASSWORD)
                .param("token", invalidTokenId);

        try {
            mvc.perform(request)
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Failed to create a session"));

            verify(repository, never()).login(any(), any());
            verify(authenticator, times(1)).generateNewSession(invalidTokenId);
        } catch (Throwable t) {
            fail(t);
        }
    }

    /**
     * todo
     */
    @Test
    public void loginFailsExpiredToken() {
        MockHttpServletRequestBuilder request = post("/api/v1/account/log-in")
                .param("email", VALID_CUSTOMER_EMAIL)
                .param("password", VALID_CUSTOMER_PASSWORD)
                .param("token", EXPIRED_TOKEN_ID);

        try {
            mvc.perform(request)
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Recent sign in required"));

            verify(repository, never()).login(any(), any());
            verify(authenticator, times(1)).generateNewSession(EXPIRED_TOKEN_ID);
        } catch (Throwable t) {
            fail(t);
        }

    }

    /**
     * todo
     */
    @Test
    public void loginFailsInvalidCredentials() {
        MockHttpServletRequestBuilder request = post("/api/v1/account/log-in")
                .param("email", "non-existent-email")
                .param("password", "non-existent-password")
                .param("token", VALID_TOKEN_ID);

        try {
            mvc.perform(request).andExpect(status().isBadRequest());

            verify(repository, times(1)).login("non-existent-email", "non-existent-password");
            verify(authenticator, times(1)).generateNewSession(VALID_TOKEN_ID);
        } catch (Throwable t) {
            fail(t);
        }
    }

    /**
     * todo
     */
    @Test
    public void logoutSucceeds() {
        MockHttpServletRequestBuilder request = post("/api/v1/account/log-out")
                .cookie(new Cookie("session", VALID_SESSION_ID));

        try {
            mvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(content().string("Logout successful!"));

            verify(authenticator, times(1)).clearSessionAndRevoke(VALID_SESSION_ID);
        } catch (Throwable t) {
            fail(t);
        }
    }

    /**
     * todo
     */
    @Test
    public void logoutFailsInvalidSession() {
        String invalidSessionId = "invalid-session-id";
        MockHttpServletRequestBuilder request = post("/api/v1/account/log-out")
                .cookie(new Cookie("session", invalidSessionId));

        try {
            mvc.perform(request)
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Failed to revoke session (invalid or already revoked)"));

            verify(authenticator, times(1)).clearSessionAndRevoke(invalidSessionId);
        } catch (Throwable t) {
            fail(t);
        }
    }

    /**
     * todo
     */
    @Test
    public void updateSucceeds() {
        MockHttpServletRequestBuilder request = put("/api/v1/account/update")
                .param("email", VALID_CUSTOMER_EMAIL)
                .cookie(new Cookie("session", VALID_SESSION_ID))
                .contentType(MediaType.APPLICATION_JSON);

        try {
            Account account = mockAccount(AccountType.CUSTOMER);
            account.setEmail(VALID_CUSTOMER_EMAIL);
            mvc.perform(request.content(jsonOf(account)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Account successfully updated!"));

            verify(repository, times(1)).update(VALID_CUSTOMER_EMAIL, account);
            verify(authenticator, times(1)).validateSession(VALID_SESSION_ID);
        } catch (Throwable t) {
            fail(t);
        }
    }

    /**
     * todo
     */
    @Test
    public void updateFailsInvalidSession() {
        String invalidSessionId = "invalid-session-id";
        MockHttpServletRequestBuilder request = put("/api/v1/account/update")
                .param("email", VALID_CUSTOMER_EMAIL)
                .cookie(new Cookie("session", invalidSessionId))
                .contentType(MediaType.APPLICATION_JSON);

        try {
            Account account = mockAccount(AccountType.CUSTOMER);
            account.setEmail(VALID_CUSTOMER_EMAIL);
            mvc.perform(request.content(jsonOf(account)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Failed to validate session"));

            verify(repository, never()).update(any(), any());
            verify(authenticator, times(1)).validateSession(invalidSessionId);
        } catch (Throwable t) {
            fail(t);
        }
    }

    /**
     * todo
     */
    @Test
    public void updateFailsEmailNotFound() {
        String invalidEmail = "not-a-valid-email";
        MockHttpServletRequestBuilder request = put("/api/v1/account/update")
                .param("email", invalidEmail)
                .cookie(new Cookie("session", VALID_SESSION_ID))
                .contentType(MediaType.APPLICATION_JSON);

        try {
            Account account = mockAccount(AccountType.CUSTOMER);
            account.setEmail(invalidEmail);
            mvc.perform(request.content(jsonOf(account)))
                    .andExpect(status().isBadRequest());

            verify(repository, times(1)).update(invalidEmail, account);
            verify(authenticator, times(1)).validateSession(VALID_SESSION_ID);
        } catch (Throwable t) {
            fail(t);
        }
    }

    /**
     * todo
     */
    @Test
    public void getSucceeds() {
        MockHttpServletRequestBuilder request = get("/api/v1/account/get")
                .param("email", VALID_CUSTOMER_EMAIL)
                .cookie(new Cookie("session", VALID_SESSION_ID));

        try {
            Account account = getFromFirestore("Accounts", VALID_CUSTOMER_EMAIL, Account.class);
            Account.filterSensitiveData(account);
            mvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(content().string(jsonOf(account)));

            verify(repository, times(1)).get(VALID_CUSTOMER_EMAIL);
            verify(authenticator, times(1)).validateSession(VALID_SESSION_ID);
        } catch (Throwable t) {
            fail(t);
        }
    }

    /**
     * todo
     */
    @Test
    public void getFailsInvalidSession() {
        String invalidSessionId = "invalid-session-id";
        MockHttpServletRequestBuilder request = get("/api/v1/account/get")
                .param("email", VALID_CUSTOMER_EMAIL)
                .cookie(new Cookie("session", invalidSessionId));

        try {
            mvc.perform(request)
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Failed to validate session"));

            verify(repository, never()).get(any());
            verify(authenticator, times(1)).validateSession(invalidSessionId);
        } catch (Throwable t) {
            fail(t);
        }
    }

    /**
     * todo
     */
    @Test
    public void getFailsEmailNotFound() {
        String invalidEmail = "invalid-email";
        MockHttpServletRequestBuilder request = get("/api/v1/account/get")
                .param("email", invalidEmail)
                .cookie(new Cookie("session", VALID_SESSION_ID));

        try {
            mvc.perform(request).andExpect(status().isBadRequest());

            verify(repository, times(1)).get(invalidEmail);
            verify(authenticator, times(1)).validateSession(VALID_SESSION_ID);
        } catch (Throwable t) {
            fail(t);
        }
    }

    /**
     * todo
     */
    @Test
    public void usernameExistsSucceeds() {
        MockHttpServletRequestBuilder request = get("/api/v1/account/usernameExists")
                .param("username", VALID_CUSTOMER_USERNAME)
                .cookie(new Cookie("session", VALID_SESSION_ID));

        try {
            mvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));

            verify(repository, times(1)).usernameExists(VALID_CUSTOMER_USERNAME);
            verify(authenticator, times(1)).validateSession(VALID_SESSION_ID);
        } catch (Throwable t) {
            fail(t);
        }
    }

    /**
     * todo
     */
    @Test
    public void usernameExistsFailsInvalidSession() {
        String invalidSessionId = "invalid-session-id";
        MockHttpServletRequestBuilder request = get("/api/v1/account/usernameExists")
                .param("username", VALID_CUSTOMER_USERNAME)
                .cookie(new Cookie("session", invalidSessionId));

        try {
            mvc.perform(request)
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Failed to validate session"));

            verify(repository, never()).usernameExists(any());
            verify(authenticator, times(1)).validateSession(invalidSessionId);
        } catch (Throwable t) {
            fail(t);
        }
    }
}