package com.example.auctrade.domain.chat.dto;

import com.example.auctrade.domain.chat.document.AuctionChatMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class MessageDTO {
    private String id;
    private Long auctionId;
    private String username;
    private String message;
    private String createdAt;

    public MessageDTO(AuctionChatMessage auctionChatMessage){
        this.id = auctionChatMessage.getId();
        this.auctionId = Long.valueOf(auctionChatMessage.getAuctionId());
        this.username = auctionChatMessage.getUsername();
        this.message = auctionChatMessage.getMessage();
        this.createdAt = auctionChatMessage.getCreatedAt();
    }
}
