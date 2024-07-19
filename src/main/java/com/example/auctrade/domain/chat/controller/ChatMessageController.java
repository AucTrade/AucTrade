package com.example.auctrade.domain.chat.controller;


import com.example.auctrade.domain.auction.service.BidLogService;
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
public class ChatMessageController {

    private final SimpMessageSendingOperations sendingOperations;
    private final ChatMessageService chatMessageService;
    private final BidLogService bidLogService;

    @MessageMapping(value = "/chat/enter")
    public void enter(MessageDTO message){
        message.setMessage(message.getUsername() + "님이 채팅방에 참여하였습니다.");

        if (chatMessageService.saveChatMessage(message) != null)
            sendingOperations.convertAndSend(
                    "/sub/chat/room/" + message.getAuctionId(), message);
    }

    @MessageMapping(value = "/chat/message")
    public void message(MessageDTO message){
        //TODO: 메세지가 텅비었다면 리턴(유효성 검증 추가 가능)
        if (chatMessageService.saveChatMessage(message) == null) {
            return;
        }

        // 입찰 메세지일 경우, 입찰 로그 기록과 관련된 서비스 로직으로 넘어간다
        if (message.getMessage().charAt(0) == '@'){
            bidLogService.updateBidPrice(message);
        }

        sendingOperations.convertAndSend(
                "/sub/chat/room/" + message.getAuctionId(), message);
    }
}