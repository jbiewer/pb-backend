package com.piggybank.mocks;

import com.google.firebase.auth.FirebaseAuthException;
import com.piggybank.components.SessionAuthenticator;

import java.util.UUID;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Mockito.*;

public class MockSessionAuthenticator {
    private static final String VALID_SESSION_ID = UUID.randomUUID().toString();

    /**
     * Returns a mock instance of SessionAuthenticator.
     */
    public static void reset(SessionAuthenticator authenticator) {
        try {
            doNothing().when(authenticator).validateSession(VALID_SESSION_ID);
            doThrow(FirebaseAuthException.class).when(authenticator).validateSession(not(eq(VALID_SESSION_ID)));
        } catch (FirebaseAuthException e) { /* ignore */ }
    }

    /**
     * @return The only valid session ID for the purpose of testing.
     */
    public static String getValidSession() {
        return VALID_SESSION_ID;
    }
}
