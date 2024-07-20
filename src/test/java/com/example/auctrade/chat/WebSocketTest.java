package com.example.auctrade.chat;

import com.example.auctrade.domain.chat.dto.MessageDTO;
import com.example.auctrade.domain.chat.service.ChatMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// 서버 포트번호 랜덤 설정, @LocalServerPort 기반 할당
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketTest {

    // 로그 기록
    private final Logger log = LoggerFactory.getLogger(getClass());

    @LocalServerPort
    private int port;

    private String url;

    // 채팅 메세지 서비스 모킹 처리
    @MockBean
    private ChatMessageService chatMessageService;

    // 향후 인증을 구현할 때를 대비한 코드
//    private StompHeaders stompHeaders;

    // StompClient 인스턴스 생성 메소드
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

    // 메세지 임의 설정 메소드
    private MessageDTO getMessageDTO() {
        MessageDTO message = new MessageDTO();

        message.setId("1L");
        message.setAuctionId(1L);
        message.setUsername("Kim Dong Jun");
        message.setMessage("테스트 성공");
        message.setCreatedAt(LocalDateTime.now().toString());

        return message;
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

        assertTrue(session.isConnected(), "WebSocket 연결 성공");
    }

    // 웹소켓 연결 후 메세지 송수신 처리
    @Test
    void testChatMessage() throws Exception {
        // given
        WebSocketStompClient stompClient = getStompClient();
        StompSession session = stompClient.connectAsync(
                url, new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);

        log.info("세션 연결: {}", session.isConnected());

        // when
        // 메세지 저장 서비스 모킹 처리
        MessageDTO mockMessage = getMessageDTO();
        when(chatMessageService.saveChatMessage(any(MessageDTO.class))).thenReturn(mockMessage);
        log.info("모킹 메세지: {}", mockMessage.getMessage());

        // 메세지 수신 비동기 작업 처용 CompletableFuture 인스턴스
        // 메세지 수신되면 완료
        CompletableFuture<MessageDTO> future = new CompletableFuture<>();

        // 구독 헤더 설정
        StompHeaders subscribeHeaders = new StompHeaders();
        subscribeHeaders.setDestination("/sub/chat/room/" + mockMessage.getAuctionId());

        // stomp 구독 설정 및 메세지 수신 설정
        session.subscribe(subscribeHeaders, new StompSessionHandlerAdapter() {
            // 페이로드 역직렬화
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MessageDTO.class;
            }

            // 메세지 get
            // CompletableFuture 인스턴스 메세지 수신 완료 처리
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                future.complete((MessageDTO) payload);
            }
        });

        // 전송 헤더 설정
        // 구독과 전송에는 각기 다른 목적의 StompHeaders 인스턴스'들'이 쓰임
        StompHeaders sendHeaders = new StompHeaders();
        sendHeaders.setDestination("/send/chat/message");
        session.send(sendHeaders, mockMessage);

        // 메시지 수신 확인(내용이 같은지)
        MessageDTO receivedMessage = future.get(5, TimeUnit.SECONDS);
        assertEquals(mockMessage.getMessage(), receivedMessage.getMessage(), "테스트 성공");
    }
}