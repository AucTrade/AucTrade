package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.document.BidLog;
import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.domain.auction.mapper.AuctionMapper;
import com.example.auctrade.domain.auction.repository.BidLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.auctrade.global.constant.Constants.REDIS_AUCTION_KEY;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidLogServiceImpl implements BidLogService {
    private final BidLogRepository bidLogRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 입찰가격 업데이트 로직 만약 입찰가가 이전보다 낮은 경우에는 업데이트를 하지 않는다.
     * @param requestDto 회원이 입력한 경매 입찰가 정보
     * @return 입찰 결과
     */
    public AuctionDTO.BidResult updateBidPrice(AuctionDTO.Bid requestDto) {
        String key = REDIS_AUCTION_KEY + requestDto.getAuctionId();
        BidLog bidLog = bidLogRepository.save(AuctionMapper.toEntity(requestDto));

        Object obj = redisTemplate.opsForHash().get(key, "bid");
        long beforePrice = (obj == null) ? -1 : (long) obj;
        if(beforePrice >= requestDto.getPrice())
            return AuctionMapper.toBidResultDTO(bidLog, false);

        Map<String, String> hash = new HashMap<>();
        hash.put("username", bidLog.getUsername());
        hash.put("bid", bidLog.getPrice().toString());
        redisTemplate.opsForHash().putAll(key, hash);

        return AuctionMapper.toBidResultDTO(bidLog, true);
    }

    /**
     * 특정 경매방의 경매내역 로그 조회
     * @param auctionId 대상이 될 경매 ID
     * @return 해당 경매 내역 로그 리스트
     */
    public List<AuctionDTO.BidList> getBidLogs(String auctionId) {
        return bidLogRepository.findAllByAuctionId(auctionId).stream().map(AuctionDTO.BidList::new).toList();
    }
}
