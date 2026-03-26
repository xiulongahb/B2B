package com.b2b.mall.auth.dto;

public class SendSmsResponse {
    private String message;
    private String debugCode;

    public SendSmsResponse() {}

    public SendSmsResponse(String message, String debugCode) {
        this.message = message;
        this.debugCode = debugCode;
    }

    public String getMessage() {
        return message;
    }

    public String getDebugCode() {
        return debugCode;
    }
}
