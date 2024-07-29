package com.example.auctrade.global.auth.filter;

import com.example.auctrade.domain.user.dto.UserDTO;
import com.example.auctrade.domain.user.entity.UserDetailsImpl;
import com.example.auctrade.domain.user.entity.UserRoleEnum;
import com.example.auctrade.domain.user.repository.UserRepository;
import com.example.auctrade.global.auth.util.JwtUtil;
import com.example.auctrade.global.auth.util.TokenPayload;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "로그인 및 JWT 생성 + 인증")
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtil jwtUtil; // 로그인 성공 시, 존맛탱 발급을 위한 의존성 주입
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisRefreshToken;


    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository, RedisTemplate<String, String> redisRefreshToken) {
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/api/users/login"); // 로그인 처리 경로 설정(매우매우 중요)
        super.setUsernameParameter("email");
        this.userRepository = userRepository;
        this.redisRefreshToken = redisRefreshToken;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // 로그인 시도를 담당함
        log.info("로그인 단계 진입");

        try {
            UserDTO.Login requestDto = new ObjectMapper().readValue(request.getInputStream(), UserDTO.Login.class);
            return getAuthenticationManager().authenticate( // 인증 처리 하는 메소드
                    new UsernamePasswordAuthenticationToken(
                            requestDto.getEmail(),
                            requestDto.getPassword(),
                            null
                    )
            );
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        log.info("로그인 성공 및 JWT 생성");

        String username = ((UserDetailsImpl) authResult.getPrincipal()).getUsername();
        UserRoleEnum role = ((UserDetailsImpl) authResult.getPrincipal()).getUser().getRole();

        // 신 버전
        // 인덱스 0: accessTokenPayload, 인덱스 1: refreshTokenPayload
        List<TokenPayload> tokenPayloads = jwtUtil.createTokenPayloads(username, role);

        String accessToken = jwtUtil.createAccessToken(tokenPayloads.get(0));
        String refreshToken = jwtUtil.createRefreshToken(tokenPayloads.get(1));
        String refreshTokenKey = JwtUtil.REFRESH_TOKEN_KEY + username;

        userRepository.findByEmail(username).orElseThrow(
                () ->  new CustomException(ErrorCode.USER_ID_MISMATCH)
        );

        if (redisRefreshToken.opsForValue().get(username) != null) {
            throw new CustomException(ErrorCode.USER_ALREADY_LOGGED_IN);
        }

        String refreshTokenValue = refreshToken.substring(7);
        log.info("초기 리프레쉬토큰: " + refreshTokenValue);

        // username(email) - refreshToken 덮어씌우기 저장
        // 7일 + 1시간을 시한으로 설정
        long expirationTime = 24 * 7 + 1;
        redisRefreshToken.opsForValue().set(refreshTokenKey, refreshTokenValue, expirationTime, TimeUnit.HOURS);

        jwtUtil.addJwtToCookie(accessToken, response);
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json;charset=UTF-8");

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonMessage = objectMapper.writeValueAsString("로그인 성공 및 토큰 발급");
        PrintWriter writer = response.getWriter();

        writer.print(jsonMessage);
        writer.flush();
        writer.close();
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        log.info("로그인 실패 및 401 에러 반환");

        // 로그인 실패로 상태코드 반환
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonMessage = objectMapper.writeValueAsString("로그인 실패");
        PrintWriter writer = response.getWriter();

        writer.print(jsonMessage);
        writer.flush();
        writer.close();
    }
}
