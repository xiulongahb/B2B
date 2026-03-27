package com.b2b.mall.auth.dto;

public class MemberMeResponse {
    private Long memberId;
    private String username;
    private String phone;

    public MemberMeResponse() {}

    public MemberMeResponse(Long memberId, String username, String phone) {
        this.memberId = memberId;
        this.username = username;
        this.phone = phone;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getUsername() {
        return username;
    }

    public String getPhone() {
        return phone;
    }
}
