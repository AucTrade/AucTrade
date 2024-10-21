package com.example.auctrade.global.message;

import com.example.auctrade.global.auth.util.JwtUtil;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeFailureException;
import org.springframework.web.socket.server.HandshakeHandler;

import java.util.List;
import java.util.Map;

@Slf4j(topic = "웹소켓 핸드쉐이킹")
@RequiredArgsConstructor
@Component
public class CustomHandshakeHandler implements HandshakeHandler {

    private final String COOKIE_START = "Authorization=Bearer%20";

    private final JwtUtil jwtUtil;

    @Override
    public boolean doHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws HandshakeFailureException {

        try {
            List<String> cookies = request.getHeaders().get(HttpHeaders.COOKIE);

            if (cookies != null) {
                for (String cookie : cookies) {
                    // 쿠키 문자열에서 Authorization 값을 추출하기
                    if (cookie.startsWith(COOKIE_START)) {
                        String token = cookie.substring(COOKIE_START.length());
                        log.info("핸드쉐이킹 과정의 엑세스토큰: {}", token);

                        if (jwtUtil.validateToken(token)) {
                            // 핸드쉐이크 성공
                            log.info("핸드쉐이킹 과정 검증 결과, 유효한 토큰");
                            return true;
                        } else {
                            // 핸드쉐이크 실패
                            log.error("핸드쉐이킹 과정 검증 결과, 유효하지 않은 토큰!");
                            throw new CustomException(ErrorCode.INVALID_AUTH_TOKEN);
                        }
                    }
                }
            }
            if (cookies == null || cookies.isEmpty()) {
                log.error("No cookies found in request headers!");
                return false;
            }
        } catch (Exception ex) {
            log.error("오류 발생: {}", ex.getMessage(), ex);
        }

        return false;
    }
}
