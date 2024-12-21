package com.example.auctrade.domain.point.service;

import com.example.auctrade.domain.point.dto.PointDto;
import com.example.auctrade.domain.point.entity.PointStatus;
import com.example.auctrade.domain.point.entity.PointType;
import com.example.auctrade.domain.point.mapper.PointMapper;
import com.example.auctrade.domain.point.vo.PointInfoVo;
import com.example.auctrade.domain.user.dto.UserDto;
import com.example.auctrade.domain.user.service.UserService;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j(topic = "User Point Service")
public class UserPointServiceImpl implements UserPointService{
    private final UserService userService;
    private final PointLogService pointLogService;

    public UserPointServiceImpl(UserService userService, PointLogService pointLogService){
        this.userService = userService;
        this.pointLogService = pointLogService;
    }

    /**
     * 유저 포인트 충전 요청
     * @param pointDto 포인트 충전 요청 정보
     * @param email 대상 유저 Email
     * @return 충전 성공 여부 반환
     */
    @Override
    public PointDto.Result chargePoint(PointDto.Create pointDto, String email) {
        UserDto.Point userPoint = userService.getPoint(email);

        Long pointId = pointLogService.chargePoint(PointMapper.toPointVo(pointDto, userPoint));
        userService.addPoint(userPoint.getUserId(), pointDto.getAmount());

        return PointMapper.toResultDto(pointId, true);
    }
    /**
     * 유저 포인트 환전 요청
     * @param pointDto 포인트 환전 요청 정보
     * @param email 대상 유저 Email
     * @return 환전 성공 여부 반환
     */
    @Override
    public PointDto.Result exchangePoint(PointDto.Create pointDto, String email) {
        UserDto.Point userPoint = userService.getPoint(email);
        if(userPoint.getPoint() < pointDto.getAmount()) throw new CustomException(ErrorCode.EXCEEDED_POINT_REQUEST);

        Long pointId = pointLogService.exchangePoint(PointMapper.toPointVo(pointDto, userPoint));
        userService.subPoint(userPoint.getUserId(), pointDto.getAmount());

        return PointMapper.toResultDto(pointId, true);
    }

    /**
     * 유저 포인트 충전 취소 요청
     * @param pointId 포인트 충전 취소 대상 ID
     * @param email 대상 유저 Email
     * @return 충전 취소 성공 여부 반환
     */
    @Override
    public PointDto.Result cancelPoint(Long pointId, String email) {
        UserDto.Point userPoint = userService.getPoint(email);
        PointInfoVo pointInfoVo = pointLogService.getPointLog(pointId);

        if(!pointInfoVo.getStatus().equals(PointStatus.CREATED))
            throw new CustomException(ErrorCode.POINT_STATUS_NOT_CREATE);

        Integer amount = pointLogService.cancelPoint(pointId, userPoint.getUserId());

        if(pointInfoVo.getType().equals(PointType.CHARGE)){
            userService.subPoint(userPoint.getUserId(),amount);
            return PointMapper.toResultDto(pointId, true);
        }

        userService.addPoint(userPoint.getUserId(),amount);
        return PointMapper.toResultDto(pointId, true);
    }
    
    /**
     * 유저 포인트 내역 조회
     * @param page 페이지 정보
     * @param size 데이터 수
     * @param email 대상 유저 Email
     * @return 포인트 내역 리스트
     */
    @Override
    public List<PointInfoVo> getAllPoint(int page, int size, String email) {
        return pointLogService.getAllPointLog(page, size, userService.getPoint(email).getUserId());
    }
}
