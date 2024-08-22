package com.example.auctrade.global.auth.service;

import com.example.auctrade.domain.user.entity.UserRoleEnum;

public interface JwtTokenService {
    String generateNewToken(String email, UserRoleEnum roleEnum);
    String validAccessToken(String tokenValue);
    String extractValue(String token);
    String getUsernameFromToken(String token);
}
