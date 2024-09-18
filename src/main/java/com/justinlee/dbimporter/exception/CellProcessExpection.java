package com.justinlee.dbimporter.exception;
/***
 * 单元格处理异常
 * **/
public class CellProcessExpection extends RuntimeException{
    public CellProcessExpection() {
    }

    public CellProcessExpection(String message) {
        super(message);
    }

    public CellProcessExpection(String message, Throwable cause) {
        super(message, cause);
    }

    public CellProcessExpection(Throwable cause) {
        super(cause);
    }

    public CellProcessExpection(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
