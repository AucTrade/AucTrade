package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.auction.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;

    // 경매 생성
    public Auction save(Auction auction) {
        return auctionRepository.save(auction);
    }

    // 경매 전원 조회
    @Transactional(readOnly = true)
    public List<Auction> findAll() {
        return auctionRepository.findAll();
    }

    // 경매 ID 기반 조회
    @Transactional(readOnly = true)
    public Auction findById(Long id) {
        return auctionRepository.findById(id).orElse(null);
    }

    // 경매 상품 등록 유효성 검증
    public boolean existsById(Long id) {
        return auctionRepository.existsById(id);
    }
}
