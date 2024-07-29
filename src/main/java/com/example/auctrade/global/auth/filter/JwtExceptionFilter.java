package com.example.auctrade.global.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@AllArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        try {
            chain.doFilter(req, res); // go to 'JwtAuthenticationFilter'
        }  catch (SecurityException | MalformedJwtException | SignatureException | UnsupportedJwtException | IllegalArgumentException ex) {
            setErrorResponse(HttpStatus.UNAUTHORIZED, res, ex);
        }
    }

    public void setErrorResponse(HttpStatus status, HttpServletResponse res, Throwable ex) throws IOException {
        res.setStatus(status.value());
        res.setContentType("application/json; charset=UTF-8");

//        ApiMessageDto apiMessageDto = new ApiMessageDto(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
//        String json = objectMapper.writeValueAsString(apiMessageDto);
        res.getWriter().write("토큰 관련 예외 발생으로 인한 필터 처리");
    }
}
