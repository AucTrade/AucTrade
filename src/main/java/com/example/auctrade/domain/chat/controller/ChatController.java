package com.example.auctrade.domain.chat.controller;


import com.example.auctrade.domain.chat.dto.MessageDTO;
import com.example.auctrade.domain.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
public class ChatController {

    private final SimpMessageSendingOperations sendingOperations;
    private final ChatMessageService chatMessageService;

    @MessageMapping(value = "/chat/enter")
    public void enter(MessageDTO message){
        message.setMessage(message.getUsername() + "님이 채팅방에 참여하였습니다.");

        if (chatMessageService.saveChatMessage(message) != null)
            sendingOperations.convertAndSend(
                    "/sub/chat/room/" + message.getAuctionId(), message);
    }

    @MessageMapping(value = "/chat/message")
    public void message(MessageDTO message){

//        if (message.getMessage().charAt(0) == '@'){
//            long price = Long.parseLong(message.getMessage().substring(1));
//            // postgreSQL 경매 플로우 로직 할당 시작(1차)
//            // 원칙적으로는 디스패처 서블릿을 지나기 전에 처리하는 것이 옳기 때문에
//            // 카프카나 토끼 같은 외부 메세지 브로커를 활용하는 것이 바람직하다고 생각함
//            // 결국 인터셉터 등을 활용하는 것 역시 서버단의 코드 로직 겹침이 일어나기 때문
//        }

        if (chatMessageService.saveChatMessage(message) != null)
            sendingOperations.convertAndSend(
                    "/sub/chat/room/" + message.getAuctionId(), message);
    }
}