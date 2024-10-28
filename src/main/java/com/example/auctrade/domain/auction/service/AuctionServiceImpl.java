package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.auction.mapper.AuctionMapper;
import com.example.auctrade.domain.auction.repository.AuctionRepository;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuctionServiceImpl implements AuctionService {

    private final AuctionRepository auctionRepository;

    /**
     * 경매 등록
     * @param request 등록할 경매 정보
     * @param productId 등록한 물품 ID
     * @param saleUsername 등록한 유저 email
     * @return 생성된 경매 ID
     */
    @Override
    public Long createAuction(AuctionDTO.Create request, long productId, String saleUsername) {
        return auctionRepository.save(AuctionMapper.toEntity(request, productId, saleUsername)).getId();
    }

    /**
     * 경매 정보 페이지 조회
     * @param pageable 조회할 페이지 정보
     * @return 페이징한 모든 상태의 경매 정보
     */
    @Override
    public List<AuctionDTO.GetList> getAuctions(Pageable pageable) {
        return auctionRepository.findAll(pageable).stream().map(AuctionMapper::toGetListDto).toList();
    }

    /**
     * 경매 조회
     * @param id 조회할 경매 ID
     * @return 조회한 경매 정보
     */
    @Override
    public AuctionDTO.Get getAuctionById(long id) {
        return AuctionMapper.toGetDto(findAuction(id));
    }


    /**
     * 시작하기 전 경매 리스트 조회
     * @param pageable 조회할 페이지 정보
     * @return 아직 실행 되지 않은 모든 경매 정보
     */
    @Override
    public List<AuctionDTO.GetList> getNotStartedAuctions(Pageable pageable) {
        Page<Auction> auctions = auctionRepository.findNotStartedAuctions(LocalDateTime.now(), pageable);
        return auctions.get().map(AuctionMapper::toGetListDto).toList();
    }

    /**
     * 시작하기 전 리스트 조회
     * @param pageable 조회할 페이지 정보
     * @param email 조회할 경매 판매자 이메일
     * @return 종료된 해당 경매 정보
     */
    @Override
    public AuctionDTO.GetPage getMyNotStartedAuctions(Pageable pageable, String email) {
        Page<Auction> auctions = auctionRepository.findNotStartedAuctionsBySeller(LocalDateTime.now(), email, pageable);
        return new AuctionDTO.GetPage(auctions.get().map(AuctionMapper::toGetListDto).toList(), auctions.getTotalPages());
    }

    /**
     * 경매 리스트 조회
     * @param pageable 조회할 페이지 정보
     * @param email 조회할 경매 판매자 이메일
     * @return 진행 중인 해당 경매 정보
     */
    @Override
    public List<AuctionDTO.GetList> getAllMyAuctions(Pageable pageable, String email) {
        return auctionRepository.findBySellerEmail(email, pageable).get().map(AuctionMapper::toGetListDto).toList();
    }

    /**
     * 진행 중인 경매 리스트 조회
     * @param pageable 조회할 페이지 정보
     * @param email 조회할 경매 판매자 이메일
     * @return 진행 중인 해당 경매 정보
     */
    @Override
    public AuctionDTO.GetPage getMyActiveAuctions(Pageable pageable, String email) {
        Page<Auction> auctions = auctionRepository.findActivateAuctionsBySeller(LocalDateTime.now(), email, pageable);
        return new AuctionDTO.GetPage(auctions.get().map(AuctionMapper::toGetListDto).toList(), auctions.getTotalPages());
    }

    /**
     * 종료된 경매 리스트 조회
     * @param pageable 조회할 페이지 정보
     * @param email 조회할 경매 판매자 이메일
     * @return 종료된 해당 경매 정보
     */
    @Override
    public AuctionDTO.GetPage getMyEndedAuctions(Pageable pageable, String email) {
        Page<Auction> auctions = auctionRepository.findEndAuctionsBySeller(LocalDateTime.now(), email, pageable);
        return new AuctionDTO.GetPage(auctions.get().map(AuctionMapper::toGetListDto).toList(), auctions.getTotalPages());
    }

    /**
     * 경매 시작 시간 조회
     * @param id 조회할 경매 ID
     * @return 경매 시작 시간
     */
    @Override
    public String getStartAt(Long id) {
        return auctionRepository.findStartAtById(id).toString();
    }

    /**
     * 경매 최대 인원 조회
     * @param id 조회할 경매 ID
     * @return 해당 경매 최대 인원 수
     */
    @Override
    public int getMaxParticipation(Long id){
        return auctionRepository.findMaxParticipantsById(id).orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));
    }

    /**
     * 경매 최소 입찰금 조회
     * @param id 조회할 경매 ID
     * @return 해당 최소 입찰금
     */
    @Override
    public int getMinimumPrice(Long id){
        return auctionRepository.findMinimumPriceById(id).orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));
    }

    private Auction findAuction(Long id){
        return auctionRepository.findById(id).orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));
    }
}
