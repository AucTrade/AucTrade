package com.example.auctrade.domain.auction.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "auctions")
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "price", nullable = false)
    private int price; // 계속 업데이트하다가 경매 종료됐을 때의 price 를 최종낙찰가로

    @Column(name = "products", nullable = false)
    private String products; // Product 엔티티 생성시 수정 필요

    @Column(name = "user_id", nullable = false)
    private Long userId; // User 엔티티 생성시 수정 필요

    @CreatedDate
    @Column(name = "created", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt; // 경매 게시일자

    @Column(name = "auction_date", nullable = false)
    private LocalDateTime auctionDate; // 경매 시작예고시간, 시간 포맷팅 고려 필요

    @Column(name = "minimum_price", nullable = false)
    private int minimumPrice;

    @Column(name = "end", nullable = false)
    private boolean end; // price 가 진행 가격인지 최종 낙찰 가격인지

    @Builder
    public Auction(int price, String products, Long userId, LocalDateTime auctionDate, int minimumPrice) {
        this.price = price;
        this.products = products;
        this.userId = userId;
        this.auctionDate = auctionDate;
        this.minimumPrice = minimumPrice;
        this.end = false;
    }
}
