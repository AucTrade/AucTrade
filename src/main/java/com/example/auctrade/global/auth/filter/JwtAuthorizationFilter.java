package com.example.auctrade.global.auth.filter;

import com.example.auctrade.domain.user.dto.UserDTO;
import com.example.auctrade.domain.user.service.UserService;
import com.example.auctrade.global.auth.util.JwtUtil;
import com.example.auctrade.global.auth.util.TokenPayload;
import com.example.auctrade.global.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "JWT 검증 및 인가")
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String accessToken = jwtUtil.getAccessToken(request);
        String email = jwtUtil.getUsernameFromToken(accessToken);
        String refreshToken = redisTemplate.opsForValue().get(jwtUtil.getRefreshTokenKey()+email);

        //2-2. 유효성 판별
        if(!jwtUtil.validRefreshToken(accessToken, refreshToken))
            throw new InternalAuthenticationServiceException(ErrorCode.INVALID_REFRESH_TOKEN.getMessage());

        //구현 위치를 어디로 설정하는 것이 좋은가?

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(createAuthentication(email));
        SecurityContextHolder.setContext(context);

        //2-3. 만약 만료된 토큰 -> 재발급 , 만료되지 않은 경우 -> 해더에 정보 저장 후 진행
        if (jwtUtil.isTokenExpired(accessToken))
            jwtUtil.addJwtToCookie(reissuanceRefresh(email), response);

        // 다음 필터로 넘어가라는 의미
        // 이걸 이용해서 리프레쉬 토큰에 대한 로직 짜면 될 것 같은데
        filterChain.doFilter(request, response);
    }

    // Authentication 객체 생성 (UPAT 생성)
    private Authentication createAuthentication(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private String reissuanceRefresh(String email){
        UserDTO.Login userInfo = userService.getUserInfo(email);
        // 인덱스 0: accessTokenPayload, 인덱스 1: refreshTokenPayload
        List<TokenPayload> tokenPayloads = jwtUtil.createTokenPayloads(userInfo.getEmail(), userInfo.getRole());

        // 새로운 리프레쉬토큰 업데이트(7일 + 1시간을 시한으로 설정)
        String newRefreshToken = jwtUtil.createToken(tokenPayloads.get(1)).substring(7);
        redisTemplate.opsForValue()
                .set(jwtUtil.getRefreshTokenKey() + email, newRefreshToken, 24 * 7 + 1L, TimeUnit.HOURS);

        return jwtUtil.createToken(tokenPayloads.get(0));
    }
}
