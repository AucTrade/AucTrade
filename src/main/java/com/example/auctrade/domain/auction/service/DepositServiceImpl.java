package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.document.DepositLog;
import com.example.auctrade.domain.auction.dto.DepositDTO;
import com.example.auctrade.domain.auction.mapper.DepositMapper;
import com.example.auctrade.domain.auction.repository.DepositLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RKeys;
import org.redisson.api.RLock;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.auctrade.global.constant.Constants.REDIS_DEPOSIT_KEY;
import static com.example.auctrade.global.constant.Constants.REDIS_DEPOSIT_LOCK_KEY;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositServiceImpl implements DepositService {
    private final RedissonClient redissonClient;
    private final DepositLogRepository depositLogRepository;

    private boolean createDeposit(String key, DepositDTO.Create dto) {
        RScoredSortedSet<String> depositSet = redissonClient.getScoredSortedSet(key);

        //예치금을 신규로 등록하는 경우
        if (depositSet.add(dto.getDeposit(), dto.getEmail()))
            return depositLogRepository.save(DepositMapper.toEntity(dto)).getId() != null;

        // 업데이트
        List<DepositLog> res = depositLogRepository.findAllByAuctionIdAndUsername(dto.getAuctionId(), dto.getEmail());
        if (res.isEmpty() || res.get(0).getDeposit() <= dto.getDeposit()) return false;

        DepositLog depositLog = res.get(0);
        depositLog.updateDeposit(dto.getDeposit());
        return depositLogRepository.save(depositLog).getId() != null;
    }

    private boolean updateDeposit(String key, DepositDTO.Create dto) {
        RScoredSortedSet<String> depositSet = redissonClient.getScoredSortedSet(key);
        boolean result = depositSet.add(dto.getDeposit(), dto.getEmail());

        if (result) {
            // 최대 인원 초과 시, 가장 낮은 예치금 제거
            depositSet.removeRangeByRank(dto.getMaxParticipation() - 1, depositSet.size());
            return depositLogRepository.save(DepositMapper.toEntity(dto)).getId() != null;
        }
        // 업데이트
        List<DepositLog> res = depositLogRepository.findAllByAuctionIdAndUsername(dto.getAuctionId(), dto.getEmail());
        if (res.isEmpty() || res.get(0).getDeposit() <= dto.getDeposit()) return false;

        DepositLog depositLog = res.get(0);
        depositLog.updateDeposit(dto.getDeposit());
        return depositLogRepository.save(depositLog).getId() != null;
    }

    /**
     * 특정 경매방의 현재 유효한 최저 예치금 조회
     * @param auctionId 대상이 될 경매 ID
     * @param maxParticipation 최대 예치 인원수
     * @return 예치금 정보 반환
     */
    @Override
    public DepositDTO.GetList getDeposit(Long auctionId, int maxParticipation) {
        return DepositMapper.toListDto(getMinDeposit(auctionId, maxParticipation), getNowParticipation(auctionId));
    }

    /**
     * 특정 경매방의 현재 유효한 최저 예치금 조회
     * @param auctionId 대상이 될 경매 ID
     * @param maxParticipation 최대 참여 인원수
     * @return 최저 예치금
     */
    @Override
    public Integer getMinDeposit(Long auctionId, int maxParticipation) {
        RScoredSortedSet<String> depositSet = redissonClient.getScoredSortedSet(REDIS_DEPOSIT_KEY + auctionId);
        int size = depositSet.size();
        int idx = (size <= maxParticipation) ? 0 : size - maxParticipation;

        ArrayList<String> range = (ArrayList<String>) depositSet.valueRange(idx, idx);
        return range.isEmpty() ? 0 : depositSet.getScore(range.get(0)).intValue();
    }

    /**
     * 예치금 등록
     * @param request 요청한 예치금 정보
     * @return 예치금 등록 성공 여부
     */
    @Override
    public DepositDTO.Result registerDeposit(DepositDTO.Create request) {
        String key = REDIS_DEPOSIT_KEY + request.getAuctionId();
        RLock lock = redissonClient.getLock(REDIS_DEPOSIT_LOCK_KEY + request.getAuctionId());
        lock.lock();
        try {
            // 현재 예치금 요청이 최저 입찰금 보다 낮은 경우
            if (request.getDeposit() < request.getMinPrice()) return DepositMapper.toResultDto(false);

            // 현재 참여 인원이 최대 인원수 미만일 경우 예치금 등록
            if (getNowParticipation(request.getAuctionId()) < request.getMaxParticipation())
                return DepositMapper.toResultDto(createDeposit(key, request));

            // 최대 예치 인원을 넘었을 경우, 최저 예치금보다 크면 갱신
            if (request.getDeposit() > getMinDeposit(request.getAuctionId(), request.getMaxParticipation()))
                return DepositMapper.toResultDto(updateDeposit(key, request));

            return DepositMapper.toResultDto(false);
        } finally {
            lock.unlock(); // 락 해제
        }
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
     * @param auctionId 대상이 될 경매 ID
     * @return 예치 인원수 조회
     */
    @Override
    public Integer getNowParticipation(Long auctionId) {
        return redissonClient.getScoredSortedSet(REDIS_DEPOSIT_KEY + auctionId).size();
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
    @Override
    public void removeMyDepositLog(String email){
        List<DepositLog> logs = depositLogRepository.findAllByUsername(email);
        for(DepositLog log : logs){
            depositLogRepository.delete(log);
        }
    }

    /**
     * 예치금 등록 취소
     * @param auctionId 대상 경매 ID
     * @param email 대상 유저 이메일
     * @return 등록 취소 성공 여부
     */
    @Override
    public DepositDTO.Result cancelDeposit(Long auctionId, String email) {
        RScoredSortedSet<String> depositSet = redissonClient.getScoredSortedSet(REDIS_DEPOSIT_KEY + auctionId);
        return DepositMapper.toResultDto(depositSet.remove(email)) ;
    }

    private List<DepositLog> getDepositLog(Long auctionId) {
        return depositLogRepository.findAllByAuctionId(auctionId.toString());
    }
}
