package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.document.BidLog;
import com.example.auctrade.domain.auction.dto.BidDTO;
import com.example.auctrade.domain.auction.repository.BidLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.auctrade.global.constant.Constants.REDIS_BID_KEY;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidServiceImpl implements BidService {
    private final BidLogRepository bidLogRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String BID_USER_KEY = "username";
    private static final String BID_PRICE_KEY = "bid";

    /**
     * 입찰가격 업데이트 로직 만약 입찰가가 이전보다 낮은 경우에는 업데이트를 하지 않는다.
     * @param request 회원이 입력한 경매 입찰가 정보
     * @return 입찰 결과
     */
    public Boolean updateBidPrice(BidDTO.Create request) {
        BidLog bidLog = bidLogRepository.save(new BidLog(request));

        if(findByAuctionId(request.getAuctionId()) >= request.getPrice()) return false;

        Map<String, String> hash = new HashMap<>();
        hash.put(BID_USER_KEY, bidLog.getUsername());
        hash.put(BID_PRICE_KEY, bidLog.getPrice().toString());
        redisTemplate.opsForHash().putAll(REDIS_BID_KEY + request.getAuctionId(), hash);

        return true;
    }

    /**
     * 특정 경매방의 현재 입찰가 조회
     * @param auctionId 대상이 될 경매 ID
     * @return 현재 입찰가 (입찰가가 존재하지 않을 경우 -1)
     */
    public Long findByAuctionId(Long auctionId) {
        HashOperations<String, String, String> ops = redisTemplate.opsForHash();
        String val = ops.get(REDIS_BID_KEY + auctionId, BID_PRICE_KEY);
        return (val == null) ? -1 : Long.parseLong(val);
    }

    /**
     * 특정 경매방의 경매내역 로그 조회
     * @param auctionId 대상이 될 경매 ID
     * @return 해당 경매 내역 로그 리스트
     */
    public List<BidDTO.List> getBidLogs(Long auctionId) {
        return bidLogRepository.findAllByAuctionId(auctionId).stream().map(BidDTO.List::new).toList();
    }

}
