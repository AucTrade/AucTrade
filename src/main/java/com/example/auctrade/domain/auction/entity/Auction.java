package com.example.auctrade.domain.auction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "auction")
@EntityListeners(AuditingEntityListener.class)
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "introduce")
    private String introduce;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "minimum_price", nullable = false)
    private Integer minimumPrice;

    @Column(name = "final_price")
    private Integer finalPrice;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "is_ended", nullable = false)
    private Boolean isEnded;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Builder
    public Auction(String title, String introduce, Integer maxParticipants, Long productId, Long userId, Integer minimumPrice, LocalDateTime startAt, LocalDateTime endAt,Boolean isEnded){
        this.title = title;
        this.introduce = introduce;
        this.maxParticipants = maxParticipants;
        this.productId = productId;
        this.userId = userId;
        this.minimumPrice = minimumPrice;
        this.startAt = startAt;
        this.endAt = endAt;
        this.isEnded = isEnded;
    }
}
