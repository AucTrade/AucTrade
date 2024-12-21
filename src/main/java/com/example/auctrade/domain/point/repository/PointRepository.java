package com.example.auctrade.domain.point.repository;


import com.example.auctrade.domain.point.entity.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointRepository extends JpaRepository<Point, Long> {
    Page<Point> findAllByUserId(Pageable pageable, Long userId);

    List<Point> findAllByUserId(Long userId);
}
