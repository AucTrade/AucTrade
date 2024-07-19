package com.example.auctrade.domain.chat.repository;

import com.example.auctrade.domain.chat.document.AuctionChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<AuctionChatMessage, String> {
    // 경매 아이디 기반 해당 모든 채팅 로그 전부 조회
    List<AuctionChatMessage> findAllByAuctionId(String auctionId);

    // 경매 아이디 기반 해당 경매 로그 조회
    List<AuctionChatMessage> findAllByAuctionIdAndBidTrue(String auctionId);
}