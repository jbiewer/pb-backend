package com.piggybank.util;

/**
 * Generic query exception, used for situational error throwing/handling.
 */
public class QueryException extends Exception {
    public QueryException(String message) {
        super(message);
    }
}
