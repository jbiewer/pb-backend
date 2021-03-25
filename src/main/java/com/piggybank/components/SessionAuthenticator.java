package com.piggybank.components;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.SessionCookieOptions;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.security.auth.message.AuthException;
import javax.servlet.http.Cookie;
import java.util.concurrent.TimeUnit;

/**
 * Manages all authentication procedures on the server-side.
 * Handles generation, validation, and revocation of session IDs through the FirebaseAuth API.
 */
@Component
public class SessionAuthenticator {
    /**
     * Given a session ID, verifies the ID's authenticity through FirebaseAuth.
     * A session ID would be valid if it hasn't expired from the initial expiration time set and it
     * hasn't been revoked yet.
     *
     * @param sessionCookie Session ID from the HTTP request cookie.
     * @throws FirebaseAuthException If the session ID is not authentic, or has been revoked.
     */
    public void validateSession(@NonNull String sessionCookie) throws FirebaseAuthException {
        FirebaseAuth.getInstance().verifyIdToken(sessionCookie, true);
    }

    /**
     * Given a session ID, revokes the authenticity of it through FirebaseAuth.
     * Initially attempts to verify a session cookie and retrieves the associated token. The token is then used to
     * revoke the session entirely. Any subsequent requests using the session ID is thus unauthorized.
     *
     * @param sessionCookie Session ID from the HTTP request cookie.
     * @throws FirebaseAuthException If the session ID is not authentic, or has already been revoked.
     */
    public void clearSessionAndRevoke(@NonNull String sessionCookie) throws FirebaseAuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifySessionCookie(sessionCookie, true);
        FirebaseAuth.getInstance().revokeRefreshTokens(decodedToken.getUid());
    }

    /**
     * Given a token from FirebaseAuth, generates a session ID and puts it in a cookie.
     * Initially verifies the token's authenticity with FirebaseAuth and checks if it's expired. The session
     * ID is then created from FirebaseAuth and then put in a cookie with an expiration of 5 days.
     *
     * @param token Token from the HTTP request parameter.
     * @return Session cookie containing the session ID.
     * @throws FirebaseAuthException If the token is invalid.
     * @throws AuthException If the token has expired.
     */
    @NonNull
    public Cookie generateNewSession(@NonNull String token) throws FirebaseAuthException, AuthException {
        // Verify Firebase token received.
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
        long authTimeMillis = TimeUnit.SECONDS.toMillis((long) decodedToken.getClaims().get("auth_time"));
        if (System.currentTimeMillis() - authTimeMillis >= TimeUnit.MINUTES.toMillis(5)) {
            throw new AuthException();
        }

        // Attempt to generate a session cookie.
        long expiresIn = TimeUnit.DAYS.toMillis(5);
        String sessionCookie;
        SessionCookieOptions options = SessionCookieOptions.builder()
                .setExpiresIn(expiresIn)
                .build();
        sessionCookie = FirebaseAuth.getInstance().createSessionCookie(token, options);

        // Configure and return cookie.
        Cookie cookie = new Cookie("session", sessionCookie);
        cookie.setMaxAge((int) expiresIn / 1000);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        return cookie;
    }
}
