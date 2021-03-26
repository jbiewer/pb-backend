package com.piggybank.mocks;

import com.google.firebase.auth.FirebaseAuthException;
import com.piggybank.components.SessionAuthenticator;
import org.springframework.lang.NonNull;

import javax.security.auth.message.AuthException;
import javax.servlet.http.Cookie;
import java.util.UUID;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.Mockito.*;

/**
 * todo
 */
public class MockSessionAuthenticator {
    public static final String VALID_SESSION_ID = UUID.randomUUID().toString();
    public static final String VALID_TOKEN_ID = UUID.randomUUID().toString();
    public static final String EXPIRED_TOKEN_ID = UUID.randomUUID().toString();

    public static void reset(@NonNull SessionAuthenticator authenticator) {
        Cookie validSessionCookie = new Cookie("session", VALID_SESSION_ID);

        try {
            // validateSession
            doNothing().when(authenticator).validateSession(eq(VALID_SESSION_ID));
            doThrow(FirebaseAuthException.class).when(authenticator).validateSession(not(eq(VALID_SESSION_ID)));

            // generateNewSession
            doReturn(validSessionCookie).when(authenticator).generateNewSession(eq(VALID_TOKEN_ID));
            doThrow(AuthException.class).when(authenticator).generateNewSession(eq(EXPIRED_TOKEN_ID));
            doThrow(FirebaseAuthException.class).when(authenticator).generateNewSession(not(or(eq(VALID_TOKEN_ID), eq(EXPIRED_TOKEN_ID))));

            // clearSessionAndRevoke
            doNothing().when(authenticator).clearSessionAndRevoke(eq(VALID_SESSION_ID));
            doThrow(FirebaseAuthException.class).when(authenticator).clearSessionAndRevoke(not(eq(VALID_SESSION_ID)));
        } catch (FirebaseAuthException | AuthException e) { /* ignore */ }
    }

}
