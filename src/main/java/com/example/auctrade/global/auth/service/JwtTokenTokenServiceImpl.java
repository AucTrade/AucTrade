package com.example.auctrade.global.auth.service;

import com.example.auctrade.domain.user.dto.UserDto;
import com.example.auctrade.domain.user.entity.UserRoleEnum;
import com.example.auctrade.domain.user.service.UserService;
import com.example.auctrade.global.auth.util.JwtUtil;
import com.example.auctrade.global.auth.vo.TokenPayload;
import com.example.auctrade.global.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.example.auctrade.global.constant.Constants.REDIS_REFRESH_KEY;

@Service
@Slf4j(topic = "Jwt Service")
@RequiredArgsConstructor
public class JwtTokenTokenServiceImpl implements JwtTokenService {
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String,String> redisTemplate;
    private final UserService userService;
    private final long ACCESS_TOKEN_EAT = 60 * 60 * 1000L; // 1H
    private final long REFRESH_TOKEN_EAT = 7 * 24 * 60 * 60 * 1000L; // 7D

    /**
     * 로그인 인증이 끝난 후 신규 토큰 발행
     * @param email 요청한 회원 이메일
     * @return 새로 발급한 토큰 정보
     */
    @Override
    public String generateNewToken(String email, UserRoleEnum role){

        Date date = new Date();

//        if (redisTemplate.opsForValue().get(email) != null)
//            throw new InternalAuthenticationServiceException(ErrorCode.USER_ALREADY_LOGGED_IN.getMessage());

        String tokenValue = jwtUtil.createToken(createTokenPayload(email, date, REFRESH_TOKEN_EAT, role)).substring(7);
        //리프레시 토큰 발행
        redisTemplate.opsForValue().set(REDIS_REFRESH_KEY+email, tokenValue, REFRESH_TOKEN_EAT, TimeUnit.MILLISECONDS);

        return jwtUtil.createToken(createTokenPayload(email, date, ACCESS_TOKEN_EAT, role));
    }

    /**
     * 입력 받은 엑세스 토큰 확인
     * @param token 대상 토큰 값
     * @return 기존 또는 갱신된 토큰
     */
    @Override
    public String validAccessToken(String token){
//        log.info("파라미터 토큰: {}", token);

        String accessToken = extractValue(token);
        String email = getUsernameFromToken(accessToken);

//        log.info("토큰 만료 확인 절차를 위한 임의 로그 2");
//
//        if(isExpired(accessToken) && isExpired(redisTemplate.opsForValue().get(REDIS_REFRESH_KEY+email)))
//            throw new JwtException(ErrorCode.INVALID_REFRESH_TOKEN.getMessage());

        // 비정상적인 인증 시도(공격자의 시도 등의 시나리오) 등에 대한 예외 로직 작성 위치
//        log.info("토큰 만료 확인 절차를 위한 임의 로그 3");

//        if (isExpired(accessToken)){
////            log.info("엑세스토큰(만) 완료로 인한 엑세스 토큰 재발급");
//            UserDTO.Info user = userService.getUserInfo(email);
//            return jwtUtil.createToken(createTokenPayload(email, new Date(), ACCESS_TOKEN_EAT, user.getRole()));
//        }

        return token;
    }

    /**
     * 토큰의 유저 이메일 반환
     * @param token 대상 토큰 값
     * @return 유저 이메일
     */
    // 이메일을 추출하려는 과정에서 토큰 파싱 검증이 일어나고
    // 만료기간이 지난 엑세스토큰이라서 클레임 관련 예외인 JwtException 을 던지는 것
    @Override
    public String getUsernameFromToken(String token) {
//        log.info("이메일 추출을 위한 토큰 파싱 작업: {}", token);

        String email = jwtUtil.getUsernameFromToken(token);

        if(!userService.existUserEmail(email))
            throw new JwtException(ErrorCode.INVALID_AUTH_TOKEN.getMessage());

        return email;
    }

    // 엑세스 토큰 재발급용 메소드
    @Override
    public String getUsernameFromExpiredJwt(ExpiredJwtException exception) {
        return jwtUtil.getUsernameFromExpiredJwt(exception);
    }

    /**
     * 토큰의 유저 정보 반환
     * @param token 대상 토큰 값
     * @return 유저 정보
     */
    public UserDto.Info getUserFromToken(String token) {
        return userService.getUserInfo(jwtUtil.getUsernameFromToken(token));
    }
    /**
     * 토큰 값 디코딩 및 추출
     * @param token 토큰이 있는 데이터
     * @return 추출한 토큰 값
     */
    @Override
    public String extractValue(String token){
//        log.info("토큰 서비스에서의 토큰 추출 확인");
        return jwtUtil.extractToken(URLDecoder.decode(token, StandardCharsets.UTF_8));
    }

    private boolean isExpired(String token) {
        return jwtUtil.isTokenExpired(token);
    }

    private TokenPayload createTokenPayload(String email,Date date,long seconds,UserRoleEnum role){
        return  new TokenPayload(email,UUID.randomUUID().toString(),date,new Date(date.getTime()+seconds),role);
    }

    // 리프레쉬 토큰 반환 메소드
    @Override
    public String getRefreshToken(String email) {
        String refreshToken  = redisTemplate.opsForValue().get(REDIS_REFRESH_KEY+email);

        // 유효성 검증 로직 추후 추가

//        log.info("{}의 refresh token: {}", email, refreshToken);
        return refreshToken;
    }
}