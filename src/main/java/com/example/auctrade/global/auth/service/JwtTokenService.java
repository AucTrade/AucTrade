package com.example.auctrade.global.auth.service;

import com.example.auctrade.domain.user.entity.UserRoleEnum;
import io.jsonwebtoken.ExpiredJwtException;

public interface JwtTokenService {
    String generateNewToken(String email, UserRoleEnum roleEnum);
    String validAccessToken(String tokenValue);
    String extractValue(String token);
    String getUsernameFromToken(String token);
    String getUsernameFromExpiredJwt(ExpiredJwtException expiredJwtException);

    // 리프레쉬 토큰 반환 메소드
    String getRefreshToken(String email);
}
