package com.example.auctrade.domain.deposit.service;

import com.example.auctrade.domain.deposit.vo.DepositInfoVo;
import com.example.auctrade.domain.deposit.vo.DepositStatus;
import com.example.auctrade.domain.deposit.vo.DepositVo;
import com.example.auctrade.domain.user.dto.UserDto;
import com.example.auctrade.domain.user.service.UserService;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.example.auctrade.global.constant.Constants.REDIS_DEPOSIT_KEY;

@Service
@Transactional
@Slf4j(topic = "UserDepositService")
public class DepositServiceImpl implements DepositService {
    private final UserService userService;
    private final DepositLogService depositLogService;
    private final RedissonClient redissonClient;
    private static final String LOCK_KEY = "depositLock:";

    public DepositServiceImpl(UserService userService, DepositLogService depositLogService, RedissonClient redissonClient){
        this.userService = userService;
        this.depositLogService = depositLogService;
        this.redissonClient = redissonClient;
    }

    private Long createDeposit(RScoredSortedSet<String> depositSet, DepositVo depositVo){
        DepositInfoVo cancelDepositVo = null;
        Boolean isCancel = false;

        try{
            //예치금을 넣을 경매의 인원이 가득찬 경우
            if(depositVo.getMaxParticipants() <= depositSet.size()){

                cancelDepositVo = depositLogService.getMinDepositLog(depositVo.getAuctionId());
                if (depositVo.getAmount() <= cancelDepositVo.getAmount()) throw new CustomException(ErrorCode.WRONG_DEPOSIT_CREATE);

                isCancel = cancelDeposit(cancelDepositVo.getAuctionId(), cancelDepositVo.getUserId());
            }

            userService.subPoint(depositVo.getUserId(), depositVo.getAmount());
            Long createdId = depositLogService.createDepositLog(depositVo);

            addRedisSet(depositSet, depositVo);
            return createdId;
            
        }catch (Exception e){
            //이전의 최소 예치금 정보를 삭제 후 에러가 발생한 경우 Redis Rollback
            if(isCancel) depositSet.add(cancelDepositVo.getAmount(), cancelDepositVo.getUserId().toString());
            throw e;
        }
    }

    private Long updateDeposit(RScoredSortedSet<String> depositSet, DepositVo depositVo) {
        Integer beforeAmount = null;
        Boolean isCancel = false;

        try{
            beforeAmount = depositSet.getScore(depositVo.getUserId().toString()).intValue();
            if(depositVo.getAmount() <= beforeAmount) throw new CustomException(ErrorCode.WRONG_DEPOSIT_UPDATE);

            isCancel = cancelDeposit(depositVo.getAuctionId(), depositVo.getUserId());

            userService.subPoint(depositVo.getUserId(), depositVo.getAmount());
            Long createdId = depositLogService.createDepositLog(depositVo);
            addRedisSet(depositSet, depositVo);
            return createdId;

        }catch (Exception e){
            if(isCancel) depositSet.add(beforeAmount, depositVo.getUserId().toString());
            throw e;
        }
    }

    /**
     * 예치금 등록
     * @param depositVo 요청한 예치금 정보
     * @return 등록한 예츠금 로그 ID
     */
    @Override
    public Long placeDeposit(DepositVo depositVo) {
        RLock lock = redissonClient.getLock(LOCK_KEY + depositVo.getAuctionId());
        lock.lock();
        try {
            RScoredSortedSet<String> depositSet = redissonClient.getScoredSortedSet(REDIS_DEPOSIT_KEY + depositVo.getAuctionId());
            return depositSet.contains(depositVo.getUserId().toString()) ? updateDeposit(depositSet, depositVo) :
                    createDeposit(depositSet, depositVo);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 예치금 등록 취소
     * @param auctionId 대상 경매 ID
     * @param userId 대상 유저 ID
     * @return 등록 취소 성공 여부
     */
    @Override
    public Boolean cancelDeposit(Long auctionId, Long userId) {
        RScoredSortedSet<String> depositSet = redissonClient.getScoredSortedSet(REDIS_DEPOSIT_KEY + auctionId);

        if(!depositSet.contains(userId.toString())) throw new CustomException(ErrorCode.REDIS_INTERNAL_ERROR);

        userService.addPoint(userId, depositSet.getScore(userId.toString()).intValue());
        depositLogService.updateLogStatus(auctionId, userId,DepositStatus.CANCEL);
        removeRedisSet(depositSet, userId);
        return true;
    }

    /**
     * 예치금 등록 취소
     * @param auctionId 대상 경매 ID
     * @param userId 대상 유저 ID
     * @return 등록 취소 성공 여부
     */
    @Override
    public Integer getDepositAmount(Long auctionId, Long userId) {
        RScoredSortedSet<String> depositSet = redissonClient.getScoredSortedSet(REDIS_DEPOSIT_KEY + auctionId);

        if(!depositSet.contains(userId.toString())) return 0;

        return depositSet.getScore(userId.toString()).intValue();
    }

    /**
     * 특정 경매의 최소 예치금 량 조회
     * @param auctionId 대상 경매 ID
     * @return 최소 예치금
     */
    @Override
    public Integer getMinDepositAmount(Long auctionId) {
        RScoredSortedSet<String> depositSet = redissonClient.getScoredSortedSet(REDIS_DEPOSIT_KEY + auctionId);
        String minUserId = depositSet.first();
        if(minUserId == null) return -1;
        return depositSet.getScore(minUserId).intValue();
    }

    /**
     * 특정 경매의 현재 예치금 인원수 조회
     * @param auctionId 대상 경매 ID
     * @return 현재 예치금 인원수
     */
    @Override
    public Integer getNowParticipants(Long auctionId) {
        return redissonClient.getScoredSortedSet(REDIS_DEPOSIT_KEY + auctionId).size();
    }

    @Override
    public List<DepositInfoVo> getAllMyDepositInfo(Integer page,Integer size, String email) {
        UserDto.Info userInfo = userService.getUserInfo(email);
        return depositLogService.getAllMyDepositLog(toPageable(page,size,"createdAt"), userInfo.getUserId());
    }

    /**
     * 특정 경매의 예치금 정보 리스트 조회
     * @param auctionId 대상 경매 ID
     * @return 예치금 정보 리스트
     */
    @Override
    public List<DepositInfoVo> getAllDepositInfo(Long auctionId) {
        RScoredSortedSet<String> depositSet = redissonClient.getScoredSortedSet(REDIS_DEPOSIT_KEY + auctionId);
        List<DepositInfoVo> result = new ArrayList<>();

        for(ScoredEntry<String> data :  depositSet.entryRange(0, -1)){
            result.add(DepositInfoVo.builder()
                    .auctionId(auctionId)
                    .userId(Long.valueOf(data.getValue()))
                    .amount(data.getScore().intValue())
                    .build());
        }
        return result;
    }

    private void addRedisSet(RScoredSortedSet<String> depositSet, DepositVo depositVo){
        if(!depositSet.add(depositVo.getAmount(), depositVo.getUserId().toString()))
            throw new CustomException(ErrorCode.REDIS_INTERNAL_ERROR);
    }

    private void removeRedisSet(RScoredSortedSet<String> depositSet, Long userId){
        if(!depositSet.remove(userId.toString()))
            throw new CustomException(ErrorCode.REDIS_INTERNAL_ERROR);
    }

    private Pageable toPageable(int page, int size, String target){
        return PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, target));
    }
}
