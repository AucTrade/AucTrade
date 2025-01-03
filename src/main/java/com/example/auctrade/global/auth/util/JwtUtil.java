package com.example.auctrade.global.auth.util;

import com.example.auctrade.global.auth.vo.TokenPayload;
import com.example.auctrade.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
@Component
@Slf4j(topic = "JWT UTIL")
public class JwtUtil {
    private final String AUTHORIZATION_KEY = "auth";
    private final String BEARER_PREFIX = "Bearer ";
    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret.key}") String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    /**
     * 시간을 동일하게 맞춘 토큰 생성
     * @param payload tokenPayload
     * @return jwtToken
     */
    public String createToken(TokenPayload payload) {
        return BEARER_PREFIX +
                Jwts.builder()
                        .subject(payload.getSub()) // 사용자 식별자값(ID)
                        .claim(AUTHORIZATION_KEY, payload.getRole()) // 사용자 권한
                        .expiration(payload.getExpiresAt()) // 만료 시간
                        .issuedAt(payload.getIat()) // 발급일
                        .id(payload.getJti()) // JWT ID
                        .signWith(secretKey) // 암호화 Key & 알고리즘
                        .compact();
    }

    /**
     * 토큰의 만료 기간 여부 확인 
     * @param token 대상 토큰 값
     * @return 만료 여부
     */
    public boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    /**
     * 토큰의 유저 정보 반환
     * @param token 대상 토큰 값
     * @return 유저 정보
     */
    public String getUsernameFromToken(String token) {
        return  getClaims(token).getSubject();
    }

    /**
     * 헤더값으로 부터 토큰 값 가져오기
     * @param fromHeader 대상 문자열
     * @return 확인된 토큰값
     */
    public String getAccessToken(String fromHeader) {
        return this.extractToken(fromHeader);
    }

    public String extractToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.contains("%20")) {
            tokenValue = URLDecoder.decode(tokenValue, StandardCharsets.UTF_8).trim();
        }
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(7);
        }

        throw new JwtException(ErrorCode.ACCESS_TOKEN_NOT_FOUND.getMessage());
    }

    /**
     * 토큰 만료일자 파싱
     * @param token 대상 토큰값
     * @return 토큰 만료일자
     */
    public Date getTokenIat(String token) {
        return getClaims(token).getIssuedAt();
    }

    /**
     * 토큰 유효여부 검증
     * @param token 대상 토큰값
     * @return 유효성 여부
     */
    public boolean validateToken(String token) {
        try {
            return !getClaims(token).isEmpty();
        }catch (JwtException e){
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    private Claims getClaims(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 엑세스 토큰 재발급용
    public String getUsernameFromExpiredJwt(ExpiredJwtException exception) {
        return exception.getClaims().getSubject();
    }
}
