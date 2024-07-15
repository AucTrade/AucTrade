package com.example.auctrade.chat.dto;

import com.example.auctrade.chat.document.ChatMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class MessageDTO {
    private String id;
    private String roomId;
    private String username;
    private String message;
    private String createdAt;

    public MessageDTO(ChatMessage chatMessage){
        this.id = chatMessage.getId();
        this.roomId = chatMessage.getAuctionId();
        this.username = chatMessage.getUsername();
        this.message = chatMessage.getMessage();
        this.createdAt = chatMessage.getCreatedAt();
    }
}
