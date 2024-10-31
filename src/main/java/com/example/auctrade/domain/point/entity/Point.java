package com.example.auctrade.domain.point.entity;

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
@Table(name = "pointLog")
@EntityListeners(AuditingEntityListener.class)
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId; // 포인트를 가지고 있는 유저 email

    @Column(name = "amount")
    private Integer amount; // 포인트 변동사항

    @Column(name = "account")
    private String account; // 포인트 환전 계좌

    @CreatedDate
    @Column(name = "created", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt; // 포인트 거래일
    
    @Builder
    public static Point createPointLog(String userId, int amount, String account) {
        Point point = new Point();
        point.userId = userId;
        point.amount = amount;
        point.account = account;
        return point;
    }
}