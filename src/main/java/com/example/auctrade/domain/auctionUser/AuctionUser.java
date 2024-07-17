package com.example.auctrade.domain.auctionUser;

import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@MappedSuperclass
@Getter
@NoArgsConstructor
public abstract class AuctionUser {
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

    public AuctionUser(User user, Auction auction) {
        this.user = user;
        this.auction = auction;
    }
}
