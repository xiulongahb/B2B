package com.b2b.admin;

import com.b2b.common.api.ApiException;
import com.b2b.config.B2bJwtProperties;
import com.b2b.domain.AdminUser;
import com.b2b.domain.AdminUserRepository;
import com.b2b.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final B2bJwtProperties jwtProperties;

    public AdminAuthService(
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            B2bJwtProperties jwtProperties) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    public LoginResult login(String username, String password) {
        AdminUser u =
                adminUserRepository
                        .findByUsername(username.trim())
                        .orElseThrow(
                                () ->
                                        new ApiException(
                                                HttpStatus.UNAUTHORIZED, "用户名或密码错误"));
        if (!passwordEncoder.matches(password, u.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }
        String token = jwtService.createAdminToken(u.getId(), u.getUsername());
        return new LoginResult(token, "Bearer", jwtProperties.getExpirationMs(), u.getId(), u.getUsername());
    }

    public static final class LoginResult {
        private final String accessToken;
        private final String tokenType;
        private final long expiresInMs;
        private final Long adminId;
        private final String username;

        public LoginResult(
                String accessToken, String tokenType, long expiresInMs, Long adminId, String username) {
            this.accessToken = accessToken;
            this.tokenType = tokenType;
            this.expiresInMs = expiresInMs;
            this.adminId = adminId;
            this.username = username;
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

        public Long getAdminId() {
            return adminId;
        }

        public String getUsername() {
            return username;
        }
    }
}
