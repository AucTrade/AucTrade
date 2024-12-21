package com.example.auctrade.domain.bid.service;

import com.example.auctrade.domain.bid.entity.BidLog;
import com.example.auctrade.domain.bid.mapper.BidMapper;
import com.example.auctrade.domain.bid.repository.BidLogRepository;
import com.example.auctrade.domain.bid.vo.BidInfoVo;
import com.example.auctrade.domain.bid.vo.BidStatus;
import com.example.auctrade.domain.bid.vo.BidVo;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
@Slf4j(topic = "Bid Log Service")
public class BidLogServiceImpl implements BidLogService {
    private final BidLogRepository bidLogRepository;

    public BidLogServiceImpl(BidLogRepository bidLogRepository){
        this.bidLogRepository = bidLogRepository;
    }

    /**
     * 입찰 로그 생성
     * @param bidVo 생성할 입찰 정보
     * @return 생성된 로그 ID
     */
    public Long createBidLog(BidVo bidVo, BidStatus status){
        return bidLogRepository.save(BidMapper.toEntity(bidVo, status)).getId();
    }

    /**
     * 입찰 로그 상태 업데이트
     * @param auctionId 업데이트할 예치금 경매 ID
     * @param userId 업데이트할 예치금 유저 ID
     * @param status 업데이트할 상태
     * @return 업데이트된 로그 ID
     */
    public Long updateLogStatus(Long auctionId, Long userId, BidStatus status) {
        BidLog bidLog = findByAuctionIdAndUserId(auctionId, userId);
        bidLog.updateStatus(status);
        return bidLog.getId();
    }

    /**
     * 특정 경매방의 특정 회원의 입찰 금액 조회
     * @param auctionId 대상이 될 경매 ID
     * @param userId 대상 유저 ID
     * @return 입찰 정보
     */
    @Override
    public BidInfoVo getBidLog(Long auctionId, Long userId) {
        List<BidLog> bidLogList = bidLogRepository.findAllByAuctionIdAndUserIdAndStatus(auctionId, userId, BidStatus.CREATE);
        if(bidLogList.isEmpty())
            throw new CustomException(ErrorCode.BID_LOG_NOT_FOUND);
        return BidMapper.toBidInfoVo(bidLogList.get(0));
    }

    /**
     * 특정 경매방의 특정 회원의 입찰 존재 여부 조히
     * @param auctionId 대상이 될 경매 ID
     * @param userId 대상 유저 ID
     * @return 해당하는 로그 중 상태가 CREATE 인 데이터 존재 여부
     */
    @Override
    public Boolean containsUserId(Long auctionId, Long userId) {
        return bidLogRepository.existsByAuctionIdAndUserIdAndStatus(auctionId, userId, BidStatus.CREATE);
    }

    /**
     * 특정 경매방의 전체 입찰 정보 조회
     * @param auctionId 대상이 될 경매 ID
     * @return 입찰 리스트
     */
    @Override
    public List<BidInfoVo> getAllBidLog(Pageable pageable, Long auctionId) {
        return bidLogRepository.findAllByAuctionId(pageable, auctionId).stream().map(BidMapper::toBidInfoVo).toList();
    }
    
    /**
     * 특정 유저의 전체 예치금 정보 조회
     * @param userId 대상이 될 경매 ID
     * @return 전체 예치금 정보
     */
    @Override
    public List<BidInfoVo> getAllMyBidLog(Pageable pageable, Long userId) {
        return bidLogRepository.findAllByUserId(pageable, userId).getContent().stream().map(BidMapper::toBidInfoVo).toList();
    }

    private BidLog findByAuctionIdAndUserId(Long auctionId, Long userId){
        return bidLogRepository.findByAuctionIdAndUserId(auctionId, userId).orElseThrow(() -> new CustomException(ErrorCode.BID_LOG_NOT_FOUND));
    }
}
