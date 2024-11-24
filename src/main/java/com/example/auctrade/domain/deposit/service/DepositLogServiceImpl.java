package com.example.auctrade.domain.deposit.service;

import com.example.auctrade.domain.deposit.entity.DepositLog;
import com.example.auctrade.domain.deposit.mapper.DepositMapper;
import com.example.auctrade.domain.deposit.repository.DepositLogRepository;
import com.example.auctrade.domain.deposit.vo.DepositInfoVo;
import com.example.auctrade.domain.deposit.vo.DepositStatus;
import com.example.auctrade.domain.deposit.vo.DepositVo;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;


@Service
@Transactional
@Slf4j(topic = "Deposit Service")
public class DepositLogServiceImpl implements DepositLogService {
    private final DepositLogRepository depositLogRepository;

    public DepositLogServiceImpl(DepositLogRepository depositLogRepository){
        this.depositLogRepository = depositLogRepository;
    }

    /**
     * 예치금 로그 생성
     * @param depositVo 생성할 예치금 정보
     * @return 생성된 로그 ID
     */
    public Long createDepositLog(DepositVo depositVo){
        return depositLogRepository.save(DepositMapper.toEntity(depositVo, DepositStatus.CREATE)).getId();
    }

    /**
     * 예치금 로그 상태 업데이트
     * @param auctionId 업데이트할 예치금 경매 ID
     * @param userId 업데이트할 예치금 유저 ID
     * @param status 업데이트할 상태
     * @return 업데이트된 로그 ID
     */
    public Long updateLogStatus(Long auctionId, Long userId, DepositStatus status) {
        DepositLog depositLog = findByAuctionIdAndUserId(auctionId, userId);
        depositLog.updateStatus(status);
        return depositLog.getId();
    }

    /**
     * 특정 경매방의 현재 유효한 최저 예치금 정보 조회
     * @param auctionId 대상이 될 경매 ID
     * @return 최저 예치금 정보
     */
    @Override
    public DepositInfoVo getMinDepositLog(Long auctionId){
        List<DepositLog> depositLogs = depositLogRepository.findDepositLogByAuctionIdAndStatus(auctionId, DepositStatus.CREATE);
        return depositLogs.isEmpty() ? null : DepositMapper.toDepositInfoVo(depositLogs.get(0));
    }
    /**
     * 특정 경매방의 특정 회원의 예치금 금액 조회
     * @param auctionId 대상이 될 경매 ID
     * @param userId 대상 유저 ID
     * @return 등록한 예치금
     */
    @Override
    public DepositInfoVo getDepositLog(Long auctionId, Long userId) {
        List<DepositLog> depositLogList = depositLogRepository.findDepositLogByAuctionIdAndUserIdAndStatus(auctionId, userId, DepositStatus.CREATE);
        if(depositLogList.isEmpty())
            throw new CustomException(ErrorCode.DEPOSIT_LOG_NOT_FOUND);
        return DepositMapper.toDepositInfoVo(depositLogList.get(0));
    }

    /**
     * 특정 경매방의 특정 회원의 예치금 존재 여부 조히
     * @param auctionId 대상이 될 경매 ID
     * @param userId 대상 유저 ID
     * @return 해당하는 로그 중 상태가 CREATE 인 데이터 존재 여부
     */
    @Override
    public Boolean containsUserId(Long auctionId, Long userId) {
        return depositLogRepository.existsByAuctionIdAndUserIdAndStatus(auctionId, userId, DepositStatus.CREATE);
    }

    /**
     * 특정 경매방의 전체 예치금 정보 조회
     * @param auctionId 대상이 될 경매 ID
     * @return 전체 예치금 정보
     */
    @Override
    public List<DepositInfoVo> getAllDepositLog(Long auctionId, DepositStatus status) {
        return depositLogRepository.findDepositLogByAuctionIdAndStatus(auctionId, status).stream().map(DepositMapper::toDepositInfoVo).toList();
    }
    
    /**
     * 특정 유저의 전체 예치금 정보 조회
     * @param userId 대상이 될 경매 ID
     * @return 전체 예치금 정보
     */
    @Override
    public List<DepositInfoVo> getAllMyDepositLog(Pageable pageable, Long userId) {
        return depositLogRepository.findAllByUserId(pageable, userId).getContent().stream().map(DepositMapper::toDepositInfoVo).toList();
    }

    private DepositLog findByAuctionIdAndUserId(Long auctionId, Long userId){
        return depositLogRepository.findByAuctionIdAndUserId(auctionId, userId).orElseThrow(() -> new CustomException(ErrorCode.DEPOSIT_LOG_NOT_FOUND));
    }
}
