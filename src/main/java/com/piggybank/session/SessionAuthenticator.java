package com.piggybank.session;

import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * todo
 */
public class SessionAuthenticator {
    private final Map<String, Session> cache = new HashMap<>();

    /**
     * todo
     * @param username
     * @param sessionId
     * @return
     */
    public boolean sessionIsValid(@NonNull String username, @NonNull String sessionId) {
        Session session = cache.get(username);
        if (session != null && session.getSessionId().equals(sessionId)) {
            if (cache.get(username).hasExpired()) {
                cache.remove(username);
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * todo
     * @param username
     * @return
     */
    @NonNull
    public String generateNewSession(@NonNull String username) {
        Session session = new Session();
        cache.remove(username);
        cache.put(username, session);
        return session.getSessionId();
    }
}
