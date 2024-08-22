package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.auction.mapper.AuctionMapper;
import com.example.auctrade.domain.auction.repository.AuctionRepository;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

        int totalCount = (int) auctionRepository.count();
        return  totalCount/size + ((totalCount%size == 0) ? 0 : 1);
    }

    @Override
    public String getStartAt(Long id) {
        return auctionRepository.findStartAtById(id).toString();
    }

    /**
     * 경매 리스트 전체 조회
     * @return 아직 실행 되지 않은 모든 경매 정보
     */
    // 즉 여기서 아직 시작되지 않은 경매를 반환시키는 중
    @Override
    public List<AuctionDTO.GetList> getDepositList(Pageable pageable) {
        return auctionRepository.findByStartedFalse(pageable).stream().map(AuctionMapper::toGetListDto).toList();
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
