package com.b2b.mall.auth.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingSmsGateway implements SmsGateway {

    private static final Logger log = LoggerFactory.getLogger(LoggingSmsGateway.class);

    @Override
    public void sendVerificationCode(String phone, String code) {
        log.info("[SMS mock] phone={} code={} (replace with real SMS provider in production)", phone, code);
    }
}
