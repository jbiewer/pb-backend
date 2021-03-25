package com.piggybank.controller;

import com.piggybank.components.SessionAuthenticator;

/**
 * Root class of all controllers for the PiggyBank back-end service.
 * Contains bean instances for the respective repository as well as the session authenticator.
 *
 * @param <R> Respective repository type.
 */
public abstract class PBController<R> {
    protected final R repository;
    protected final SessionAuthenticator authenticator;

    /**
     * Constructor for Spring dependency injection.
     *
     * @param repository Interface for the controller-specific business logic.
     * @param authenticator Authenticator for tokens and session IDs.
     */
    public PBController(R repository, SessionAuthenticator authenticator) {
        this.repository = repository;
        this.authenticator = authenticator;
    }
}
