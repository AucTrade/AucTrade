package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.document.BidLog;
import com.example.auctrade.domain.auction.dto.BidDTO;
import com.example.auctrade.domain.auction.mapper.BidMapper;
import com.example.auctrade.domain.auction.repository.BidLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.auctrade.global.constant.Constants.REDIS_BID_KEY;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidServiceImpl implements BidService {

    private final BidLogRepository bidLogRepository;
    private final RedissonClient redissonClient;

    private static final String BID_USER_KEY = "username";
    private static final String BID_PRICE_KEY = "bid";
    private static final String LOCK_KEY = "bidLock";
    private static final String BID_QUEUE_KEY = "bidQueue:";

    /**
     * 입찰가격 업데이트 로직 만약 입찰가가 이전보다 낮은 경우에는 업데이트를 하지 않는다.
     * @param request 회원이 입력한 경매 입찰가 정보
     * @return 입찰 결과
     */
    @Override
    public BidDTO.Result updateBidPrice(BidDTO.Create request) {
        bidLogRepository.save(new BidLog(request));

        if (getBidPrice(request.getAuctionId()) >= request.getPrice())
            return BidMapper.toBidResultDto(request, false);


        RList<String> bidQueue = redissonClient.getList(BID_QUEUE_KEY + request.getAuctionId());
        bidQueue.add(0,request.getUsername() + ":" + request.getPrice());
        return BidMapper.toBidResultDto(request, true);
    }

    /**
     * 입찰 update
     * @param auctionId 대상이 될 경매 ID
     */
    @Override
    public void processBids(Long auctionId) {
        RLock lock = redissonClient.getLock(LOCK_KEY);
        boolean isLocked = false;
        try {
            // 락을 획득하려고 시도합니다. (최대 대기 시간: 100ms, 락 유지 시간: 10s)
            isLocked = lock.tryLock(100, 10, TimeUnit.SECONDS);
            if(!isLocked) return;

            RList<String> bidQueue = redissonClient.getList(BID_QUEUE_KEY + auctionId);
            if (bidQueue.isEmpty()) return;
            RMap<String, String> bidMap = redissonClient.getMap(REDIS_BID_KEY + auctionId);
            String bid;
            while ((bid = bidQueue.remove(bidQueue.size() - 1)) != null) {
                String[] res = bid.split(":");
                bidMap.put(BID_USER_KEY, res[0]);
                bidMap.put(BID_PRICE_KEY, res[1]);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (isLocked) lock.unlock();
        }
    }

    /**
     * 특정 경매방의 현재 입찰 정보 조회
     * @param auctionId 대상이 될 경매 ID
     * @return 현재 입찰 정보 조회
     */
    @Override
    public BidDTO.Get getBid(Long auctionId) {
        RMap<String, String> bidMap = redissonClient.getMap(REDIS_BID_KEY + auctionId);
        String username = bidMap.get(BID_USER_KEY);
        String price = bidMap.get(BID_PRICE_KEY);
        return BidDTO.Get.builder()
                .username((username == null) ? "NONE" : username)
                .price((price == null) ? -1L : Long.parseLong(price))
                .build();
    }

    /**
     * 특정 경매방의 현재 입찰가 조회
     * @param auctionId 대상이 될 경매 ID
     * @return 현재 입찰가 (입찰가가 존재하지 않을 경우 -1)
     */
    @Override
    public long getBidPrice(Long auctionId) {
        RMap<String, String> bidMap = redissonClient.getMap(REDIS_BID_KEY + auctionId);
        String price = bidMap.get(BID_PRICE_KEY);
        return (price == null) ? -1L : Long.parseLong(price);
    }

    /**
     * 특정 경매방의 현재 입찰자 조회
     * @param auctionId 대상이 될 경매 ID
     * @return 현재 입찰자 (입찰자가 존재하지 않을 경우 빈 문자열)
     */
    @Override
    public String getBidUser(Long auctionId) {
        RMap<String, String> bidMap = redissonClient.getMap(REDIS_BID_KEY + auctionId);
        return bidMap.getOrDefault(BID_USER_KEY, "");
    }

    /**
     * 특정 경매방의 경매내역 로그 조회
     * @param auctionId 대상이 될 경매 ID
     * @return 해당 경매 내역 로그 리스트
     */
    @Override
    public List<BidDTO.List> getBidLogs(Long auctionId) {
        return bidLogRepository.findAllByAuctionId(auctionId).stream().map(BidMapper::toListDto).toList();
    }
}
