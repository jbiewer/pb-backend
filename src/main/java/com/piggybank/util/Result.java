package com.piggybank.util;

public class Result<T> {
    private String errorType;
    private String errorMessage;
    private T data;

    public Result() {
        this.data = null;
        this.errorMessage = null;
        this.errorType = null;
    }

    public Result(T data) {
        this.data = data;
        this.errorMessage = null;
        this.errorType = null;
    }

    public Result(Throwable t) {
        this.data = null;
        this.setThrowable(t);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setThrowable(Throwable t) {
        errorMessage = t.getMessage();
        errorType = t.getClass().getSimpleName();
    }

    public String getErrorType() {
        return errorType;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isOk() {
        return errorType == null && errorMessage == null;
    }
}