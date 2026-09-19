package com.haoyou.service.user.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 签发与解析（jjwt 0.12.5 API，HS256）
 */
@Component
public class JwtUtil {

    @Value("${haoyou.jwt.secret}")
    private String secret;

    @Value("${haoyou.jwt.expire-hours:24}")
    private long expireHours;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String sign(Long userId, String nickname) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("nickname", nickname)
                .expiration(new Date(System.currentTimeMillis() + expireHours * 3600_000L))
                .signWith(key())
                .compact();
    }

    /** 解析并校验签名/过期，失败抛 JwtException */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
