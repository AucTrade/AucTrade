package com.example.auctrade.domain.point.service;

import com.example.auctrade.domain.point.dto.PointDTO;
import com.example.auctrade.domain.point.entity.PointExchangeLog;
import com.example.auctrade.domain.point.entity.PointLog;
import com.example.auctrade.domain.point.mapper.PointMapper;
import com.example.auctrade.domain.point.repository.PointExchangeLogRepository;
import com.example.auctrade.domain.point.repository.PointLogRepository;
import com.example.auctrade.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PointServiceImpl implements PointService {

    private final PointExchangeLogRepository pointExchangeLogRepository;
    private final PointLogRepository pointLogRepository;
    private final UserService userService;

    @Override
    public PointDTO.Result createPointLog(PointDTO.Recharge request, String email) {
        if(userService.updatePoint(request.getRecharge(), email))
            return PointMapper.toResultDTO(pointLogRepository.save(PointMapper.toPointLog(request, email)));

        return PointMapper.toResultDTO((PointLog) null);
    }

    @Override
    public PointDTO.Result createPointExchangeLog(PointDTO.Refund request, String email) {
        if(userService.updatePoint(-1*request.getRefund(), email))
            return PointMapper.toResultDTO(pointExchangeLogRepository.save(PointMapper.toPointExchangeLog(request, email)));

        return PointMapper.toResultDTO((PointExchangeLog) null);
    }
}
