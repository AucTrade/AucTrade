package com.example.auctrade.domain.chat.dto;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.domain.chat.document.AuctionChatMessage;
import com.example.auctrade.global.vaild.MessageValidationGroups;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


public class MessageDTO {
    @Getter
    @AllArgsConstructor
    public static class Create {

        @NotBlank(message = "경매 ID가 없습니다.", groups = MessageValidationGroups.AuctionIdBlankGroup.class)
        private Long auctionId;
        @NotBlank(message = "user ID 가 없습니다.", groups = MessageValidationGroups.UsernameBlankGroup.class)
        private String username;
        @NotBlank(message = "message 내용이 없습니다.", groups = MessageValidationGroups.MessageBlankGroup.class)
        private String message;
    }

    @Getter
    public static class Get{
        private final String id;
        private final Long auctionId;
        private final String username;
        private final String message;
        private final String createAt;

        public Get(AuctionChatMessage auctionChatMessage){
            this.id = auctionChatMessage.getId();
            this.auctionId = Long.parseLong(auctionChatMessage.getAuctionId());
            this.username = auctionChatMessage.getUsername();
            this.message = auctionChatMessage.getMessage();
            this.createAt = auctionChatMessage.getCreatedAt();
        }
    }



    public static class BidGet{
        private final Long auctionId;
        private final String username;
        private final Boolean isSuccess;
        private final Long price;
        public BidGet(AuctionDTO.BidResult bidResult){
            this.auctionId = bidResult.getAuctionId();
            this.username = bidResult.getUsername();
            this.isSuccess = bidResult.getIsSuccess();
            this.price = bidResult.getPrice();
        }
    }
}
