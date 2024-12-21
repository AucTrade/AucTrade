package com.example.auctrade.domain.bid.repository;

import com.example.auctrade.domain.bid.entity.BidLog;
import com.example.auctrade.domain.bid.vo.BidStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidLogRepository extends JpaRepository<BidLog, Long> {

    Optional<BidLog> findByAuctionIdAndUserId(Long auctionId, Long userId);

    Boolean existsByAuctionIdAndUserIdAndStatus(Long auctionId, Long userId, BidStatus status);

    List<BidLog> findAllByAuctionId(Pageable pageable, Long auctionId);

    @Query("SELECT d FROM BidLog d " +
            "WHERE d.auctionId = :auctionId AND d.userId = :userId AND d.status = :status " +
            "ORDER BY d.createdAt DESC")
    List<BidLog> findAllByAuctionIdAndUserIdAndStatus(@Param("auctionId") Long auctionId, @Param("userId") Long userId, @Param("status") BidStatus status);

    Page<BidLog> findAllByUserId(Pageable pageable, Long userId);


}
