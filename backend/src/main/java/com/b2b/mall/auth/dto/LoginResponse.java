package com.b2b.mall.auth.dto;

public class LoginResponse {
    private String accessToken;
    private String tokenType;
    private long expiresInMs;
    private Long memberId;
    private String phone;
    private String memberType;

    public LoginResponse() {}

    public LoginResponse(
            String accessToken,
            String tokenType,
            long expiresInMs,
            Long memberId,
            String phone,
            String memberType) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresInMs = expiresInMs;
        this.memberId = memberId;
        this.phone = phone;
        this.memberType = memberType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresInMs() {
        return expiresInMs;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getPhone() {
        return phone;
    }

    public String getMemberType() {
        return memberType;
    }
}
