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
 * todo
 */
@Component
public class SessionAuthenticator {
    /**
     * todo
     * @param sessionCookie
     * @throws FirebaseAuthException
     */
    public void validateSession(@NonNull String sessionCookie) throws FirebaseAuthException {
        FirebaseAuth.getInstance().verifyIdToken(sessionCookie, true);
    }

    /**
     * todo
     * @param token
     * @return
     * @throws FirebaseAuthException
     * @throws AuthException
     */
    @NonNull
    public Cookie generateNewSession(@NonNull String token) throws FirebaseAuthException, AuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
        long authTimeMillis = TimeUnit.SECONDS.toMillis((long) decodedToken.getClaims().get("auth_time"));
        if (System.currentTimeMillis() - authTimeMillis >= TimeUnit.MINUTES.toMillis(5)) {
            throw new AuthException();
        }

        long expiresIn = TimeUnit.DAYS.toMillis(5);

        // Attempt to generate a session cookie.
        String sessionCookie;
        SessionCookieOptions options = SessionCookieOptions.builder()
                .setExpiresIn(expiresIn)
                .build();
        sessionCookie = FirebaseAuth.getInstance().createSessionCookie(token, options);

        // Configure cookie and respond with OK.
        Cookie cookie = new Cookie("session", sessionCookie);
        cookie.setMaxAge((int) expiresIn / 1000);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        return cookie;
    }
}
