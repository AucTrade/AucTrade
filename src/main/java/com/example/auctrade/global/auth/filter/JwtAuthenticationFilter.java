package com.example.auctrade.global.auth.filter;

import com.example.auctrade.domain.user.service.UserService;
import com.example.auctrade.global.auth.service.JwtTokenService;
import com.example.auctrade.global.exception.ErrorCode;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import static com.example.auctrade.global.constant.Constants.COOKIE_AUTH_HEADER;

@Slf4j(topic = "JwtAuthenticationFilter")
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(UserService userService, JwtTokenService jwtTokenService){
        this.userDetailsService = userService;
        this.jwtTokenService = jwtTokenService;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.info("인증 시도");
        String beforeToken = findAccessToken(request.getCookies());
        if(beforeToken == null) throw new JwtException(ErrorCode.ACCESS_TOKEN_NOT_FOUND.getMessage());

        String accessToken = jwtTokenService.vaildAccessToken(beforeToken);

        if(!accessToken.equals(beforeToken)){
            Cookie cookie = new Cookie(COOKIE_AUTH_HEADER, accessToken);
            cookie.setPath("/");
            response.addCookie(cookie);
        }

        String tokenValue = jwtTokenService.extractValue(accessToken);
        
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(createAuthentication(jwtTokenService.getUsernameFromToken(tokenValue)));
        SecurityContextHolder.setContext(context);

        filterChain.doFilter(request, response);
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

