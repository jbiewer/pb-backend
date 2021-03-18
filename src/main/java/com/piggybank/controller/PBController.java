package com.piggybank.controller;

import com.piggybank.components.SessionAuthenticator;

/**
 * todo
 * @param <R>
 */
public abstract class PBController<R> {
    protected final R repository;
    protected final SessionAuthenticator authenticator;

    /**
     * todo
     * @param repository
     * @param repository
     */
    public PBController(R repository, SessionAuthenticator authenticator) {
        this.repository = repository;
        this.authenticator = authenticator;
    }
}
