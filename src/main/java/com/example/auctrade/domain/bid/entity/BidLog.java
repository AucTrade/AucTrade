package com.example.auctrade.domain.bid.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "bid_log")
public class BidLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "auction_id", nullable = false)
    private Long auctionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "result", nullable = false)
    private Boolean isSuccess;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public BidLog(Long auctionId, Long userId, Integer amount, Boolean isSuccess){
        this.auctionId = auctionId;
        this.userId = userId;
        this.amount = amount;
        this.isSuccess = isSuccess;
    }
}
