package com.example.auctrade.global.auth.vo;

import com.example.auctrade.domain.user.entity.UserRoleEnum;
import lombok.Getter;

import java.util.Date;

@Getter
public class TokenPayload {
    private final String sub;
    private final String jti;
    private final String role;
    private final Date iat;
    private final Date expiresAt;

    public TokenPayload(String sub, String jti, Date iat, Date expiresAt, UserRoleEnum role) {
        this.sub = sub;
        this.jti = jti;
        this.role = role.getRole();
        this.iat = iat;
        this.expiresAt = expiresAt;
    }
}
