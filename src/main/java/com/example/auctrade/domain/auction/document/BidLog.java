package com.example.auctrade.domain.auction.document;

import com.example.auctrade.domain.auction.dto.BidDTO;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@Document(collection = "bid_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BidLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private Long auctionId;
    private String username;
    private Long price;

    @CreatedDate
    @Indexed
    private String createdAt;

    public BidLog(BidDTO.Create bidLogDTO) {
        this.auctionId = bidLogDTO.getAuctionId();
        this.username = bidLogDTO.getUsername();
        this.price =  bidLogDTO.getPrice();
    }
}
