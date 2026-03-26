package com.b2b.common.api;

public class ErrorBody {
    private String code;
    private String message;

    public ErrorBody() {}

    public ErrorBody(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
