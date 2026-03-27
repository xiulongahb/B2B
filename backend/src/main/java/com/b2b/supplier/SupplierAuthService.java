package com.b2b.supplier;

import com.b2b.mall.auth.AuthValidators;
import com.b2b.common.api.ApiException;
import com.b2b.config.B2bJwtProperties;
import com.b2b.domain.Supplier;
import com.b2b.domain.SupplierRepository;
import com.b2b.domain.SupplierStatus;
import com.b2b.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierAuthService {

    private final SupplierRepository supplierRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final B2bJwtProperties jwtProperties;

    public SupplierAuthService(
            SupplierRepository supplierRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            B2bJwtProperties jwtProperties) {
        this.supplierRepository = supplierRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public LoginResult register(String username, String password, String companyName) {
        String u = username == null ? "" : username.trim();
        if (!AuthValidators.isValidUsername(u)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "用户名格式不正确");
        }
        if (!AuthValidators.isValidPassword(password)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "密码格式不正确");
        }
        if (companyName == null || companyName.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "企业名称不能为空");
        }
        if (supplierRepository.findByUsername(u).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "用户名已存在");
        }
        Supplier s =
                new Supplier(u, passwordEncoder.encode(password), companyName.trim());
        supplierRepository.save(s);
        return new LoginResult(null, null, 0, s.getId(), s.getUsername(), s.getCompanyName(), s.getStatus().name(), "注册成功，请等待运营审核");
    }

    public LoginResult login(String username, String password) {
        Supplier s =
                supplierRepository
                        .findByUsername(username.trim())
                        .orElseThrow(
                                () ->
                                        new ApiException(
                                                HttpStatus.UNAUTHORIZED, "用户名或密码错误"));
        if (s.getStatus() != SupplierStatus.APPROVED) {
            throw new ApiException(HttpStatus.FORBIDDEN, "账号未审核通过或已拒绝");
        }
        if (!passwordEncoder.matches(password, s.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }
        String token = jwtService.createSupplierToken(s.getId(), s.getUsername());
        return new LoginResult(
                token,
                "Bearer",
                jwtProperties.getExpirationMs(),
                s.getId(),
                s.getUsername(),
                s.getCompanyName(),
                s.getStatus().name(),
                "ok");
    }

    public static final class LoginResult {
        private final String accessToken;
        private final String tokenType;
        private final long expiresInMs;
        private final Long supplierId;
        private final String username;
        private final String companyName;
        private final String status;
        private final String message;

        public LoginResult(
                String accessToken,
                String tokenType,
                long expiresInMs,
                Long supplierId,
                String username,
                String companyName,
                String status,
                String message) {
            this.accessToken = accessToken;
            this.tokenType = tokenType;
            this.expiresInMs = expiresInMs;
            this.supplierId = supplierId;
            this.username = username;
            this.companyName = companyName;
            this.status = status;
            this.message = message;
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

        public Long getSupplierId() {
            return supplierId;
        }

        public String getUsername() {
            return username;
        }

        public String getCompanyName() {
            return companyName;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
}
