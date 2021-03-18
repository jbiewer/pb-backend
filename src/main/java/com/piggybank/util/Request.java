package com.piggybank.util;

public class Request<T> {
    private final T data;
    private final String token;

    public Request(T data, String token) {
        this.data = data;
        this.token = token;
    }

    public T getData() {
        return data;
    }

    public String getToken() {
        return token;
    }
}
