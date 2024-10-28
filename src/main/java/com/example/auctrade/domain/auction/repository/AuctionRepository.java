package com.example.auctrade.domain.auction.repository;

import com.example.auctrade.domain.auction.entity.Auction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    Page<Auction> findBySellerEmail(String sellerEmail, Pageable pageable);

    @Query("SELECT a FROM Auction a WHERE a.startTime <= :currentTime AND a.endTime >= :currentTime AND a.sellerEmail = :sellerEmail")
    Page<Auction> findActivateAuctionsBySeller(@Param("currentTime") LocalDateTime currentTime,
                                               @Param("sellerEmail") String sellerEmail,
                                               Pageable pageable);

    @Query("SELECT a FROM Auction a WHERE a.endTime < :currentTime AND a.sellerEmail = :sellerEmail")
    Page<Auction> findEndAuctionsBySeller(@Param("currentTime") LocalDateTime currentTime,
                                          @Param("sellerEmail") String sellerEmail,
                                          Pageable pageable);

    @Query("SELECT a FROM Auction a WHERE a.startTime > :currentTime AND a.sellerEmail = :sellerEmail")
    Page<Auction> findNotStartedAuctionsBySeller(@Param("currentTime") LocalDateTime currentTime,
                                                 @Param("sellerEmail") String sellerEmail,
                                                 Pageable pageable);

    @Query("SELECT a FROM Auction a WHERE a.startTime > :currentTime")
    Page<Auction> findNotStartedAuctions(@Param("currentTime") LocalDateTime currentTime, Pageable pageable);

    @Query("SELECT a.minimumPrice FROM Auction a WHERE a.id = :id")
    Optional<Integer> findMinimumPriceById(@Param("id") Long id);

    @Query("SELECT a.maxParticipants FROM Auction a WHERE a.id = :id")
    Optional<Integer> findMaxParticipantsById(@Param("id") Long id);

    @Query("SELECT a.id FROM Auction a")
    List<Long> findAllAuctionIds();

    @Query("SELECT a.startTime FROM Auction a WHERE a.id = :id")
    LocalDateTime findStartAtById(@Param("id") Long id);

    long count();

    long countBySellerEmail(String sellerEmail);
}
