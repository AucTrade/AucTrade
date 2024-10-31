package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.document.DepositLog;
import com.example.auctrade.domain.auction.dto.DepositDTO;
import com.example.auctrade.domain.auction.mapper.DepositMapper;
import com.example.auctrade.domain.auction.repository.DepositLogRepository;
import com.example.auctrade.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.example.auctrade.global.constant.Constants.REDIS_DEPOSIT_KEY;
import static com.example.auctrade.global.constant.Constants.REDIS_DEPOSIT_LOCK_KEY;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositServiceImpl implements DepositService {
    private final RedissonClient redissonClient;
    private final DepositLogRepository depositLogRepository;
    private final UserService userService;
    private final AuctionService auctionService;


    private boolean createDeposit(RScoredSortedSet<String> depositSet , DepositDTO.Create dto, int maxParticipation){
        long auctionId = dto.getAuctionId();

        //최대 인원수 이하인 경우
        if(getNowParticipation(auctionId) < maxParticipation){
            depositSet.add(dto.getDeposit(), dto.getEmail());
            userService.subPoint(dto.getDeposit(), dto.getEmail());
            depositLogRepository.save(DepositMapper.toEntity(dto));
            return true;
        }

        // 최대 인원수 이상인 경우
        if(getMinDeposit(auctionId) >= dto.getDeposit()) return false;
        if(!cancelDepositByEmail(auctionId, depositSet.first())) return false;

        depositSet.add(dto.getDeposit(), dto.getEmail());
        userService.subPoint(dto.getDeposit(), dto.getEmail());
        depositLogRepository.save(DepositMapper.toEntity(dto));
        return true;
    }

    private boolean updateDeposit(String key, DepositDTO.Create dto, int maxParticipation) {
        RScoredSortedSet<String> depositSet = redissonClient.getScoredSortedSet(key);
        // 요청한 유저가 해당 경매에 처음 등록하는 경우
        if(!depositSet.contains(dto.getEmail())) return createDeposit(depositSet, dto, maxParticipation);

        //자신의 이전 예치금보다 값이 작은 경우
        if(depositSet.getScore(dto.getEmail()) > dto.getDeposit()) return false;

        List<DepositLog> res = depositLogRepository.findAllByAuctionIdAndUsername(dto.getAuctionId(), dto.getEmail());
        if (res.isEmpty()) return false;

        DepositLog depositLog = res.get(0);
        depositLog.updateDeposit(dto.getDeposit());

        if(!userService.subPoint(dto.getDeposit(), dto.getEmail())) return false;
        if(depositLogRepository.save(depositLog).getId() == null) return false;
        depositSet.add(dto.getDeposit(), dto.getEmail());
        return true;
    }

    /**
     * 특정 경매방의 현재 예치금 정보 조회
     * @param auctionId 대상이 될 경매 ID
     * @return 예치금 정보 반환
     */
    @Override
    public DepositDTO.GetList getDepositInfo(Long auctionId) {
        return DepositMapper.toListDto(getMinDeposit(auctionId), getNowParticipation(auctionId));
    }

    /**
     * 특정 경매방의 현재 유효한 최저 예치금 조회
     * @param auctionId 대상이 될 경매 ID
     * @return 최저 예치금
     */
    @Override
    public Integer getMinDeposit(Long auctionId) {
        RScoredSortedSet<String> depositSet = redissonClient.getScoredSortedSet(REDIS_DEPOSIT_KEY + auctionId);
        int size = depositSet.size();
        int maxParticipation = auctionService.getMaxParticipation(auctionId);
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

        if(request.getDeposit() > userService.getPoint(request.getEmail())){
            return DepositMapper.toResultDto(false);
        }

        if (request.getDeposit() < auctionService.getMinimumPrice(request.getAuctionId()))
            return DepositMapper.toResultDto(false);

        String startAt = auctionService.getStartAt(request.getAuctionId());
        if(LocalDateTime.parse(startAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME).isBefore(LocalDateTime.now()))
            return DepositMapper.toResultDto(false);

        String key = REDIS_DEPOSIT_KEY + request.getAuctionId();
        RLock lock = redissonClient.getLock(REDIS_DEPOSIT_LOCK_KEY + request.getAuctionId());
        lock.lock();
        try {
            return DepositMapper
                    .toResultDto(updateDeposit(key, request, auctionService.getMaxParticipation(request.getAuctionId())));

        }catch (Exception e){
            log.error(e.getMessage());
            return DepositMapper.toResultDto(false);
        } finally {
            lock.unlock(); // 락 해제
        }
    }

    /**
     * 특정 유저가 예치금을 넣은 경매 리스트 조회
     * @param email 유저 이메일
     * @param pageable 페이지 정보
     * @return 예치한 경매 리스트
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

    /**
     * 특정 회원의 예치금 히스토리 삭제 ( Admin - Only )
     * @param email 대상 이메일
     */
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
        return DepositMapper.toResultDto(cancelDepositByEmail(auctionId, email)) ;
    }

    private boolean cancelDepositByEmail(Long auctionId, String email){
        RScoredSortedSet<String> depositSet = redissonClient.getScoredSortedSet(REDIS_DEPOSIT_KEY + auctionId);
        Double cancelPoint = depositSet.getScore(email);

        if(cancelPoint == null) return false;
        return userService.addPoint(cancelPoint.intValue(), email) && depositSet.remove(email);
    }

    private List<DepositLog> getDepositLog(Long auctionId) {
        return depositLogRepository.findAllByAuctionId(auctionId.toString());
    }
}
