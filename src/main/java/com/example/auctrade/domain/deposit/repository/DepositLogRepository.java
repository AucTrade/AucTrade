package com.example.auctrade.domain.deposit.repository;


import com.example.auctrade.domain.deposit.entity.DepositLog;
import com.example.auctrade.domain.deposit.vo.DepositStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface DepositLogRepository extends JpaRepository<DepositLog, Long> {
    Optional<DepositLog> findByAuctionIdAndUserId(Long auctionId, Long userId);

    Boolean existsByAuctionIdAndUserIdAndStatus(Long auctionId, Long userId, DepositStatus status);

    @Query("SELECT d FROM DepositLog d " +
            "WHERE d.auctionId = :auctionId AND d.status = :status " +
            "ORDER BY d.amount ASC, d.createdAt DESC")
    List<DepositLog> findAllByAuctionIdAndStatus(@Param("auctionId") Long auctionId, @Param("status") DepositStatus status);

    @Query("SELECT d FROM DepositLog d " +
            "WHERE d.auctionId = :auctionId AND d.userId = :userId AND d.status = :status " +
            "ORDER BY d.amount ASC, d.createdAt DESC")
    List<DepositLog> findAllByAuctionIdAndUserIdAndStatus(@Param("auctionId") Long auctionId, @Param("userId") Long userId, @Param("status") DepositStatus status);

    Page<DepositLog> findAllByUserId(Pageable pageable, Long userId);
}
