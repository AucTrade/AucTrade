package com.example.auctrade.global.auth.filter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
@Slf4j
@Component
@AllArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        try {
            chain.doFilter(req, res);
        }  catch (JwtException | IllegalArgumentException ex) {
            log.error(ex.getMessage());
            this.invalidateCookies(req, res); // 클라이언트의 쿠키(정확히는 엑세스 토큰) 삭제
            // 추가로 리프레쉬 토큰 삭제 역시 같이 갖춰야 함

            res.setStatus(HttpStatus.UNAUTHORIZED.value());
            res.sendRedirect("/login");
        }
    }

    private void invalidateCookies(HttpServletRequest req, HttpServletResponse res) {
        // 쿠키 전부 비우기
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setMaxAge(0);
                cookie.setValue("");
                cookie.setPath("/");
                res.addCookie(cookie);
            }
        }
    }
}
