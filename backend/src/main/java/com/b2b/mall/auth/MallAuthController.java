package com.b2b.mall.auth;

import com.b2b.mall.auth.dto.LoginResponse;
import com.b2b.mall.auth.dto.MemberMeResponse;
import com.b2b.mall.auth.dto.PasswordLoginRequest;
import com.b2b.mall.auth.dto.RegisterRequest;
import com.b2b.mall.auth.dto.SendSmsRequest;
import com.b2b.mall.auth.dto.SendSmsResponse;
import com.b2b.mall.auth.dto.SmsLoginRequest;
import com.b2b.security.MallMemberPrincipal;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mall/auth")
public class MallAuthController {

    private final MallAuthService mallAuthService;

    public MallAuthController(MallAuthService mallAuthService) {
        this.mallAuthService = mallAuthService;
    }

    @PostMapping("/sms/send")
    public ResponseEntity<SendSmsResponse> sendSms(@Valid @RequestBody SendSmsRequest req) {
        MallAuthService.SendSmsResult r = mallAuthService.sendLoginCode(req.getPhone());
        return ResponseEntity.ok(new SendSmsResponse("验证码已发送", r.getDebugCode()));
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        MallAuthService.LoginResult r =
                mallAuthService.register(
                        req.getUsername(), req.getPassword(), req.getPhone(), req.getSmsCode());
        return ResponseEntity.ok(toLoginResponse(r));
    }

    @PostMapping("/password/login")
    public ResponseEntity<LoginResponse> loginByPassword(@Valid @RequestBody PasswordLoginRequest req) {
        MallAuthService.LoginResult r =
                mallAuthService.loginByPassword(req.getUsername(), req.getPassword());
        return ResponseEntity.ok(toLoginResponse(r));
    }

    @PostMapping("/sms/login")
    public ResponseEntity<LoginResponse> loginBySms(@Valid @RequestBody SmsLoginRequest req) {
        MallAuthService.LoginResult r = mallAuthService.loginBySms(req.getPhone(), req.getCode());
        return ResponseEntity.ok(toLoginResponse(r));
    }

    @GetMapping("/me")
    public ResponseEntity<MemberMeResponse> me(@AuthenticationPrincipal MallMemberPrincipal principal) {
        return ResponseEntity.ok(
                new MemberMeResponse(
                        principal.getMemberId(), principal.getLoginName(), principal.getPhone()));
    }

    private static LoginResponse toLoginResponse(MallAuthService.LoginResult r) {
        return new LoginResponse(
                r.getAccessToken(),
                r.getTokenType(),
                r.getExpiresInMs(),
                r.getMemberId(),
                r.getUsername(),
                r.getPhone(),
                r.getMemberType());
    }
}
