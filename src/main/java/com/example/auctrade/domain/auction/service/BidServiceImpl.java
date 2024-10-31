package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.document.BidLog;
import com.example.auctrade.domain.auction.dto.BidDTO;
import com.example.auctrade.domain.auction.mapper.BidMapper;
import com.example.auctrade.domain.auction.repository.BidLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
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
    private final DepositService depositService;
    private final BidLogRepository bidLogRepository;
    private final RedissonClient redissonClient;

    private static final String BID_USER_KEY = "username";
    private static final String BID_PRICE_KEY = "bid";
    private static final String LOCK_KEY = "bidLock:";

    /**
     * 입찰가격 업데이트 로직 만약 입찰가가 이전보다 낮은 경우에는 업데이트를 하지 않는다.
     * @param request 회원이 입력한 경매 입찰가 정보
     * @return 입찰 결과
     */
    public BidDTO.Result placeBid(BidDTO.Create request) {
        RLock lock = redissonClient.getLock(LOCK_KEY + request.getAuctionId());
        try {
            if(lock.tryLock(100, 10, TimeUnit.SECONDS)
                    && processBid(request.getAuctionId(), request.getPrice(), request.getUsername())){
                bidLogRepository.save(BidMapper.toBidLogEntity(request));
                return BidMapper.toBidResultDto(request, true);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
            return BidMapper.toBidResultDto(request, false);
        }finally {
            lock.unlock();
        }
        return BidMapper.toBidResultDto(request, false);
    }

    private boolean processBid(long auctionId, long price, String email) {

        if(getBidPrice(auctionId) >= price) return false;
        if(depositService.getMyDepositByAuctionId(auctionId,email) < price) return false;
        try {
            RMap<String, String> auctionBids = redissonClient.getMap(REDIS_BID_KEY + auctionId);
            auctionBids.put(BID_USER_KEY, email);
            auctionBids.put(BID_PRICE_KEY, String.valueOf(price));
            return true;
        }catch (Exception e){
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * 특정 경매방의 현재 입찰 정보 조회
     * @param auctionId 대상이 될 경매 ID
     * @return 현재 입찰 정보 조회
     */
    @Override
    public BidDTO.Get getCurrentBid(Long auctionId) {
        RMap<String, String> bidMap = redissonClient.getMap(REDIS_BID_KEY + auctionId);
        String username = bidMap.get(BID_USER_KEY);
        String price = bidMap.get(BID_PRICE_KEY);
        return BidDTO.Get.builder()
                .username((username == null) ? "NONE" : username)
                .price((price == null) ? -1 : Integer.parseInt(price))
                .build();
    }

    /**
     * 특정 경매방의 현재 입찰가 조회
     * @param auctionId 대상이 될 경매 ID
     * @return 현재 입찰가 (입찰가가 존재하지 않을 경우 -1)
     */
    @Override
    public int getBidPrice(Long auctionId) {
        RMap<String, String> bidMap = redissonClient.getMap(REDIS_BID_KEY + auctionId);
        String price = bidMap.get(BID_PRICE_KEY);
        return (price == null) ? -1 : Integer.parseInt(price);
    }

    /**
     * 특정 경매방의 현재 입찰자 조회
     * @param auctionId 대상이 될 경매 ID
     * @return 현재 입찰자 (입찰자가 존재하지 않을 경우 빈 문자열)
     */
    @Override
    public String getBidUser(Long auctionId) {
        RMap<String, String> bidMap = redissonClient.getMap(REDIS_BID_KEY + auctionId);
        return bidMap.getOrDefault(BID_USER_KEY, "NONE");
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

    @Override
    public void removeMyBidLog(String email){
        List<BidLog> logs = bidLogRepository.findAllByUsername(email);
        for(BidLog log : logs){
            bidLogRepository.delete(log);
        }
    }
}
