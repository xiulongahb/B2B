package com.b2b.admin;

import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody AdminLoginRequest req) {
        AdminAuthService.LoginResult r = adminAuthService.login(req.getUsername(), req.getPassword());
        Map<String, Object> body = new HashMap<>();
        body.put("accessToken", r.getAccessToken());
        body.put("tokenType", r.getTokenType());
        body.put("expiresInMs", r.getExpiresInMs());
        body.put("adminId", r.getAdminId());
        body.put("username", r.getUsername());
        return ResponseEntity.ok(body);
    }
}
