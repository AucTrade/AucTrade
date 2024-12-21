package com.example.auctrade.domain.point.vo;

import com.example.auctrade.domain.point.entity.PointStatus;
import com.example.auctrade.domain.point.entity.PointType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class PointVo {
    private Long userId;
    private Integer amount;
    private Integer balance;
    private String account;
}
