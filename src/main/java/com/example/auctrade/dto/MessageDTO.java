package com.example.auctrade.dto;

import com.example.auctrade.document.ChatMessage;
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
        this.roomId = chatMessage.getRoomId();
        this.username = chatMessage.getUsername();
        this.message = chatMessage.getMessage();
        this.createdAt = chatMessage.getCreatedAt();
    }
}
