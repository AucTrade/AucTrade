package com.example.auctrade.domain.auctionUser;

import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

//
@Entity
@Getter
@NoArgsConstructor
@Table(name = "award_user")
public class AwardUser extends AuctionUser {
    public AwardUser(User user, Auction auction) {
        super(user, auction);
    }
}