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
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "max_participants", nullable = false)
    private int maxParticipants;

    @Column(name = "product_id", nullable = false)
    private long productId;

    @Column(name = "seller_email", nullable = false)
    private String sellerEmail;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @CreatedDate
    @Column(name = "created", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Column(name = "minimum_price", nullable = false)
    private int minimumPrice;

    @Column(name = "final_price")
    private int finalPrice;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
}
