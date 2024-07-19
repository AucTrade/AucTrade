package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.repository.BidLogRepository;
import com.example.auctrade.domain.chat.dto.MessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BidLogService {

    private final BidLogRepository bidLogRepository;

    // 입찰가격을 계속 업데이트한다
    // 대신 이전에 호출한 가격보다 낮으면 업데이트에서 배제한다

    // 입찰가격 업데이트 로직
    public void updateBidPrice(MessageDTO messageDTO) {
        // redis key, 경매방 아이디
        String auctionId = messageDTO.getAuctionId().toString();

        // hash key, 유저 식별자(이메일)
        String username = messageDTO.getUsername();

        // hash value, 입찰가격
        String message = messageDTO.getMessage();
        Integer bidPrice = Integer.parseInt(message.substring(1));

        //TODO: 유효성 검증
        if (bidPrice <= 0) return;

        if (isCorrectBidPrice(auctionId, username, bidPrice)) {
            Map<String, Integer> hash = new HashMap<>();
            hash.put(username, bidPrice);

            bidLogRepository.saveHash(auctionId, hash);
        }
    }

    // username 의 hash value 조회
    private boolean isCorrectBidPrice(String key, String hash, Integer value) {
        Integer previousBidPrice = bidLogRepository.getHashValue(key, hash);
        return value > previousBidPrice;
    }

    // 특정 경매방의 경매내역 로그 조회
    public List<Map.Entry<String, Integer>> getBidLogs(String auctionId) {
        Map<Object, Object> hash = bidLogRepository.getHash(auctionId);

        return hash.entrySet().stream()
                .filter(entry -> entry.getKey() instanceof String && entry.getValue() instanceof Integer)
                .map(entry -> new AbstractMap.SimpleEntry<>((String) entry.getKey(), (Integer) entry.getValue()))
                .collect(Collectors.toList());
    }
}
