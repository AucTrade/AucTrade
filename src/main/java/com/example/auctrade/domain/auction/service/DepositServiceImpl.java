package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.document.DepositLog;
import com.example.auctrade.domain.auction.dto.DepositDTO;
import com.example.auctrade.domain.auction.mapper.DepositMapper;
import com.example.auctrade.domain.auction.repository.DepositLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.example.auctrade.global.constant.Constants.REDIS_DEPOSIT_KEY;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositServiceImpl implements DepositService {
    private final RedisTemplate<String, String> redisTemplate;
    private final DepositLogRepository depositLogRepository;

    private boolean createDeposit(String key, String email, String startAt, DepositDTO.Create dto) {
        Boolean result = redisTemplate.opsForZSet().add(key, email, dto.getDeposit());

        if(result == null) {
                return false;
        }else if(result){
                //만약 이전에 값이 없으면 신규 등록
            depositLogRepository.save(new DepositLog(dto, startAt, email));
        } else {
                // 있으면 update
            List<DepositLog> res = depositLogRepository.findAllByAuctionIdAndUsername(dto.getAuctionId(), email);
            if(res.isEmpty() || res.get(0).getDeposit() <= dto.getDeposit()) return false;

            DepositLog depositLog = res.get(0);
            depositLog.updateDeposit(dto.getDeposit());
            depositLogRepository.save(depositLog);
        }
        return true;
    }

    private boolean updateDeposit(String key, String email, String startAt, DepositDTO.Create dto, int maxPersonnel) {
        Boolean result = redisTemplate.opsForZSet().add(key, email, dto.getDeposit());
        if(result == null){
            return false;
        }else if(result){
            redisTemplate.opsForZSet().removeRange(key,0, maxPersonnel-1L);
            depositLogRepository.save(new DepositLog(dto, startAt, email));
        }else{
            List<DepositLog> res = depositLogRepository.findAllByAuctionIdAndUsername(dto.getAuctionId(), email);
            if(res.isEmpty() || res.get(0).getDeposit() <= dto.getDeposit()) return false;

            DepositLog depositLog = res.get(0);
            depositLog.updateDeposit(dto.getDeposit());
            depositLogRepository.save(depositLog);
        }
        return true;
    }

    /**
     * 특정 경매방의 현재 유효한 최저 예치금 조회
     * @param auctionId 대상이 될 경매 ID
     * @param maxPersonnel 최대 예치 인원수
     * @return 예치금 정보 반환
     */
    @Override
    public DepositDTO.List getDeposit(Long auctionId, int maxPersonnel){
        return DepositMapper.toListDto(getMinDeposit(auctionId, maxPersonnel), getCurrentPersonnel(auctionId));
    }

    /**
     * 특정 경매방의 현재 유효한 최저 예치금 조회
     *
     * @param auctionId 대상이 될 경매 ID
     * @return 최저 예치금
     */
    @Override
    public Long getMinDeposit(Long auctionId, int maxPersonnel) {
        Set<ZSetOperations.TypedTuple<String>> rangeWithScores;
        Long val = redisTemplate.opsForZSet().zCard(REDIS_DEPOSIT_KEY + auctionId);
        long idx = (val == null || val <= maxPersonnel) ? 0 : val - maxPersonnel;

        rangeWithScores = redisTemplate.opsForZSet().rangeWithScores(REDIS_DEPOSIT_KEY + auctionId, idx, idx);

        return (rangeWithScores == null || rangeWithScores.isEmpty()) ? 0 :
                rangeWithScores.iterator().next().getScore().longValue();
    }

    @Override
    public DepositDTO.Result depositPrice(DepositDTO.Create requestDto, String email, int maxPersonnel, String startDate) {
        String key = REDIS_DEPOSIT_KEY + requestDto.getAuctionId();

        if(getCurrentPersonnel(requestDto.getAuctionId()) < maxPersonnel-1)
             return DepositMapper.toResultDto(createDeposit(key, email, startDate, requestDto));

        //최대 예치 인원을 넘은 경우 최저값 보다 큰 경우 갱신
        if(requestDto.getDeposit() > getMinDeposit(requestDto.getAuctionId(), maxPersonnel))
            return DepositMapper.toResultDto(updateDeposit(key,email,startDate,requestDto,maxPersonnel));

        return DepositMapper.toResultDto(false);
    }

    /**
     * 특정 유저가 입찰한 경매 리스트 조회
     * @param email 유저 이메일
     * @param pageable 페이지 정보
     * @return 입찰한 경매 리스트
     */
    @Override
    public List<Long> getMyAuctions(Pageable pageable, String email) {
        return depositLogRepository.findAllAuctionIdByUsername(pageable, email).stream().map(DepositLog::getAuctionId).toList();
    }

    /**
     * 특정 경매방의 예치 인원수 조회
     *
     * @param auctionId 대상이 될 경매 ID
     * @return 예치 인원수 조회
     */
    @Override
    public Integer getCurrentPersonnel(Long auctionId) {
        Long cnt = redisTemplate.opsForZSet().size(REDIS_DEPOSIT_KEY + auctionId);
        return (cnt==null) ? 0 : Math.toIntExact(cnt);
    }

    /**
     * 특정 회원의 예치금 리스트 총 갯수 반환
     * @param email 대상 이메일
     * @return 예치금 리스트 총 갯수
     */
    @Override
    public Long getMyDepositSize(String email) {
        return depositLogRepository.countByUsername(email);
    }

    private List<DepositLog> getDepositLog(Long auctionId){
        return depositLogRepository.findAllByAuctionId(auctionId.toString());
    }
}