package com.example.auctrade.domain.point.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "point")
@EntityListeners(AuditingEntityListener.class)
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "balance")
    private Integer balance;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PointType type;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PointStatus status;

    @Column(name = "account")
    private String account;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;
    
    @Builder
    public Point(Long userId,Integer amount, Integer balance, PointType type, PointStatus status, String account) {
        this.userId = userId;
        this.amount = amount;
        this.balance = balance;
        this.status = status;
        this.type = type;
        this.account = account;
    }

    public void updateStatus(PointStatus status){
        this.status = status;
    }
    public void updateBalance(Integer balance){
        this.balance = balance;
    }
}