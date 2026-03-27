package com.b2b.supplier;

import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/supplier/auth")
public class SupplierAuthController {

    private final SupplierAuthService supplierAuthService;

    public SupplierAuthController(SupplierAuthService supplierAuthService) {
        this.supplierAuthService = supplierAuthService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody SupplierRegisterBody body) {
        SupplierAuthService.LoginResult r =
                supplierAuthService.register(body.getUsername(), body.getPassword(), body.getCompanyName());
        Map<String, Object> m = new HashMap<>();
        m.put("supplierId", r.getSupplierId());
        m.put("username", r.getUsername());
        m.put("companyName", r.getCompanyName());
        m.put("status", r.getStatus());
        m.put("message", r.getMessage());
        return ResponseEntity.ok(m);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody SupplierLoginBody body) {
        SupplierAuthService.LoginResult r =
                supplierAuthService.login(body.getUsername(), body.getPassword());
        Map<String, Object> m = new HashMap<>();
        m.put("accessToken", r.getAccessToken());
        m.put("tokenType", r.getTokenType());
        m.put("expiresInMs", r.getExpiresInMs());
        m.put("supplierId", r.getSupplierId());
        m.put("username", r.getUsername());
        m.put("companyName", r.getCompanyName());
        m.put("status", r.getStatus());
        m.put("message", r.getMessage());
        return ResponseEntity.ok(m);
    }

    public static class SupplierRegisterBody {
        @NotBlank private String username;
        @NotBlank private String password;
        @NotBlank private String companyName;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getCompanyName() {
            return companyName;
        }

        public void setCompanyName(String companyName) {
            this.companyName = companyName;
        }
    }

    public static class SupplierLoginBody {
        @NotBlank private String username;
        @NotBlank private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
