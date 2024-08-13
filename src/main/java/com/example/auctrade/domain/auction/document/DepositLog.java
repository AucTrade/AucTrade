package com.example.auctrade.domain.auction.document;

import com.example.auctrade.domain.auction.dto.DepositDTO;
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
    @Indexed
    private String startAt;
    private String username;
    private Long deposit;

    @CreatedDate
    private String createdAt;

    public DepositLog(DepositDTO.Create requestDTO,String startAt, String email) {
        this.auctionId = requestDTO.getAuctionId();
        this.startAt = startAt;
        this.username = email;
        this.deposit =  requestDTO.getDeposit();
    }

    public void updateDeposit(long deposit){
        this.deposit = deposit;
    }
}
