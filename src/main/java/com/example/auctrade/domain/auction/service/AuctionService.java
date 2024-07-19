package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.auction.mapper.AuctionMapper;
import com.example.auctrade.domain.auction.repository.AuctionRepository;
import com.example.auctrade.domain.product.entity.Product;
import com.example.auctrade.domain.product.repository.ProductRepository;
import com.example.auctrade.domain.user.entity.User;
import com.example.auctrade.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // 경매 생성
    public AuctionDTO save(AuctionDTO dto) {
        User user = userRepository.findById(dto.getSaleUserId()).orElseThrow();
        Product product = productRepository.findById(dto.getProductId()).orElseThrow();

        Auction auction = AuctionMapper.toEntity(dto, product, user);
        auctionRepository.save(auction);

        return AuctionMapper.toDto(auction);
    }

    // 경매 전원 조회(정확히는 경매 아직 시작 안 돼서 노출되는 경매들 조회)
    @Transactional(readOnly = true)
    public List<AuctionDTO> findAll() {
        List<Auction> auctions = auctionRepository.findByStartedFalse();
        return AuctionMapper.toDtoList(auctions);
    }

    // 경매 ID 기반 조회
    @Transactional(readOnly = true)
    public AuctionDTO findById(Long id) {
        Auction auction = auctionRepository.findById(id).orElseThrow();
        return AuctionMapper.toDto(auction);
    }

    // 경매 시작 설정(시간이 지나지 않아도 유저가 조금 일찍 시작하는 로직 고려)
    public void startAuction(Long id) {
        Auction auction = auctionRepository.findById(id).orElseThrow();
        auction.start();
    }

    // 경매 종료 설정
    public void endAuction(Long id) {
        Auction auction = auctionRepository.findById(id).orElseThrow();
        auction.end();
    }
}
