package com.example.auctrade.domain.auction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "auctions")
@EntityListeners(AuditingEntityListener.class)
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 경매장 id

    @Column(name = "title", nullable = false)
    private String title; // 경매 제목

    @Column(name = "introduce")
    private String introduce; // 경매 설명

    @Column(name = "personnel", nullable = false)
    private int personnel; // 경매 참여인(최대)

    @Column(name = "product_id", nullable = false)
    private long productId;

    @Column(name = "sale_username", nullable = false)
    private String saleUsername;

    @Column(name = "started", nullable = false)
    private boolean started; // 경매 시작여부

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate; // 경매 시작예고시간, 시간 포맷팅 고려 필요

    @CreatedDate
    @Column(name = "created", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt; // 경매 채팅방 생성일자

    @Column(name = "minimum_price", nullable = false)
    private int minimumPrice; // 최소입찰금액

    @Column(name = "price", nullable = false)
    private int price; // 최종낙찰가

    @Column(name = "ended", nullable = false)
    private boolean ended;

    @Column(name = "finish_date", nullable = false)
    private LocalDateTime finishDate;

    @Builder
    public static Auction createAuction(
            String title,
            String introduce,
            int personnel,
            long productId,
            String saleUsername,
            LocalDateTime startDate,
            int minimumPrice,
            int price,
            LocalDateTime finishDate
    ) {
        Auction auction = new Auction();
        auction.title = title;
        auction.introduce = introduce;
        auction.personnel = personnel;
        auction.productId = productId;
        auction.saleUsername = saleUsername;
        auction.started = false;
        auction.startDate = startDate;
        auction.minimumPrice = minimumPrice;
        auction.price = price;
        auction.ended = false;
        auction.finishDate = finishDate;
        return auction;
    }

    public void start() {
        this.started = true;
    }

    // 날짜 지났으면 강제 시작 처리
    public boolean checkAndStartAuction(LocalDateTime now) {
        if (now.isAfter(this.startDate)) {
            this.start();
            return true;
        }

        return false;
    }

    public void end() {
        this.ended = true;
    }

    // 날짜 지났으면 강제 종료 처리
    public boolean checkAndEndAuction(LocalDateTime now) {
        if (now.isAfter(this.finishDate)) {
            this.end();
            return true;
        }

        return false;
    }
}
