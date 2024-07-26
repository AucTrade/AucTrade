package com.example.auctrade.domain.auction.document;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
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
@Document(collection = "deposit_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DepositLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private Long auctionId;
    private String username;
    private Long deposit;

    @CreatedDate
    @Indexed
    private String createdAt;

    public DepositLog(AuctionDTO.Deposit requestDTO) {
        this.auctionId = requestDTO.getId();
        this.username = requestDTO.getUsername();
        this.deposit =  requestDTO.getDeposit();
    }
}
