package com.example.auctrade.domain.bid.repository;

import com.example.auctrade.domain.bid.entity.BidLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BidLogRepository extends JpaRepository<BidLog, Long> {

}
