package com.example.auctrade.domain.chat.controller;

import com.example.auctrade.domain.chat.dto.MessageDTO;
import com.example.auctrade.domain.chat.service.ChatMessageServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

import static com.example.auctrade.global.constant.Constants.CHAT_AUCTION_DESTINATION;

@RestController
@RequiredArgsConstructor
@Log4j2
public class ChatMessageController {

    private final SimpMessageSendingOperations sendingOperations;
    private final ChatMessageServiceImpl chatMessageService;

    @MessageMapping(value = "/chat/enter")
    public void enter(MessageDTO.Create message, Principal principal){
        log.info("{} 채팅방 입장", principal.getName());
        message.setUsername(principal.getName());
        message.setMessage(principal.getName() + " 님이 채팅방에 입장하였습니다.");

        MessageDTO.Get responseDto = chatMessageService.saveChatMessage(message);
        if( responseDto != null)
            sendingOperations.convertAndSend(CHAT_AUCTION_DESTINATION + message.getAuctionId(), responseDto);
    }


    @MessageMapping(value = "/chat/message")
    public void message(MessageDTO.Create message, Principal principal){
        log.info("{} 메세지 전송: {}", principal.getName(), message.getMessage());
        message.setUsername(principal.getName());

        sendingOperations.convertAndSend(CHAT_AUCTION_DESTINATION + message.getAuctionId(), message);
    }
}