package com.b2b.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "b2b.mall.sms")
public class B2bMallSmsProperties {

    private int codeLength = 6;
    private int codeTtlSeconds = 300;
    private int resendIntervalSeconds = 60;
    /** 仅开发：发码接口响应中带回验证码 */
    private boolean devExposeCodeInResponse = false;

    public int getCodeLength() {
        return codeLength;
    }

    public void setCodeLength(int codeLength) {
        this.codeLength = codeLength;
    }

    public int getCodeTtlSeconds() {
        return codeTtlSeconds;
    }

    public void setCodeTtlSeconds(int codeTtlSeconds) {
        this.codeTtlSeconds = codeTtlSeconds;
    }

    public int getResendIntervalSeconds() {
        return resendIntervalSeconds;
    }

    public void setResendIntervalSeconds(int resendIntervalSeconds) {
        this.resendIntervalSeconds = resendIntervalSeconds;
    }

    public boolean isDevExposeCodeInResponse() {
        return devExposeCodeInResponse;
    }

    public void setDevExposeCodeInResponse(boolean devExposeCodeInResponse) {
        this.devExposeCodeInResponse = devExposeCodeInResponse;
    }
}
