package com.example.auctrade.domain.chat.repository;

import com.example.auctrade.domain.chat.entity.AuctionChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuctionChatMessageRepository extends JpaRepository<AuctionChatMessage, Long> {
    List<AuctionChatMessage> findAllByAuctionId(Long auctionId);
}