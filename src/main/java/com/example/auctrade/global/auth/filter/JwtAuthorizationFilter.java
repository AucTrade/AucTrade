package com.example.auctrade.global.auth.filter;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Slf4j(topic = "JwtAuthorizationFilter")
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 유저 , 관리자, 들어온 사람, 권한을 가져오는 메서드
       //권한을 체크하는 로직
        filterChain.doFilter(request, response);
    }
}
