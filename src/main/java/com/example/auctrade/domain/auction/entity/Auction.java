package com.example.auctrade.domain.auction.entity;

import com.example.auctrade.domain.product.entity.Product;
import com.example.auctrade.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
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

    @OneToOne
    @JoinColumn(name = "product")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "sale_user", nullable = false)
    private User saleUser; // User 객체 (경매 생성자 및 판매자)

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate; // 경매 시작예고시간, 시간 포맷팅 고려 필요

    @CreatedDate
    @Column(name = "created", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt; // 경매 채팅방 생성일자

    @Column(name = "minimum_price", nullable = false)
    private int minimumPrice; // 최소입찰금액

    @Column(name = "price", nullable = false)
    private int price; // 계속 업데이트하다가 경매 종료됐을 때의 price 를 최종낙찰가로

    @Column(name = "ended", nullable = false)
    private boolean ended; // price 가 진행 가격인지 최종 낙찰 가격인지

    @Column(name = "finish_date", nullable = false)
    private LocalDateTime finishDate; // 경매 종료예고시간, 시간 포맷팅 고려 필요
}
