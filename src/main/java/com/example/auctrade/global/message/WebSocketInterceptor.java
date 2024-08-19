package com.example.auctrade.global.message;

import com.example.auctrade.global.auth.util.JwtUtil;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import static com.example.auctrade.global.constant.Constants.COOKIE_AUTH_HEADER;

@Slf4j(topic = "WebSocketInterceptor")
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userService;

    private static final Logger logger = LoggerFactory.getLogger(WebSocketInterceptor.class);

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            setAuthenticate(accessor);
        }
        return message;
    }

    private void setAuthenticate(final StompHeaderAccessor accessor) {
        String accessToken = jwtUtil.getAccessToken(accessor.getFirstNativeHeader(COOKIE_AUTH_HEADER));
        validateToken(accessToken);

        String email = jwtUtil.getUsernameFromToken(accessToken);

        logger.info("소켓 CONNECT 시도, 유저 이메일 : {}", email);

        Authentication authentication = createAuthentication(email);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        accessor.setUser(authentication);
    }

    private Authentication createAuthentication(final String email) {
        final UserDetails userDetails = userService.loadUserByUsername(email);

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    private void validateToken(String accessToken) {
        if (!jwtUtil.validateToken(accessToken)) {
            throw new CustomException(ErrorCode.INVALID_AUTH_TOKEN);
        }
    }
}
