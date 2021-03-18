package com.piggybank.controller;

/**
 * todo
 * @param <R>
 */
public abstract class PBController<R> {
    protected final R repository;

    /**
     * todo
     * @param repository
     */
    public PBController(R repository) {
        this.repository = repository;
    }
}
