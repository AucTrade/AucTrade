package com.example.auctrade.chat;

import com.example.auctrade.domain.chat.dto.MessageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

// 서버 포트번호 랜덤 설정, @LocalServerPort 기반 할당
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketTest {

    // 로그 기록
    private final Logger log = LoggerFactory.getLogger(getClass());

    @LocalServerPort
    private int port;

    private String url;
    private StompHeaders stompHeaders;

    private WebSocketStompClient getStompClient() {
        // WebSocketStompClient 인스턴스 설정
        // SockJS 사용 고려
        WebSocketStompClient stompClient =
                new WebSocketStompClient(new SockJsClient(List.of(
                        new WebSocketTransport(new StandardWebSocketClient()))));

        // JSON 직렬화 및 역직렬화를 위한 converter 설정
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        return stompClient;
    }

    // 랜덤 포트 할당
    @BeforeEach
    void setUp() {
        this.url = String.format("ws://localhost:%d/stomp/chat", port);
    }

    // 웹소켓 연결 확인
    @Test
    void testWebSocketConnection() throws Exception {
        // given & when, then
        WebSocketStompClient stompClient = getStompClient();
        StompSession session = stompClient.connectAsync(
                url, new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);

        log.info("세션 연결: {}", session.isConnected());

        assertTrue(session.isConnected(), "WebSocket 연결 성공");
    }
}