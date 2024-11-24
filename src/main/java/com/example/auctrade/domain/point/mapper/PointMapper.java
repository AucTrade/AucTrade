package com.example.auctrade.domain.point.mapper;

import com.example.auctrade.domain.point.dto.PointDto;
import com.example.auctrade.domain.point.entity.Point;
import com.example.auctrade.domain.point.entity.PointStatus;
import com.example.auctrade.domain.point.entity.PointType;
import com.example.auctrade.domain.point.vo.PointInfoVo;
import com.example.auctrade.domain.point.vo.PointVo;
import com.example.auctrade.domain.user.dto.UserDto;

public class PointMapper {
    private PointMapper(){}

    public static Point toEntity(PointVo pointVo, PointStatus status, PointType type) {
        return pointVo == null ? null : Point.builder()
                .userId(pointVo.getUserId())
                .amount(pointVo.getAmount())
                .account(pointVo.getAccount())
                .balance(pointVo.getBalance())
                .status(status)
                .type(type)
                .build();
    }

    public static PointVo toPointVo(PointDto.Create pointDto, UserDto.Point userPoint){
        return pointDto == null ? null : PointVo.builder()
                .userId(userPoint.getUserId())
                .balance(userPoint.getPoint())
                .amount(pointDto.getAmount())
                .account(pointDto.getAccount())
                .build();
    }
    public static PointDto.Result toResultDto(Long pointId, Boolean isSuccess){
        return pointId == null ? null : new PointDto.Result(pointId, isSuccess);
    }

    public static PointInfoVo toPointInfoVo(Point point){
        if(point == null) return null;
        int before;
        int after;

        if(PointStatus.CREATED.equals(point.getStatus())){
            before = point.getBalance();
            after = before + (PointType.CHARGE.equals(point.getType()) ? 1 : -1) * point.getAmount();
        }
        else{
            before = point.getBalance();
            after = before + (PointType.CHARGE.equals(point.getType()) ? -1 : 1) * point.getAmount();
        }

        return PointInfoVo.builder()
                .id(point.getId())
                .amount(point.getAmount())
                .before(before)
                .after(after)
                .type(point.getType())
                .status(point.getStatus())
                .account(point.getAccount())
                .createAt(point.getCreatedAt())
                .build();
    }
}

