package com.example.auctrade.domain.auction.repository;

import com.example.auctrade.domain.auction.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    // 시작되지 않은 경매들 반환(이걸 기점으로 검색 등등...)
    List<Auction> findByStartedFalse();
    // 상품 ID가 해당 경매 엔티티에 존재하는지 여부
    // 서비스 로직에서 상품 ID의 독립 고유성 검증
    boolean existsByProductContaining(Long productId);
}
