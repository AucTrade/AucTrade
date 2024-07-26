package com.example.auctrade.domain.auction.repository;

import com.example.auctrade.domain.auction.document.DepositLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DepositLogRepository extends MongoRepository<DepositLog, String> {
    List<DepositLog> findAllByAuctionId(String auctionId);
}
