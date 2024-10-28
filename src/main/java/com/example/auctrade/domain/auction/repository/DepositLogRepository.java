package com.example.auctrade.domain.auction.repository;

import com.example.auctrade.domain.auction.document.DepositLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface DepositLogRepository extends MongoRepository<DepositLog, String> {
    List<DepositLog> findAllByAuctionId(String auctionId);

    List<DepositLog> findAllByUsername(String username);

    List<DepositLog> findAllByAuctionIdAndUsername(Long auctionId, String username);

    @Query(value = "{ 'username': ?0 }", fields = "{ 'auctionId': 1, '_id': 0 }")
    List<DepositLog> findAllAuctionIdByUsername(Pageable pageable, String username);

    long countByUsername(String username);
}
