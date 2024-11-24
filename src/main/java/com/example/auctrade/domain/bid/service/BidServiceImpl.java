package com.example.auctrade.domain.bid.service;


import com.example.auctrade.domain.auction.mapper.AuctionMapper;
import com.example.auctrade.domain.bid.mapper.BidMapper;
import com.example.auctrade.domain.bid.repository.BidLogRepository;
import com.example.auctrade.domain.bid.vo.BidInfoVo;
import com.example.auctrade.domain.bid.vo.BidVo;
import com.example.auctrade.domain.deposit.service.DepositService;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.auctrade.global.constant.Constants.REDIS_BID_KEY;
import static com.example.auctrade.global.constant.Constants.BID_USER_KEY;
import static com.example.auctrade.global.constant.Constants.BID_PRICE_KEY;



@Service
@Transactional
@Slf4j(topic = "Bid Service")
public class BidServiceImpl implements BidService {
    private final RedissonClient redissonClient;
    private final DepositService depositService;
    private final BidLogRepository bidLogRepository;

    private static final String LOCK_KEY = "bidLock:";

    public BidServiceImpl(RedissonClient redissonClient, DepositService depositService, BidLogRepository bidLogRepository){
        this.redissonClient = redissonClient;
        this.depositService = depositService;
        this.bidLogRepository = bidLogRepository;
    }

    /**
     * 입찰가격 업데이트 로직 만약 입찰가가 이전보다 낮은 경우에는 업데이트를 하지 않는다.
     * @param bidVo 회원이 입력한 경매 입찰 정보
     * @return 입찰 결과
     */
    public Boolean placeBid(BidVo bidVo) {
        if(bidVo.getAmount() > depositService.getDepositAmount(bidVo.getAuctionId(), bidVo.getUserId())) throw new CustomException(ErrorCode.WRONG_BID_AMOUNT);

        RLock lock = redissonClient.getLock(LOCK_KEY + bidVo.getAuctionId());
        lock.lock();
        try {
            RMap<String, String> bidMap = redissonClient.getMap(REDIS_BID_KEY + bidVo.getAuctionId());

            String bid = bidMap.get(BID_PRICE_KEY);
            if(bid != null && bidVo.getAmount() <= Integer.parseInt(bid)){
                bidLogRepository.save(BidMapper.toEntity(bidVo, false));
                return false;
            }

            bidMap.put(BID_USER_KEY, bidVo.getEmail());
            bidMap.put(BID_PRICE_KEY, String.valueOf(bidVo.getAmount()));
            bidLogRepository.save(BidMapper.toEntity(bidVo, true));
            return true;

        } finally {
            lock.unlock();
        }
    }

    /**
     * 특정 경매방의 현재 입찰 정보 조회
     * @param auctionId 대상이 될 경매 ID
     * @return 현재 입찰 정보 조회
     */
    @Override
    public BidInfoVo getBidInfo(Long auctionId) {
        RMap<String, String> bidMap = redissonClient.getMap(REDIS_BID_KEY + auctionId);
        return AuctionMapper.toBidInfoVo(auctionId, bidMap.get(BID_USER_KEY), bidMap.get(BID_PRICE_KEY));
    }

    /**
     * 입찰 취소
     * @param auctionId 대상 경매 ID
     * @param email 대상 유저 이메일
     * @return 입찰 취소 성공 여부
     */
    @Override
    public Boolean cancelBid(Long auctionId, String email) {
        String key = REDIS_BID_KEY + auctionId;
        RMap<String, String> bidMap = redissonClient.getMap(key);
        String target = bidMap.get(BID_USER_KEY);

        if(target == null || !target.equals(email))
            throw new CustomException(ErrorCode.REDIS_INTERNAL_ERROR);

        bidMap.remove(key);
        return true;
    }
}
