package com.b2b.mall.auth.dto;

public class MemberMeResponse {
    private Long memberId;
    private String phone;

    public MemberMeResponse() {}

    public MemberMeResponse(Long memberId, String phone) {
        this.memberId = memberId;
        this.phone = phone;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getPhone() {
        return phone;
    }
}
