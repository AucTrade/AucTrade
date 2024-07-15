package com.example.auctrade.chat.repository;

import com.example.auctrade.chat.document.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByRoomId(int roomId);
    ChatMessage findTopByRoomIdOrderByCreatedAtDesc(int roomId);
    List<ChatMessage> findAllByRoomId(String roomId);
}