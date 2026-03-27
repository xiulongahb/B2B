package com.b2b.mall.auth;

import com.b2b.common.api.ApiException;
import com.b2b.config.B2bJwtProperties;
import com.b2b.config.B2bMallSmsProperties;
import com.b2b.mall.auth.sms.InMemorySmsCodeStore;
import com.b2b.mall.auth.sms.SmsGateway;
import com.b2b.mall.member.Member;
import com.b2b.mall.member.MemberRepository;
import com.b2b.mall.member.MemberType;
import com.b2b.security.JwtService;
import java.security.SecureRandom;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MallAuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final B2bMallSmsProperties smsProps;
    private final InMemorySmsCodeStore smsCodeStore;
    private final SmsGateway smsGateway;
    private final MemberRepository memberRepository;
    private final JwtService jwtService;
    private final B2bJwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;

    public MallAuthService(
            B2bMallSmsProperties smsProps,
            InMemorySmsCodeStore smsCodeStore,
            SmsGateway smsGateway,
            MemberRepository memberRepository,
            JwtService jwtService,
            B2bJwtProperties jwtProperties,
            PasswordEncoder passwordEncoder) {
        this.smsProps = smsProps;
        this.smsCodeStore = smsCodeStore;
        this.smsGateway = smsGateway;
        this.memberRepository = memberRepository;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.passwordEncoder = passwordEncoder;
    }

    public SendSmsResult sendLoginCode(String rawPhone) {
        String phone = PhoneNumber.normalize(rawPhone);
        if (!PhoneNumber.isValidCnMobile(phone)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "手机号格式不正确");
        }
        String code = randomDigits(smsProps.getCodeLength());
        long ttlMs = smsProps.getCodeTtlSeconds() * 1000L;
        long intervalMs = smsProps.getResendIntervalSeconds() * 1000L;
        boolean accepted = smsCodeStore.tryPutCode(phone, code, ttlMs, intervalMs);
        if (!accepted) {
            throw new ApiException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "发送过于频繁，请 " + smsProps.getResendIntervalSeconds() + " 秒后再试");
        }
        smsGateway.sendVerificationCode(phone, code);
        String debugCode = smsProps.isDevExposeCodeInResponse() ? code : null;
        return new SendSmsResult(debugCode);
    }

    @Transactional
    public LoginResult register(String rawUsername, String rawPassword, String rawPhone, String rawSmsCode) {
        String username = rawUsername == null ? null : rawUsername.trim();
        String password = rawPassword;
        String phone = PhoneNumber.normalize(rawPhone);
        String smsCode = rawSmsCode == null ? "" : rawSmsCode.trim();

        if (!AuthValidators.isValidUsername(username)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST, "用户名须为 4-32 位字母、数字或下划线");
        }
        if (!AuthValidators.isValidPassword(password)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "密码须 8-64 位且包含大小写字母与数字");
        }
        if (!PhoneNumber.isValidCnMobile(phone)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "手机号格式不正确");
        }
        if (smsCode.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "请输入验证码");
        }
        if (!smsCodeStore.verifyAndConsume(phone, smsCode)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "验证码错误或已过期");
        }
        if (memberRepository.existsByUsername(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "用户名已被使用");
        }
        if (memberRepository.existsByPhone(phone)) {
            throw new ApiException(HttpStatus.CONFLICT, "该手机号已注册");
        }
        String hash = passwordEncoder.encode(password);
        Member member =
                memberRepository.save(
                        new Member(username, hash, phone, MemberType.RETAIL));
        return buildLoginResult(member);
    }

    @Transactional
    public LoginResult loginByPassword(String rawUsername, String rawPassword) {
        String username = rawUsername == null ? "" : rawUsername.trim();
        String password = rawPassword == null ? "" : rawPassword;
        if (username.isEmpty() || password.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "请输入用户名和密码");
        }
        Member member =
                memberRepository
                        .findByUsername(username)
                        .orElseThrow(
                                () ->
                                        new ApiException(
                                                HttpStatus.UNAUTHORIZED, "用户名或密码错误"));
        if (member.getPasswordHash() == null
                || !passwordEncoder.matches(password, member.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }
        member.touch();
        return buildLoginResult(member);
    }

    @Transactional
    public LoginResult loginBySms(String rawPhone, String rawCode) {
        String phone = PhoneNumber.normalize(rawPhone);
        if (!PhoneNumber.isValidCnMobile(phone)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "手机号格式不正确");
        }
        String code = rawCode == null ? "" : rawCode.trim();
        if (code.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "请输入验证码");
        }
        if (!smsCodeStore.verifyAndConsume(phone, code)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "验证码错误或已过期");
        }
        Member member =
                memberRepository
                        .findByPhone(phone)
                        .orElseThrow(
                                () ->
                                        new ApiException(
                                                HttpStatus.UNAUTHORIZED, "该手机号尚未注册，请先注册"));
        member.touch();
        return buildLoginResult(member);
    }

    private LoginResult buildLoginResult(Member member) {
        String token =
                jwtService.createMallMemberToken(
                        member.getId(), member.getPhone(), member.getUsername());
        return new LoginResult(
                token,
                "Bearer",
                jwtProperties.getExpirationMs(),
                member.getId(),
                member.getUsername(),
                member.getPhone(),
                member.getMemberType().name());
    }

    private static String randomDigits(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    public static final class SendSmsResult {
        private final String debugCode;

        public SendSmsResult(String debugCode) {
            this.debugCode = debugCode;
        }

        public String getDebugCode() {
            return debugCode;
        }
    }

    public static final class LoginResult {
        private final String accessToken;
        private final String tokenType;
        private final long expiresInMs;
        private final Long memberId;
        private final String username;
        private final String phone;
        private final String memberType;

        public LoginResult(
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
}
