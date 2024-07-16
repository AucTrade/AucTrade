package com.example.auctrade.domain.auction.repository;

import com.example.auctrade.domain.auction.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    // 모든 경매 전체 조회

    // 경매 id 기반 조회

    // 경매 생성 저장
}
