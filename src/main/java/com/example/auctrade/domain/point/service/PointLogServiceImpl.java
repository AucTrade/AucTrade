package com.example.auctrade.domain.point.service;

import com.example.auctrade.domain.point.entity.Point;
import com.example.auctrade.domain.point.entity.PointStatus;
import com.example.auctrade.domain.point.entity.PointType;
import com.example.auctrade.domain.point.mapper.PointMapper;
import com.example.auctrade.domain.point.repository.PointRepository;
import com.example.auctrade.domain.point.vo.PointInfoVo;
import com.example.auctrade.domain.point.vo.PointVo;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j(topic = "Point Service")
public class PointLogServiceImpl implements PointLogService {
    private final PointRepository pointRepository;

    public PointLogServiceImpl(PointRepository pointRepository){
        this.pointRepository = pointRepository;
    }

    /**
     * 유저 포인트 충전 요청
     * @param pointVo 포인트 충전 요청 정보
     * @return 충전 포인트 량 반환
     */
    @Override
    public Long chargePoint(PointVo pointVo) {
        return pointRepository.save(PointMapper.toEntity(pointVo, PointStatus.CREATED, PointType.CHARGE)).getId();
    }

    /**
     * 유저 포인트 환전 요청
     * @param pointVo 포인트 환전 요청 정보
     * @return 환전 포인트 량 반환
     */
    @Override
    public Long exchangePoint(PointVo pointVo){
        return pointRepository.save(PointMapper.toEntity(pointVo, PointStatus.CREATED, PointType.EXCHANGE)).getId();
    }

    /**
     * 유저 포인트 충전/환전 취소 요청
     * @param pointId 대상 포인트 ID
     * @param userId 대상 유저 ID
     * @return 취소된 포인트 량 반환
     */
    @Override
    public Integer cancelPoint(Long pointId, Long userId) {
        Point point = findById(pointId);
        if(!point.getUserId().equals(userId))
            throw new CustomException(ErrorCode.POINT_USER_NOT_EQUAL);
        point.updateStatus(PointStatus.CANCELED);
        return point.getAmount();
    }
    
    /**
     * 포인트 내역 조회
     * @param pointId 대상 포인트 ID
     * @return 포인트 정보 반환
     */
    @Override
    public PointInfoVo getPointLog(Long pointId) {
        Point point = findById(pointId);
        return PointMapper.toPointInfoVo(point);
    }

    /**
     * 특정 유저의 포인트 내역 리스트 조회
     * @param page 페이지 정보
     * @param size 페이지 내 데이터 갯수
     * @param userId 대상 유저 ID
     * @return 포인트 내역 리스트
     */
    @Override
    public List<PointInfoVo> getAllPointLog(int page, int size, Long userId){
        return pointRepository.findAllByUserId(toPageable(page, size, "createdAt"), userId).stream().map(PointMapper::toPointInfoVo).toList();
    }

    private Point findById(Long pointId){
        return pointRepository.findById(pointId).orElseThrow(()-> new CustomException(ErrorCode.POINT_NOT_FOUND));
    }

    private Pageable toPageable(int page, int size, String target){
        return PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, target));
    }
}
