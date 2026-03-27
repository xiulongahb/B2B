package com.b2b.security;

import com.b2b.config.B2bJwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final B2bJwtProperties props;
    private final SecretKey key;

    public JwtService(B2bJwtProperties props) {
        this.props = props;
        byte[] bytes = props.getSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("b2b.jwt.secret must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String createMallMemberToken(long memberId, String phone, String username) {
        long now = System.currentTimeMillis();
        JwtBuilder builder =
                Jwts.builder()
                        .setSubject(String.valueOf(memberId))
                        .claim("phone", phone)
                        .claim("typ", "mall_member")
                        .setIssuedAt(new Date(now))
                        .setExpiration(new Date(now + props.getExpirationMs()));
        if (username != null) {
            builder.claim("username", username);
        }
        return builder.signWith(key).compact();
    }

    public String createAdminToken(long adminId, String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(String.valueOf(adminId))
                .claim("typ", "admin")
                .claim("username", username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + props.getExpirationMs()))
                .signWith(key)
                .compact();
    }

    public String createSupplierToken(long supplierId, String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(String.valueOf(supplierId))
                .claim("typ", "supplier")
                .claim("username", username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + props.getExpirationMs()))
                .signWith(key)
                .compact();
    }

    public Optional<UnifiedPayload> parseToken(String token) {
        try {
            Claims claims =
                    Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();
            String typ = claims.get("typ", String.class);
            long id = Long.parseLong(claims.getSubject());
            String uname = claims.get("username", String.class);
            if ("mall_member".equals(typ)) {
                String phone = claims.get("phone", String.class);
                return Optional.of(
                        new UnifiedPayload(
                                UnifiedPayload.Kind.MALL_MEMBER, id, phone, uname));
            }
            if ("admin".equals(typ)) {
                return Optional.of(
                        new UnifiedPayload(UnifiedPayload.Kind.ADMIN, id, null, uname));
            }
            if ("supplier".equals(typ)) {
                return Optional.of(
                        new UnifiedPayload(UnifiedPayload.Kind.SUPPLIER, id, null, uname));
            }
            return Optional.empty();
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    /** @deprecated use {@link #parseToken(String)} */
    @Deprecated
    public Optional<MallTokenPayload> parseMallMemberToken(String token) {
        return parseToken(token)
                .filter(p -> p.getKind() == UnifiedPayload.Kind.MALL_MEMBER)
                .map(
                        p ->
                                new MallTokenPayload(
                                        p.getId(), p.getPhone(), p.getUsername()));
    }

    public static final class UnifiedPayload {
        public enum Kind {
            MALL_MEMBER,
            ADMIN,
            SUPPLIER
        }

        private final Kind kind;
        private final long id;
        private final String phone;
        private final String username;

        public UnifiedPayload(Kind kind, long id, String phone, String username) {
            this.kind = kind;
            this.id = id;
            this.phone = phone;
            this.username = username;
        }

        public Kind getKind() {
            return kind;
        }

        public long getId() {
            return id;
        }

        public String getPhone() {
            return phone;
        }

        public String getUsername() {
            return username;
        }
    }

    public static final class MallTokenPayload {
        private final long memberId;
        private final String phone;
        private final String username;

        public MallTokenPayload(long memberId, String phone, String username) {
            this.memberId = memberId;
            this.phone = phone;
            this.username = username;
        }

        public long getMemberId() {
            return memberId;
        }

        public String getPhone() {
            return phone;
        }

        public String getUsername() {
            return username;
        }
    }
}
