package com.b2b.admin;

import com.b2b.domain.AdminUser;
import java.time.Instant;

/** 后台用户列表/详情（不含密码） */
public class AdminUserResponse {

    private final Long id;
    private final String username;
    private final boolean enabled;
    private final Instant createdAt;

    public AdminUserResponse(Long id, String username, boolean enabled, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.enabled = enabled;
        this.createdAt = createdAt;
    }

    public static AdminUserResponse from(AdminUser u) {
        return new AdminUserResponse(u.getId(), u.getUsername(), u.isEnabled(), u.getCreatedAt());
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
