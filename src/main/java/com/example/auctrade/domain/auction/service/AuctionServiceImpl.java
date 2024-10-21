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
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuctionServiceImpl implements AuctionService {

    private final AuctionRepository auctionRepository;

    /**
     * 경매 등록
     * @param request 회원이 입력한 경매 정보
     * @return 생성된 경매 정보
     */
    @Override
    public Long createAuction(AuctionDTO.Create request, long productId, String saleUsername) {
        return auctionRepository.save(AuctionMapper.toEntity(request, productId, saleUsername)).getId();
    }
    /**
     * 경매 리스트 전체 조회
     * @return 아직 실행 되지 않은 모든 경매 정보
     */
    @Override
    public List<AuctionDTO.GetList> findAll(Pageable pageable) {
        return auctionRepository.findAll(pageable).stream().map(AuctionMapper::toGetListDto).toList();
    }
    /**
     * 경매 리스트 전체 조회
     * @return 아직 실행 되지 않은 모든 경매 정보
     */
    @Override
    public AuctionDTO.Get findById(long id) {
        return AuctionMapper.toGetDto(findAuction(id));
    }

    /**
     * 시작하기 전 경매 리스트 전체 조회
     * @return 아직 실행 되지 않은 모든 경매 정보
     */
    @Override
    public List<AuctionDTO.GetList> getMyAuctions(Pageable pageable, String email) {
        return auctionRepository.findByStartedFalse(pageable).stream().map(AuctionMapper::toGetListDto).toList();
    }

    // 내가 개설한 것들 가지고 오기
    @Override
    public AuctionDTO.GetPage getMyOpeningAuctions(Pageable pageable, String email) {
        Page<Auction> auctions = auctionRepository.findBySaleUsernameAndEndedFalse(email, pageable);
        return new AuctionDTO.GetPage(auctions.get().map(AuctionMapper::toGetListDto).toList(), auctions.getTotalPages());
    }

    @Override
    public AuctionDTO.GetPage getMyEndedAuctions(Pageable pageable, String email) {
        Page<Auction> auctions = auctionRepository.findBySaleUsernameAndEndedTrue(email, pageable);
        return new AuctionDTO.GetPage(auctions.get().map(AuctionMapper::toGetListDto).toList(), auctions.getTotalPages());
    }

    @Override
    public List<AuctionDTO.GetList> getMyAuctions(List<Long> ids) {
        return auctionRepository.findAllById(ids).stream().map(AuctionMapper::toGetListDto).toList();
    }

    /**
     * 진행중인 경매 리스트 전체 조회
     * @return 진행 중인 경매 Id 리스트
     */
    @Override
    public List<Long> findAllActiveAuctionIds() {
        return auctionRepository.findAllAuctionIds().stream().toList();
    }

    /**
     * 진행중인 경매 리스트 전체 조회
     * @return 진행 중인 경매 Id 리스트
     */
    @Override
    public int getLastPageNum(String email, int size) {
        int totalCount = (int) auctionRepository.countBySaleUsername(email);
        return  totalCount/size + ((totalCount%size == 0) ? 0 : 1);
    }

    @Override
    public String getStartAt(Long id) {
        return auctionRepository.findStartAtById(id).toString();
    }

    /**
     * 경매 리스트 예치금을 넣을 수 있는 경매 리스트 조회
     * @return 아직 실행 되지 않은 모든 경매 정보
     */
    // 즉 여기서 아직 시작되지 않은 경매를 반환시키는 중
    @Override
    public List<AuctionDTO.GetList> getDepositList(Pageable pageable) {
        List<Auction> auctions = auctionRepository.findByStartedFalse(pageable).stream().toList();
        List<Auction> unStartedAuctions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Auction auction : auctions) {
            log.info("조회되는 경매 제목: {}", auction.getTitle());

            // 시작하지 않은 경매들이 리스트에 올라야 한다.
            // 그리고 이 메소드는 클라이언트에 데이터를 흩뿌리는 용도의 시발점
            if (!auction.checkAndStartAuction(now)) {
                log.info("경매 아직 시작 안 함 // 지금: {} // 경매 시작: {}", now, auction.getStartDate());
                unStartedAuctions.add(auction);
            }
        }

        return unStartedAuctions.stream().map(AuctionMapper::toGetListDto).toList();
    }

    /**
     * 지정된 경매 시작일 보다 빠르게 경매를 시작
     * @param id 경매 id
     */
    @Override
    public void startAuction(Long id) {
        findAuction(id).start();
    }

    /**
     * 지정된 경매 종료일 보다 빠르게 경매를 종료
     * @param id 경매 id
     */
    @Override
    public void endAuction(Long id) {
        findAuction(id).end();
    }

    @Override
    public int getMaxPersonnel(Long id){
        return auctionRepository.findPersonnelById(id).orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));
    }

    @Override
    public int getMinimumPrice(Long id){
        return auctionRepository.findMinimumPriceById(id).orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));
    }

    private Auction findAuction(Long id){
        return auctionRepository.findById(id).orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));
    }
}
