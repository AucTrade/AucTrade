package com.example.auctrade.global.auth.util;

import com.example.auctrade.domain.user.entity.UserRoleEnum;
import com.example.auctrade.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
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
import java.util.*;

@Component
@Slf4j
public class JwtUtil {
    private final String AUTHORIZATION_HEADER = "Authorization";
    private final String AUTHORIZATION_KEY = "auth";
    private final String BEARER_PREFIX = "Bearer ";
    private final long HOUR_SECONDS = 60 * 60 * 1000L;
    private final long DAY_SECONDS = 24 * 60 * 60 * 1000L;
    private final SecretKey secretKey;

    // 로그 세팅
    public static final Logger logger = LoggerFactory.getLogger("jwt 발급 및 처리 로직");

    public JwtUtil(@Value("${jwt.secret.key}") String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    /**
     * 분산 서버 환경에서 시간을 동일하게 맞춰주기 위한 동시 생성 메소드 <br>
     * 인덱스 0: accessTokenPayload <br>
     * 인덱스 1: refreshTokenPayload <br>
     * @param email 요청한 회원 이메일
     * @param role 요청한 회원 권한
     * @return tokenPayloads
     */
    public List<TokenPayload> createTokenPayloads(String email, UserRoleEnum role) {
        List<TokenPayload> tokenPayloads = new ArrayList<>();
        Date date = new Date();
        tokenPayloads.add(createTokenPayload(email, date, HOUR_SECONDS, role));
        tokenPayloads.add(createTokenPayload(email, date, 7*DAY_SECONDS, role));
        return tokenPayloads;
    }

    private TokenPayload createTokenPayload(String email,Date date,long seconds,UserRoleEnum role){
        return  new TokenPayload(email,UUID.randomUUID().toString(),date,new Date(date.getTime()+seconds),role);
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
    // 토큰이 만료되었는지 확인하는 메서드
    public boolean isTokenExpired(String token) {
        // 서명을 검증하지 않고 클레임을 파싱
        logger.info("아직 토큰 만료되지 않음");
        return getClaims(token).getExpiration().before(new Date());
    }

    /**
     * 토큰의 유저 정보 반환
     * @param token 대상 토큰 값
     * @return 유저 정보
     */
    public String getUsernameFromToken(String token) {
        // 만료된 토큰에서 클레임을 파싱하되 서명 검증은 생략
        logger.info("기한이 아직 남은 토큰으로부터 이메일 추출");
        return  getClaims(token).getSubject();
    }

    /**
     * 쿠키에 토큰 저장
     * @param token 대상 토큰 값
     * @param res 저장 대상 response
     */
    public void addJwtToCookie(String token, HttpServletResponse res) {
        logger.info("쿠키에 엑세스 토큰이 저장됨: {}", token);
        token = URLEncoder.encode(token, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        Cookie cookie = new Cookie(AUTHORIZATION_HEADER, token); // Name-Value
        cookie.setPath("/");
        res.addCookie(cookie);
    }

    /**
     * 쿠키에 토큰 삭제
     * @param token 대상 토큰 값
     * @param res 삭제 대상 response
     */
    public void removeJwtToCookie(String token, HttpServletResponse res) {
        logger.info("쿠키에 엑세스 토큰이 저장됨: {}", token);
        token = URLEncoder.encode(token, StandardCharsets.UTF_8).replaceAll("\\+", "%20"); // Cookie Value 에는 공백이 불가능해서 encoding 진행
        Cookie cookie = new Cookie(AUTHORIZATION_HEADER, token); // Name-Value
        cookie.setPath("/");

        // Response 객체에 Cookie 추가
        res.addCookie(cookie);
    }
    /**
     * 쿠키 정보로 부터 토큰 값 가져오기
     * @param req 대상 request
     * @return 확인된 토큰값
     */
    public String getAccessToken(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();

        if(cookies == null)
            throw new JwtException(ErrorCode.ACCESS_TOKEN_NOT_FOUND.getMessage());

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(AUTHORIZATION_HEADER))
                return extractToken(URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8));
        }
        log.info("토큰 없음");
        throw new JwtException(ErrorCode.ACCESS_TOKEN_NOT_FOUND.getMessage());
    }
    /**
     * 헤더값으로 부터 토큰 값 가져오기
     * @param fromHeader 대상 문자열
     * @return 확인된 토큰값
     */
    public String getAccessToken(String fromHeader) {
        return extractToken(fromHeader);
    }

    private String extractToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX))
            return tokenValue.substring(7);

        throw new JwtException(ErrorCode.ACCESS_TOKEN_NOT_FOUND.getMessage());
    }
    public boolean validRefreshToken(String access, String refresh){
        return refresh != null && getClaims(refresh).getIssuedAt().equals(getClaims(access).getIssuedAt());
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
        return !getClaims(token).isEmpty();
    }

    public String getRefreshTokenKey(){
        return "REFRESH_TOKEN:";
    }
    public String getAuthorizationHeader(){
        return AUTHORIZATION_HEADER;
    }
    private Claims getClaims(String token){
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
    }
}
