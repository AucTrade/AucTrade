package com.example.auctrade.global.auth.filter;

import com.example.auctrade.domain.user.dto.UserDTO;
import com.example.auctrade.domain.user.entity.UserDetailsImpl;
import com.example.auctrade.domain.user.entity.UserRoleEnum;
import com.example.auctrade.domain.user.service.UserService;
import com.example.auctrade.global.auth.util.JwtUtil;
import com.example.auctrade.global.auth.util.TokenPayload;
import com.example.auctrade.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "로그인 및 JWT 생성")
public class CustomLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final RedisTemplate<String, String> redisTemplate;


    public CustomLoginFilter(JwtUtil jwtUtil, UserService userService, RedisTemplate<String, String> redisTemplate) {
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/api/users/login"); // 로그인 처리 경로 설정(매우매우 중요)
        super.setUsernameParameter("email");
        this.userService = userService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        log.info("로그인 단계 진입");
        try {
            UserDTO.Login requestDto = new ObjectMapper().readValue(request.getInputStream(), UserDTO.Login.class);
            // 인증 처리 하는 메소드
            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDto.getEmail(),
                            requestDto.getPassword(),
                            null
                    ));
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new InternalAuthenticationServiceException(e.getMessage());
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult){
        log.info("로그인 성공 및 JWT 생성");

        String username = ((UserDetailsImpl) authResult.getPrincipal()).getUsername();
        UserRoleEnum role = ((UserDetailsImpl) authResult.getPrincipal()).getUser().getRole();

        // 신 버전
        // 인덱스 0: accessTokenPayload, 인덱스 1: refreshTokenPayload
        List<TokenPayload> tokenPayloads = jwtUtil.createTokenPayloads(username, role);

        if(!userService.existUserEmail(username))
            throw new UsernameNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage());

        if (redisTemplate.opsForValue().get(username) != null)
            throw new InternalAuthenticationServiceException(ErrorCode.USER_ALREADY_LOGGED_IN.getMessage());

        String refreshTokenValue = jwtUtil.createToken(tokenPayloads.get(1)).substring(7);
        log.info("초기 리프레쉬토큰: " + refreshTokenValue);

        // username(email) - refreshToken 덮어씌우기 저장
        // 7일 + 1시간을 시한으로 설정
        redisTemplate.opsForValue()
                .set(jwtUtil.getRefreshTokenKey()+username, refreshTokenValue, 24 * 7 + 1L, TimeUnit.HOURS);

        jwtUtil.addJwtToCookie(jwtUtil.createToken(tokenPayloads.get(0)), response);
        sendResponseMsg(response, HttpStatus.OK.value(),"로그인 성공 및 토큰 발급");
    }


    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception){
        log.info("로그인 실패 및 401 에러 반환");

        String errorMessage = null;
        if (exception instanceof BadCredentialsException) {
            errorMessage = "아이디와 비밀번호를 확인해주세요.";
        } else if (exception instanceof InternalAuthenticationServiceException) {
            errorMessage = "내부 시스템 문제로 로그인할 수 없습니다. 관리자에게 문의하세요.";
        } else if (exception instanceof UsernameNotFoundException) {
            errorMessage = "존재하지 않는 계정입니다.";
        } else {
            errorMessage = "알 수없는 오류입니다.";
        }
        sendResponseMsg(response,HttpStatus.UNAUTHORIZED.value(),errorMessage);
    }

    private void sendResponseMsg(HttpServletResponse response, int statusCode, String msg){
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");
        try {

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", statusCode);
            responseBody.put("data", msg);

            PrintWriter writer = response.getWriter();
            writer.print(new ObjectMapper().writeValueAsString(responseBody));
            writer.flush();
            writer.close();
        } catch(IOException e){
            log.error(e.getMessage());
        }
    }
}
