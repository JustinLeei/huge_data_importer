package com.justinlee.dbimporter.exception;

public class CreateDataTableExpection extends RuntimeException{
    public CreateDataTableExpection() {
        super();
    }

    public CreateDataTableExpection(String message) {
        super(message);
    }

    public CreateDataTableExpection(String message, Throwable cause) {
        super(message, cause);
    }

    public CreateDataTableExpection(Throwable cause) {
        super(cause);
    }

    protected CreateDataTableExpection(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
