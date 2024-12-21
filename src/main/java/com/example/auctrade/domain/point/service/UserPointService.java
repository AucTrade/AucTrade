package com.example.auctrade.domain.point.service;

import com.example.auctrade.domain.point.dto.PointDto;
import com.example.auctrade.domain.point.vo.PointInfoVo;
import com.example.auctrade.domain.user.dto.UserDto;

import java.util.List;

public interface UserPointService {
    PointDto.Result chargePoint(PointDto.Create pointDto, String email);

    PointDto.Result exchangePoint(PointDto.Create pointDto, String email);

    PointDto.Result cancelPoint(Long pointId, String email);

    List<PointInfoVo> getAllPoint(int page, int size,String email);
}
