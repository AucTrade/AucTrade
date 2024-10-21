package com.example.auctrade.domain.auction.repository;

import com.example.auctrade.domain.auction.entity.Auction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
    Page<Auction> findByStartedFalse(Pageable pageable);

    Page<Auction> findBySaleUsernameAndEndedFalse(String username, Pageable pageable);

    Page<Auction> findBySaleUsernameAndEndedTrue(String username, Pageable pageable);

    @Query("SELECT minimumPrice FROM Auction WHERE id = :id")
    Optional<Integer> findMinimumPriceById(Long id);

    @Query("SELECT personnel FROM Auction WHERE id = :id")
    Optional<Integer> findPersonnelById(Long id);

    @Query("SELECT id FROM Auction WHERE started = true AND ended = false")
    List<Long> findAllActiveAuctionIds();

    @Query("SELECT id FROM Auction")
    List<Long> findAllAuctionIds();

    @Query("SELECT startDate FROM Auction WHERE id = :id")
    LocalDateTime findStartAtById(@Param("id") Long id);

    long count();

    long countBySaleUsername(String saleUsername);
}
