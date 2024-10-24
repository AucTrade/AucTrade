package com.example.auctrade.global.auth.filter;

import com.example.auctrade.domain.user.entity.UserRoleEnum;
import com.example.auctrade.domain.user.service.UserService;
import com.example.auctrade.global.auth.service.JwtTokenService;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.example.auctrade.global.constant.Constants.COOKIE_AUTH_HEADER;

@RequiredArgsConstructor
@Slf4j(topic = "JwtAuthenticationFilter")
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final UserService userService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.info("인증 시도");
        String beforeToken = findAccessToken(request.getCookies());

        try {
//            log.info("before access token: {}", beforeToken);

            // 엑세스토큰 유효기간 만료시 바로 JwtException 발생
            // 그로 인해 JwtException 필터에서 곧바로 로그인 화면으로 내보내는 것
            // 즉, 엑세스토큰 유효기간 만료시, 리프레쉬 토큰을 기반으로 한 재발급 절차 추가가 필요

            if(beforeToken == null) throw new JwtException(ErrorCode.ACCESS_TOKEN_NOT_FOUND.getMessage());

//            log.info("토큰 만료 확인 절차를 위한 임의 로그");

            String accessToken = jwtTokenService.validAccessToken(beforeToken);

//            log.info("validated access token: {}", accessToken);

            String tokenValue = jwtTokenService.extractValue(accessToken);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(createAuthentication(jwtTokenService.getUsernameFromToken(tokenValue)));
            SecurityContextHolder.setContext(context);

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException ex) {
//            log.error("JwtAuthenticationFilter 엑세스 토큰 만료");

            String username = jwtTokenService.getUsernameFromExpiredJwt(ex);
//            log.info("Expired Jwt username: {}", username);

            if (jwtTokenService.getRefreshToken(username) != null) {

                UserRoleEnum role = userService.getUserInfo(username).getRole();
//                log.info("Expired Jwt user role: {}", role.getRole());

                String newAccessToken = jwtTokenService.generateNewToken(username, role);
//                log.info("New access token: {}", newAccessToken);

                String encodedToken = URLEncoder.encode(newAccessToken, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

                Cookie cookie = new Cookie(COOKIE_AUTH_HEADER, encodedToken);
                cookie.setPath("/");
                response.addCookie(cookie);

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(createAuthentication(username));
                SecurityContextHolder.setContext(context);

                filterChain.doFilter(request, response);
            } else {
//                log.error("JwtAuthenticationFilter 리프레시 토큰 없음");
                throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
            }
        }
    }

    // Authentication 객체 생성 (UPAT 생성)
    private Authentication createAuthentication(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private String findAccessToken(Cookie[] cookies){
        if(cookies == null) return null;

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(COOKIE_AUTH_HEADER)) return cookie.getValue();
        }
        return null;
    }
}

