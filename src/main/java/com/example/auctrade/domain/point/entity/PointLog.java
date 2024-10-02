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
public class PointLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId; // 포인트를 가지고 있는 유저 email

    @Column(name = "point")
    private Long point; // 포인트 변동사항

    @CreatedDate
    @Column(name = "created", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt; // 포인트 충전일
    
    @Builder
    public static PointLog createPointLog(String userId, long point) {
        PointLog pointLog = new PointLog();
        pointLog.userId = userId;
        pointLog.point = point;
        return pointLog;
    }
}
