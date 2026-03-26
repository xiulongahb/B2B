package com.b2b.mall.auth.sms;

/** 短信发送网关，生产环境对接阿里云/腾讯云等 */
public interface SmsGateway {

    void sendVerificationCode(String phone, String code);
}
