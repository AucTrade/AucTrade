package com.example.auctrade.domain.chat.document;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Document(collection = "DirectChatMessage")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;	//도큐먼트 아이디

    private String username;
    private String message;
    private String createdAt;

    public ChatMessage(String username, String message){
        this.username = username;
        this.message = message;
    }

}
