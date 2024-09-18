package com.justinlee.dbimporter.exception;

public class WriteMysqlExpection extends RuntimeException{
    public WriteMysqlExpection() {
    }

    public WriteMysqlExpection(String message) {
        super(message);
    }

    public WriteMysqlExpection(String message, Throwable cause) {
        super(message, cause);
    }

    public WriteMysqlExpection(Throwable cause) {
        super(cause);
    }

    public WriteMysqlExpection(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
