package com.example.auctrade.domain.chat.repository;

import com.example.auctrade.domain.chat.document.AuctionChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<AuctionChatMessage, String> {
    List<AuctionChatMessage> findByAuctionId(String auctionId);
    AuctionChatMessage findTopByAuctionIdOrderByCreatedAtDesc(String auctionId);
    List<AuctionChatMessage> findAllByAuctionId(String roomId);
}