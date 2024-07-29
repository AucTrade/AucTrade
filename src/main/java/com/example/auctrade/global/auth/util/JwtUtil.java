package com.example.auctrade.global.auth.util;

import com.example.auctrade.domain.user.entity.UserRoleEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtUtil {
    // Request 에서 받을 KEY 값
    public static final String AUTHORIZATION_HEADER = "Authorization";
    // 사용자 권한 값의 KEY
    public static final String AUTHORIZATION_KEY = "auth";
    // Token 식별자
    public static final String BEARER_PREFIX = "Bearer ";
    // 리프레쉬 토큰 저장용 redis 키 식별자
    public static final String REFRESH_TOKEN_KEY = "REFRESH_TOKEN:";

    private final SecretKey secretKey;

    // 로그 세팅
    public static final Logger logger = LoggerFactory.getLogger("jwt 발급 및 처리 로직");

    public JwtUtil(@Value("${jwt.secret.key}") String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    // 분산 서버 환경에서 시간을 동일하게 맞춰주기 위한 동시 생성 메소드
    // 인덱스 0: accessTokenPayload, 인덱스 1: refreshTokenPayload
    public List<TokenPayload> createTokenPayloads(String email, UserRoleEnum role) {
        List<TokenPayload> tokenPayloads = new ArrayList<>();
        Date date = new Date();

        // Access 토큰 만료시간
        // 1시간
        long ACCESS_TOKEN_TIME = 60 * 60 * 1000L;
        TokenPayload accessTokenPayload = new TokenPayload(
                email,
                UUID.randomUUID().toString(),
                date,
                new Date(date.getTime() + ACCESS_TOKEN_TIME),
                role
        );

        // Refresh 토큰 만료시간
        // 7일
        long REFRESH_TOKEN_TIME = 60 * 60 * 24 * 7 * 1000L;
        TokenPayload refreshTokenPayload = new TokenPayload(
                email,
                UUID.randomUUID().toString(),
                date,
                new Date(date.getTime() + REFRESH_TOKEN_TIME),
                role
        );

        tokenPayloads.add(accessTokenPayload);
        tokenPayloads.add(refreshTokenPayload);

        return tokenPayloads;
    }

    // 11.5 -> 12.3
    public String createAccessToken(TokenPayload payload) {
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

    public String createRefreshToken(TokenPayload payload) {
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
     * 새로 추가된 메소드
     * */
    // 토큰이 만료되었는지 확인하는 메서드
    public boolean isTokenExpired(String token) {
        try {
            // 서명을 검증하지 않고 클레임을 파싱
            logger.info("아직 토큰 만료되지 않음");
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException ex) {
            // 토큰 파싱 중 에러가 발생한 경우
            logger.info("토큰 파싱 결과: {}", ex.getMessage());
            return true;
        }
    }

    // 엑세스토큰의 만료는 예외 발생 대상이 아니므로 별도의 메소드 작성
    public String getUsernameFromAnyToken(String token) {
        try {
            // 만료된 토큰에서 클레임을 파싱하되 서명 검증은 생략
            logger.info("기한이 아직 남은 토큰으로부터 발급시간 추출");
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject(); // username이나 email을 subject로 저장했다고 가정
        } catch (ExpiredJwtException e) {
            // 토큰이 만료되었을 경우 ExpiredJwtException에서 클레임을 추출
            logger.info("기한이 만료된 토큰으로부터 발급시간 추출");
            return e.getClaims().getSubject();
        }
    }

    // 쿠키에 액세스 토큰 저장
    public void addJwtToCookie(String token, HttpServletResponse res) {
        logger.info("쿠키에 엑세스 토큰이 저장됨: {}", token);
        token = URLEncoder.encode(token, StandardCharsets.UTF_8).replaceAll("\\+", "%20"); // Cookie Value 에는 공백이 불가능해서 encoding 진행
        Cookie cookie = new Cookie(AUTHORIZATION_HEADER, token); // Name-Value
        cookie.setPath("/");

        // Response 객체에 Cookie 추가
        res.addCookie(cookie);
    }

    // 쿠키에서 엑세스 토큰 갖고오기
    public String getAccessTokenFromRequestCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();

        if(cookies != null) {
            logger.info("요청으로부터 쿠키가 확인됨");

            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(AUTHORIZATION_HEADER)) {
                    return URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8); // Encode 되어 넘어간 Value 다시 Decode
                }
            }
        }

        return null;
    }

    // jwt 토큰 substring
    public String substringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(7);
        }

        logger.error("Not Found Token");
        throw new NullPointerException("Not Found Token");
    }

    // 토큰 만료일자 파싱
    public Date getTokenIat(String token) {
        try {
            // 만료된 토큰에서 클레임을 파싱하되 서명 검증은 생략
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getIssuedAt(); // username 이나 email 을 subject 로 저장했다고 가정
        } catch (ExpiredJwtException e) {
            // 토큰이 만료되었을 경우 ExpiredJwtException 에서 클레임을 추출
            return e.getClaims().getIssuedAt();
        }
    }
}
