package com.example.auctrade.domain.auction.repository;

import com.example.auctrade.domain.auction.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
    List<Auction> findByStartedFalse();
    @Query("SELECT minimumPrice FROM Auction WHERE id = :id")
    Optional<Integer> findMinimumPriceById(Long id);
    @Query("SELECT personnel FROM Auction WHERE id = :id")
    Optional<Integer> findPersonnelById(Long id);
}
