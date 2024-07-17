package com.example.auctrade.domain.auctionUser;

import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 판매 회원
@Entity
@Getter
@NoArgsConstructor
@Table(name = "sale_user")
public class SaleUser extends AuctionUser {
    public SaleUser(User user, Auction auction) {
        super(user, auction);
    }
}
