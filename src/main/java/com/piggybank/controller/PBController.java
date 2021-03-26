package com.piggybank.controller;

import com.piggybank.components.SessionAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Root class of all controllers for the PiggyBank back-end service.
 * Contains bean instances for the respective repository as well as the session authenticator.
 *
 * @param <R> Respective repository type.
 */
public abstract class PBController<R> {
    @Autowired protected R repository;
    @Autowired protected SessionAuthenticator authenticator;
}
