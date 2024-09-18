package com.justinlee.dbimporter.exception;

public class FileTypeNotSupportExpection extends RuntimeException{
    public FileTypeNotSupportExpection() {
        super();
    }

    public FileTypeNotSupportExpection(String message) {
        super(message);
    }
}
