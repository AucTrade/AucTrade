package com.example.auctrade.domain.deposit.entity;

import com.example.auctrade.domain.deposit.vo.DepositStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "deposit_log")
public class DepositLog {
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

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private DepositStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public DepositLog(Long auctionId, Long userId, Integer amount, DepositStatus status){
        this.auctionId = auctionId;
        this.userId = userId;
        this.amount = amount;
        this.status = status;
    }

    public Boolean updateStatus(DepositStatus status){
        this.status = status;
        return true;
    }
}
