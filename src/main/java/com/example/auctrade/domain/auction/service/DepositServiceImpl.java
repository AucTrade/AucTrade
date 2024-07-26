package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.DepositDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import java.util.Set;

import static com.example.auctrade.global.constant.Constants.REDIS_DEPOSIT_KEY;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositServiceImpl implements DepositService {
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 경매 예치금 입금
     *
     * @param request 입금할 예치금 정보
     * @return 저장된 예치금 순위 정보;
     */
    public Boolean save(DepositDTO.Create request) {
        String key = REDIS_DEPOSIT_KEY + request.getAuctionId();
        int curNum = getCurrentPersonnel(request.getAuctionId());

        if(curNum < request.getMaxPersonnel()-1){
            redisTemplate.opsForZSet().add(key, request.getUsername(), request.getPrice());
            return true;
        }
        if(request.getPrice() > getMinDeposit(request.getAuctionId(),request.getMaxPersonnel())){
            redisTemplate.opsForZSet().add(key, request.getUsername(), request.getPrice());
            redisTemplate.opsForZSet().removeRange(key,0, request.getMaxPersonnel()-1);
            return true;
        }
        return false;
    }

    /**
     * 특정 경매방의 현재 유효한 최저 예치금 조회
     *
     * @param auctionId 대상이 될 경매 ID
     * @return 최저 예치금
     */
    public Long getMinDeposit(Long auctionId, int maxPersonnel) {
        Set<ZSetOperations.TypedTuple<String>> rangeWithScores;
        Long val = redisTemplate.opsForZSet().zCard(REDIS_DEPOSIT_KEY + auctionId);
        long idx = (val == null || val <= maxPersonnel) ? 0 : val - maxPersonnel;

        rangeWithScores = redisTemplate.opsForZSet().rangeWithScores(REDIS_DEPOSIT_KEY + auctionId, idx, idx);

        return (rangeWithScores == null || rangeWithScores.isEmpty()) ? 0 :
                rangeWithScores.iterator().next().getScore().longValue();
    }

    /**
     * 특정 경매방의 예치 인원수 조회
     *
     * @param auctionId 대상이 될 경매 ID
     * @return 예치 인원수 조회
     */
    public Integer getCurrentPersonnel(Long auctionId) {
        Long val = redisTemplate.opsForZSet().zCard(REDIS_DEPOSIT_KEY + auctionId);
        return (val == null) ? 0 : val.intValue();
    }

}