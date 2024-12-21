package com.example.auctrade.domain.chat.dto;

import com.example.auctrade.global.valid.MessageValidationGroups;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;


public class AuctionMessageDto {
    private AuctionMessageDto(){}

    @Getter
    @AllArgsConstructor
    public static class Create {
        @NotBlank(message = "경매 ID가 없습니다.", groups = MessageValidationGroups.AuctionIdBlankGroup.class)
        private Long auctionId;
        @NotBlank(message = "message 내용이 없습니다.", groups = MessageValidationGroups.MessageBlankGroup.class)
        private String message;
    }
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Enter {
        @NotBlank(message = "경매 ID가 없습니다.", groups = MessageValidationGroups.AuctionIdBlankGroup.class)
        private Long auctionId;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Get{
        private final Long id;
        private final Long auctionId;
        private final String email;
        private final String message;
        private final LocalDateTime createAt;
    }
}
