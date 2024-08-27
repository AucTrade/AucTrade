package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.document.DepositLog;
import com.example.auctrade.domain.auction.dto.DepositDTO;
import com.example.auctrade.domain.auction.mapper.DepositMapper;
import com.example.auctrade.domain.auction.repository.DepositLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.example.auctrade.global.constant.Constants.REDIS_DEPOSIT_KEY;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositServiceImpl implements DepositService {
    private final RedissonClient redissonClient;
    private final DepositLogRepository depositLogRepository;

    private boolean createDeposit(String key, String email, String startAt, DepositDTO.Create dto) {
        RScoredSortedSet<String> depositSet = redissonClient.getScoredSortedSet(key);
        Boolean result = depositSet.add(dto.getDeposit(), email);

        if (result == null) {
            return false;
        } else if (result) {
            // 신규 등록
            depositLogRepository.save(new DepositLog(dto, startAt, email));
        } else {
            // 업데이트
            List<DepositLog> res = depositLogRepository.findAllByAuctionIdAndUsername(dto.getAuctionId(), email);
            if (res.isEmpty() || res.get(0).getDeposit() <= dto.getDeposit()) return false;

            DepositLog depositLog = res.get(0);
            depositLog.updateDeposit(dto.getDeposit());
            depositLogRepository.save(depositLog);
        }
        return true;
    }

    private boolean updateDeposit(String key, String email, String startAt, DepositDTO.Create dto, int maxPersonnel) {
        RScoredSortedSet<String> depositSet = redissonClient.getScoredSortedSet(key);
        Boolean result = depositSet.add(dto.getDeposit(), email);

        if (result == null) {
            return false;
        } else if (result) {
            // 최대 인원 초과 시, 가장 낮은 예치금 제거
            depositSet.removeRangeByRank(maxPersonnel - 1, depositSet.size());
            depositLogRepository.save(new DepositLog(dto, startAt, email));
        } else {
            // 업데이트
            List<DepositLog> res = depositLogRepository.findAllByAuctionIdAndUsername(dto.getAuctionId(), email);
            if (res.isEmpty() || res.get(0).getDeposit() <= dto.getDeposit()) return false;

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
    public DepositDTO.List getDeposit(Long auctionId, int maxPersonnel) {
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
        RScoredSortedSet<String> depositSet = redissonClient.getScoredSortedSet(REDIS_DEPOSIT_KEY + auctionId);
        int size = depositSet.size();
        int idx = (size <= maxPersonnel) ? 0 : size - maxPersonnel;

        ArrayList<String> range = (ArrayList<String>) depositSet.valueRange(idx, idx);
        return range.isEmpty() ? 0 : depositSet.getScore(range.get(0)).longValue();
    }

    @Override
    public DepositDTO.Result depositPrice(DepositDTO.Create requestDto, String email, int maxPersonnel, String startDate) {
        String key = REDIS_DEPOSIT_KEY + requestDto.getAuctionId();

        if (getCurrentPersonnel(requestDto.getAuctionId()) < maxPersonnel - 1)
            return DepositMapper.toResultDto(createDeposit(key, email, startDate, requestDto));

        //최대 예치 인원을 넘은 경우 최저값 보다 큰 경우 갱신
        if (requestDto.getDeposit() > getMinDeposit(requestDto.getAuctionId(), maxPersonnel))
            return DepositMapper.toResultDto(updateDeposit(key, email, startDate, requestDto, maxPersonnel));

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
        List<Long> auctions = depositLogRepository.findAllAuctionIdByUsername(pageable, email).stream().map(DepositLog::getAuctionId).toList();
        log.info("Mongo DB 에서 추출한 경매 id들: {}", auctions);

        return auctions;
    }

    /**
     * 특정 경매방의 예치 인원수 조회
     *
     * @param auctionId 대상이 될 경매 ID
     * @return 예치 인원수 조회
     */
    @Override
    public Integer getCurrentPersonnel(Long auctionId) {
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

    private List<DepositLog> getDepositLog(Long auctionId) {
        return depositLogRepository.findAllByAuctionId(auctionId.toString());
    }
}
