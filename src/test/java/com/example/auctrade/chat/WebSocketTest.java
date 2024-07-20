package com.example.auctrade.chat;

import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.List;

// 서버 포트번호 랜덤 설정, @LocalServerPort 기반 할당
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketTest {

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

}