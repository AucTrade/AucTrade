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
import org.springframework.util.StringUtils;
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

        String accessTokenValue = jwtUtil.getAccessTokenFromRequestCookie(request);

        // 만약 필터를 거쳤는데 토큰이 없다면 login page로 이동
        if(!StringUtils.hasText(accessTokenValue)) {
            response.sendRedirect("/login");
            return;
        }
        
        String accessToken = jwtUtil.getTokenValue(accessTokenValue);
        if(accessToken == null) throw new InternalAuthenticationServiceException("Token Not Found");
        String email = jwtUtil.getUsernameFromAnyToken(accessToken);

        //2-2. 유효성 판별
        if(!checkRefresh(accessToken, email))
            throw new InternalAuthenticationServiceException(ErrorCode.INVALID_REFRESH_TOKEN.getMessage());

        //2-3. 만약 만료된 토큰 -> 재발급 , 만료되지 않은 경우 -> 해더에 정보 저장 후 진행
        try {
            setAuthentication(email);

            if (jwtUtil.isTokenExpired(accessToken))
                jwtUtil.addJwtToCookie(reissuanceRefresh(email), response);

        }catch (Exception e) {
            log.error(e.getMessage());
            throw new InternalAuthenticationServiceException(ErrorCode.INVALID_REFRESH_TOKEN.getMessage());
        }

        // 다음 필터로 넘어가라는 의미
        // 이걸 이용해서 리프레쉬 토큰에 대한 로직 짜면 될 것 같은데
        filterChain.doFilter(request, response);
    }

    // 인증 처리
    public void setAuthentication(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(createAuthentication(username));
        SecurityContextHolder.setContext(context);
    }

    // Authentication 객체 생성
    private Authentication createAuthentication(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        // UPAT 생성
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private boolean checkRefresh(String accessToken, String email){
        String refreshToken = redisTemplate.opsForValue().get(JwtUtil.REFRESH_TOKEN_KEY + email);
        if (refreshToken == null) return false;

        // 발급일자 비교를 통한 블랙리스트 여부 확인
        return jwtUtil.getTokenIat(refreshToken).equals(jwtUtil.getTokenIat(accessToken));
    }
    private String reissuanceRefresh(String email){
        UserDTO.Login userInfo = userService.getUserInfo(email);
        // 인덱스 0: accessTokenPayload, 인덱스 1: refreshTokenPayload
        List<TokenPayload> tokenPayloads = jwtUtil.createTokenPayloads(userInfo.getEmail(), userInfo.getRole());

        // 새로운 리프레쉬토큰 업데이트(7일 + 1시간을 시한으로 설정)
        String newRefreshToken = jwtUtil.createRefreshToken(tokenPayloads.get(1)).substring(7);
        redisTemplate.opsForValue()
                .set(JwtUtil.REFRESH_TOKEN_KEY + email, newRefreshToken, 24 * 7 + 1L, TimeUnit.HOURS);

        return jwtUtil.createAccessToken(tokenPayloads.get(0));
    }
}
