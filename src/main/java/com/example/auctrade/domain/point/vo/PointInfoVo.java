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
public class PointInfoVo {
    private Long id;
    private Integer amount;
    private Integer before;
    private Integer after;
    private PointType type;
    private PointStatus status;
    private String account;
    private LocalDateTime createAt;
}
