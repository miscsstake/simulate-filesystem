package com.eaglesoup.exception;

public class BusinessException extends Exception {
    private int errorCode;
    public static final int FILE_NO_EXIST = 1001;

    public BusinessException(String msg) {
        super(msg);
    }

    public BusinessException(int code, String msg) {
        super(msg);
        this.errorCode = code;
    }
}
