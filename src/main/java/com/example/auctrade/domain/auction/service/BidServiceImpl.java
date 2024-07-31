package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.document.BidLog;
import com.example.auctrade.domain.auction.dto.BidDTO;
import com.example.auctrade.domain.auction.repository.BidLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.example.auctrade.global.constant.Constants.REDIS_BID_KEY;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidServiceImpl implements BidService {
    private final BidLogRepository bidLogRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String BID_USER_KEY = "username";
    private static final String BID_PRICE_KEY = "bid";
    private static final String LOCK_KEY = "bidLock";
    private static final String BID_QUEUE_KEY = "bidQueue:";

    private final RedissonClient redissonClient;

    /**
     * 입찰가격 업데이트 로직 만약 입찰가가 이전보다 낮은 경우에는 업데이트를 하지 않는다.
     * @param request 회원이 입력한 경매 입찰가 정보
     * @return 입찰 결과
     */
    public Boolean updateBidPrice(BidDTO.Create request) {
        bidLogRepository.save(new BidLog(request));
        if(findBidPriceByAuctionId(request.getAuctionId()) >= request.getPrice()) return false;
        redisTemplate.opsForList().leftPush(BID_QUEUE_KEY+request.getAuctionId(), request.getUsername()+":"+request.getPrice());
        return true;
    }
    /**
     * 입찰 update
     * @param auctionId 대상이 될 경매 ID
     */
    public void processBids(Long auctionId) {
        RLock lock = redissonClient.getLock(LOCK_KEY);
        boolean isLocked = false;
        try {
            // 락을 획득하려고 시도합니다. (최대 대기 시간: 100ms, 락 유지 시간: 10s)
            isLocked = lock.tryLock(100, 10000, TimeUnit.MILLISECONDS);
            if (isLocked) {
                String bid;
                while ((bid = redisTemplate.opsForList().rightPop(BID_QUEUE_KEY+auctionId)) != null) {
                    String[] res = bid.split(":");
                    Map<String, String> hash = new HashMap<>();
                    hash.put(BID_USER_KEY, res[0]);
                    hash.put(BID_PRICE_KEY, res[1]);
                    redisTemplate.opsForHash().putAll(REDIS_BID_KEY + auctionId, hash);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (isLocked) lock.unlock();
        }
    }

    /**
     * 특정 경매방의 현재 입찰가 조회
     * @param auctionId 대상이 될 경매 ID
     * @return 현재 입찰가 (입찰가가 존재하지 않을 경우 -1)
     */
    public Long findBidPriceByAuctionId(Long auctionId) {
        Object val = redisTemplate.opsForHash().get(REDIS_BID_KEY + auctionId, BID_PRICE_KEY);
        return (val == null) ? -1 : Long.parseLong(val.toString());
    }

    /**
     * 특정 경매방의 현재 입찰자 조회
     * @param auctionId 대상이 될 경매 ID
     * @return 현재 입찰자 (입찰자가 존재하지 않을 경우 빈 문자열)
     */
    public String findBidUserByAuctionId(Long auctionId) {
        Object val = redisTemplate.opsForHash().get(REDIS_BID_KEY + auctionId, BID_USER_KEY);
        return (val == null) ? "" : val.toString();
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
