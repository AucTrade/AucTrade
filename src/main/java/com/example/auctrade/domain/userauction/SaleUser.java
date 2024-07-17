package com.example.auctrade.domain.userauction;

import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 판매 회원
@Entity
@NoArgsConstructor
@Getter
@Table(name = "sale_user")
public class SaleUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "auction_id")
    private Auction auction;
}
