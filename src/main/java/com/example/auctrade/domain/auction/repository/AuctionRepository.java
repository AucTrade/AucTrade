package com.example.auctrade.domain.auction.repository;

import com.example.auctrade.domain.auction.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    // 상품 ID가 해당 경매 엔티티에 존재하는지 여부
    // 서비스 로직에서 상품 ID의 독립 고유성 검증
    boolean existsByProductIdsContaining(Long productId);
}
