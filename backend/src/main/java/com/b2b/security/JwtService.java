package com.b2b.security;

import com.b2b.config.B2bJwtProperties;
import io.jsonwebtoken.Claims;
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

    public String createMallMemberToken(long memberId, String phone) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .claim("phone", phone)
                .claim("typ", "mall_member")
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + props.getExpirationMs()))
                .signWith(key)
                .compact();
    }

    public Optional<MallTokenPayload> parseMallMemberToken(String token) {
        try {
            Claims claims =
                    Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();
            if (!"mall_member".equals(claims.get("typ"))) {
                return Optional.empty();
            }
            long memberId = Long.parseLong(claims.getSubject());
            String p = claims.get("phone", String.class);
            return Optional.of(new MallTokenPayload(memberId, p));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public static final class MallTokenPayload {
        private final long memberId;
        private final String phone;

        public MallTokenPayload(long memberId, String phone) {
            this.memberId = memberId;
            this.phone = phone;
        }

        public long getMemberId() {
            return memberId;
        }

        public String getPhone() {
            return phone;
        }
    }
}
