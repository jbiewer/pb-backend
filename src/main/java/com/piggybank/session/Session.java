package com.piggybank.session;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * todo
 */
public class Session {
    private final String sessionId;
    private long lastRefreshed;

    /**
     * todo
     */
    public Session() {
        this.sessionId = UUID.randomUUID().toString();
    }

    public String getSessionId() {
        return sessionId;
    }

    /**
     * todo
     */
    public void refresh() {
        lastRefreshed = 0;
    }

    /**
     * todo
     * @return
     */
    public boolean hasExpired() {
        long duration = System.currentTimeMillis() - lastRefreshed;
        // todo: make expiration time a property.
        return TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS) > 15;
    }
}
