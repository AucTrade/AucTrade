package com.example.auctrade.domain.point.service;

import com.example.auctrade.domain.point.vo.PointInfoVo;
import com.example.auctrade.domain.point.vo.PointVo;

import java.util.List;

public interface PointLogService {
    Long chargePoint(PointVo pointVo);

    Long exchangePoint(PointVo pointVo);

    Integer cancelPoint(Long pointId, Long userId);

    PointInfoVo getPointLog(Long pointId);

    List<PointInfoVo> getAllPointLog(int page, int size, Long userId);
}
