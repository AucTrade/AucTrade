package com.example.auctrade.controller.api;


import com.example.auctrade.dto.MessageDTO;
import com.example.auctrade.service.ChatMessageService;
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
        if(chatMessageService.saveMsg(message) != null)
            sendingOperations.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
    }

    @MessageMapping(value = "/chat/message")
    public void message(MessageDTO message){
        if(message.getMessage().charAt(0) == '@'){
            long price = Long.parseLong(message.getMessage().substring(1));
        }
        if(chatMessageService.saveMsg(message) != null)
            sendingOperations.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
    }
}