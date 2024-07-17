package com.example.auctrade.domain.auctionUser;

import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 참여 회원
@Entity
@Getter
@NoArgsConstructor
@Table(name = "participant")
public class Participant extends AuctionUser {
    public Participant(User user, Auction auction) {
        super(user, auction);
    }
}
