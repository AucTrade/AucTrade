package com.example.auctrade.domain.chat.controller;

import com.example.auctrade.domain.chat.dto.AuctionMessageDto;
import com.example.auctrade.domain.chat.service.AuctionChatMessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static com.example.auctrade.global.constant.Constants.CHAT_AUCTION_DESTINATION;

@RestController
public class AuctionChatMessageController {

    private final SimpMessageSendingOperations sendingOperations;
    private final AuctionChatMessageService auctionChatMessageService;

    public AuctionChatMessageController(SimpMessageSendingOperations sendingOperations, AuctionChatMessageService auctionChatMessageService){
        this.sendingOperations = sendingOperations;
        this.auctionChatMessageService = auctionChatMessageService;
    }

    @MessageMapping(value = "/chat/message/enter")
    public void enter(AuctionMessageDto.Enter message, @AuthenticationPrincipal UserDetails userDetails){
        AuctionMessageDto.Get auctionMessageDto = auctionChatMessageService.createEnterChatMessage(message.getAuctionId(), userDetails.getUsername());

        if(auctionMessageDto != null)
            sendingOperations.convertAndSend(CHAT_AUCTION_DESTINATION + auctionMessageDto.getAuctionId(), auctionMessageDto);
    }


    @MessageMapping(value = "/chat/message")
    public void message(AuctionMessageDto.Create message, @AuthenticationPrincipal UserDetails userDetails){
        AuctionMessageDto.Get auctionMessageDto = auctionChatMessageService.createChatMessage(message, userDetails.getUsername());

        if(auctionMessageDto != null)
            sendingOperations.convertAndSend(CHAT_AUCTION_DESTINATION + message.getAuctionId(), auctionMessageDto);
    }
}