package com.piggybank.controller;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.servlet.http.Cookie;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import static com.piggybank.mocks.MockSessionAuthenticator.VALID_SESSION_ID;
import static com.piggybank.mocks.MockSessionAuthenticator.VALID_TOKEN_ID;
import static com.piggybank.util.FirebaseEmulatorServices.*;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerTest {

    @MockBean private AccountRepository repository;
    @MockBean private SessionAuthenticator authenticator;

    @Autowired private MockMvc mvc;

    /**
     * todo
     */
    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException  {
        MockAccountRepository.reset(repository);
        MockSessionAuthenticator.reset(authenticator);

        URI uri = requireNonNull(ClassLoader.getSystemResource("collections")).toURI();
        generateFirestoreData(new File(uri));
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
                    .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()))
                    .andExpect(content().string("Failed to validate session"));
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
            Account account = getFromFirestore("Accounts", "user1@email.com", Account.class);
            String accountJson = new ObjectMapper().writeValueAsString(account);
            mvc.perform(request.content(accountJson))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Account created successfully!"));
        } catch (Throwable t) {
            fail(t);
        }
    }

//    @Test
//    public void getSucceeds() {
//        String sessionCookieId = VALID_SESSION_ID;
//        MockHttpServletRequestBuilder request = get("/api/v1/account/get")
//                .param("email", "user1@email.com")
//                .cookie(new Cookie("session", sessionCookieId));
//
//        try {
//            Account account = FirebaseEmulatorServices.get("Accounts", "user1@email.com", Account.class);
//            String accountJson = new ObjectMapper().writeValueAsString(account);
//            mvc.perform(request)
//                    .andExpect(status().isOk())
//                    .andDo(print());
//                    .andExpect(content().json(accountJson));
//        } catch (Exception e) {
//            fail(e);
//        }
//    }
}
