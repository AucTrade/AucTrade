package com.example.auctrade.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.WebSocketSession;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public class AuctionChatRoomDTO implements Serializable {
    private  String id;
    private  String title;
    private int count;
    private  String createdTime;
    private Set<WebSocketSession> sessions = new HashSet<>();

    @Builder
    public AuctionChatRoomDTO(String id, int count, String createdTime, String title){
        this.id = id;
        this.count = count;
        this.createdTime = createdTime;
        this.title = title;
    }
    public AuctionChatRoomDTO(String title){
        this.title = title;
    }
    public void updateCount(int count){
        this.count = count;
    }
}
