package com.b2b.mall.auth.dto;

public class LoginResponse {
    private String accessToken;
    private String tokenType;
    private long expiresInMs;
    private Long memberId;
    /** 注册用户名 */
    private String username;
    private String phone;
    private String memberType;

    public LoginResponse() {}

    public LoginResponse(
            String accessToken,
            String tokenType,
            long expiresInMs,
            Long memberId,
            String username,
            String phone,
            String memberType) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresInMs = expiresInMs;
        this.memberId = memberId;
        this.username = username;
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

    public String getUsername() {
        return username;
    }

    public String getPhone() {
        return phone;
    }

    public String getMemberType() {
        return memberType;
    }
}
