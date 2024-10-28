package com.example.auctrade.domain.auction.document;

import com.example.auctrade.domain.auction.dto.DepositDTO;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@AllArgsConstructor
@Builder
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
    private Integer deposit;

    @CreatedDate
    private String createdAt;

    public void updateDeposit(int deposit){
        this.deposit = deposit;
    }
}
