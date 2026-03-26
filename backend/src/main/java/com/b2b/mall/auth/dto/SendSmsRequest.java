package com.b2b.mall.auth.dto;

import javax.validation.constraints.NotBlank;

public class SendSmsRequest {
    @NotBlank private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
