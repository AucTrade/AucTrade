package com.example.auctrade.domain.bid.service;

import com.example.auctrade.domain.bid.mapper.BidMapper;
import com.example.auctrade.domain.bid.vo.BidInfoVo;
import com.example.auctrade.domain.bid.vo.BidStatus;
import com.example.auctrade.domain.bid.vo.BidUserInfoVo;
import com.example.auctrade.domain.bid.vo.BidVo;
import com.example.auctrade.domain.deposit.service.DepositService;
import com.example.auctrade.domain.user.dto.UserDto;
import com.example.auctrade.domain.user.service.UserService;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.auctrade.global.constant.Constants.REDIS_BID_KEY;
import static com.example.auctrade.global.constant.Constants.BID_USER_KEY;
import static com.example.auctrade.global.constant.Constants.BID_PRICE_KEY;



@Service
@Transactional
@Slf4j(topic = "Bid Service")
public class BidServiceImpl implements BidService {
    private final RedissonClient redissonClient;
    private final DepositService depositService;
    private final BidLogService bidLogService;
    private final UserService userService;
    private static final String LOCK_KEY = "bidLock:";

    public BidServiceImpl(RedissonClient redissonClient, DepositService depositService, BidLogService bidLogService, UserService userService){
        this.redissonClient = redissonClient;
        this.depositService = depositService;
        this.bidLogService = bidLogService;
        this.userService = userService;
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
                bidLogService.createBidLog(bidVo,BidStatus.FAIL);
                return false;
            }

            bidMap.put(BID_USER_KEY, bidVo.getEmail());
            bidMap.put(BID_PRICE_KEY, String.valueOf(bidVo.getAmount()));
            bidLogService.createBidLog(bidVo,BidStatus.CREATE);
            return true;

        } finally {
            lock.unlock();
        }
    }

    /**
     * 특정 경매방의 현재 입찰 정보 조회
     * @param auctionId 대상이 될 경매 ID
     * @return 현재 입찰 정보
     */
    @Override
    public BidUserInfoVo getBidUserInfo(Long auctionId) {
        RMap<String, String> bidMap = redissonClient.getMap(REDIS_BID_KEY + auctionId);
        return BidMapper.toBidUserInfoVo(bidMap.get(BID_USER_KEY), Integer.valueOf(bidMap.get(BID_PRICE_KEY)));
    }

    /**
     * 특정 유저의 입찰 정보 조회
     * @param page 페이지 정보
     * @param size 데이터 수
     * @param email 대상이 될 회원 Email
     * @return 입찰 정보 리스트
     */
    @Override
    public List<BidInfoVo> getAllMyBid(int page, int size, String email) {
        UserDto.Info userInfo = userService.getUserInfo(email);
        return bidLogService.getAllMyBidLog(toPageable(page,size,"createdAt"), userInfo.getUserId());
    }

    /**
     * 특정 경매의 입찰 정보 조회
     * @param page 페이지 정보
     * @param size 데이터 수
     * @param auctionId 대상이 될 경매 ID
     * @return 입찰 정보 리스트
     */
    @Override
    public List<BidInfoVo> getAllByAuctionId(int page, int size, Long auctionId) {
        return bidLogService.getAllBidLog(toPageable(page, size, "createdAt"), auctionId);
    }

    /**
     * 특정 경매의 입찰 완료 처리
     * @param auctionId 대상이 될 경매 ID
     * @return 입찰 완료 성공 여부
     */
    @Override
    public Boolean completeBid(Long auctionId) {
        String key = REDIS_BID_KEY + auctionId;
        RMap<String, String> bidMap = redissonClient.getMap(key);
        String email = bidMap.get(BID_USER_KEY);
        String amount = bidMap.get(BID_PRICE_KEY);

        if(email == null || amount== null) return false;
        UserDto.Info userInfo = userService.getUserInfo(email);
        userService.subPoint(userInfo.getUserId(), Integer.valueOf(amount));
        bidLogService.updateLogStatus(auctionId, userInfo.getUserId(), BidStatus.COMPLETE);

        bidMap.remove(key);
        return true;
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

    private Pageable toPageable(int page, int size, String target){
        return PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, target));
    }
}
