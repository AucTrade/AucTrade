package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.auction.mapper.AuctionMapper;
import com.example.auctrade.domain.auction.repository.AuctionRepository;
import com.example.auctrade.domain.product.mapper.ProductMapper;
import com.example.auctrade.domain.product.service.FileService;
import com.example.auctrade.domain.product.service.ProductService;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuctionServiceImpl implements AuctionService {

    private final AuctionRepository auctionRepository;
    private final ProductService productService;
    private final FileService fileService;
    private final DepositService depositService;
    private static final String SORT_DEFAULT = "startTime";


    /**
     * 경매 등록
     * @param request 등록할 경매 정보
     * @param saleUsername 등록한 유저 email
     * @return 생성된 경매 ID
     */
    @Override
    public AuctionDTO.Result createAuction(AuctionDTO.Create request, MultipartFile[] files, String saleUsername) {
        try {
            long productId = productService.create(ProductMapper.toDTO(request, saleUsername));

            if(Boolean.FALSE.equals(fileService.uploadFile(files, productId)))
                throw new CustomException(ErrorCode.WRONG_MULTIPARTFILE);

            auctionRepository.save(AuctionMapper.toEntity(request, productId, saleUsername));
            return AuctionMapper.toResultDto(true);

        }catch (Exception e){
            log.error(e.getMessage());
            return AuctionMapper.toResultDto(false);
        }
    }

    /**
     * 경매 조회
     * @param id 조회할 경매 ID
     * @return 조회한 경매 정보
     */
    @Override
    public AuctionDTO.Enter getAuctionById(long id) {
        Auction auction = findAuction(id);
        return AuctionMapper.toEnterDto(auction, productService.get(auction.getProductId()), fileService.getFiles(id));
    }


    /**
     * 아직 시작하지 않은 경매 리스트를 반환
     * @param page 현재 페이지 인덱스
     * @param size 리스트 사이즈
     * @return 경매 리스트
     */
    @Override
    public List<AuctionDTO.BeforeStart> getNotStartedAuctions(int page, int size) {
        Page<Auction> auctions = auctionRepository.findNotStartedAuctions(LocalDateTime.now(), toPageable(page, size, "createdAt"));
        List<AuctionDTO.BeforeStart> result = new ArrayList<>();
        for(Auction auction : auctions.getContent()){
            result.add(AuctionMapper.toBeforeStartDto(
                    auction,
                    depositService.getDepositInfo(auction.getId()),
                    productService.get(auction.getProductId()).getCategoryName(),
                    fileService.getThumbnail(auction.getProductId()).getFilePath()));
        }
        return result;
    }

    /**
     * 시작하기 전 내가 작성한 리스트 조회
     * @param page 현재 페이지 인덱스
     * @param size 리스트 사이즈
     * @param email 조회할 경매 판매자 이메일
     * @return 종료된 해당 경매 정보
     */
    @Override
    public AuctionDTO.GetPage getMyNotStartedAuctions(int page, int size, String email) {
        Page<Auction> auctions = auctionRepository.findNotStartedAuctionsBySeller(LocalDateTime.now(), email, toPageable(page, size, "createdAt"));

        List<AuctionDTO.GetList> result = new ArrayList<>();
        for(Auction auction : auctions.getContent()){
            result.add(AuctionMapper.toGetListDto(auction,
                    depositService.getNowParticipation(auction.getId()),
                    productService.get(auction.getProductId()).getCategoryName(),
                    fileService.getThumbnail(auction.getProductId()).getFilePath()
            ));
        }
        return AuctionMapper.toGetPageDto(result, auctions.getTotalPages());
    }

    /**
     * 경매 리스트 조회
     * @param page 현재 페이지 인덱스
     * @param size 리스트 사이즈
     * @param email 조회할 경매 판매자 이메일
     * @return 진행 중인 해당 경매 정보
     */
    @Override
    public AuctionDTO.GetPage getAllMyAuctions(int page, int size, String email) {
        Page<Auction> auctions = auctionRepository.findBySellerEmail(email, toPageable(page, size, SORT_DEFAULT));
        List<AuctionDTO.GetList> result = new ArrayList<>();

        for(Auction auction : auctions.getContent()){
            result.add(AuctionMapper.toGetListDto(auction,
                    depositService.getNowParticipation(auction.getId()),
                    productService.get(auction.getProductId()).getCategoryName(),
                    fileService.getThumbnail(auction.getProductId()).getFilePath()
            ));
        }
        return AuctionMapper.toGetPageDto(result, auctions.getTotalPages());
    }

    /**
     * 진행 중인 경매 리스트 조회
     * @param page 현재 페이지 인덱스
     * @param size 리스트 사이즈
     * @param email 조회할 경매 판매자 이메일
     * @return 진행 중인 해당 경매 정보
     */
    @Override
    public AuctionDTO.GetPage getMyActiveAuctions(int page, int size, String email) {
        Page<Auction> auctions = auctionRepository.findActivateAuctionsBySeller(LocalDateTime.now(), email, toPageable(page, size, SORT_DEFAULT));
        List<AuctionDTO.GetList> result = new ArrayList<>();
        for(Auction auction : auctions.getContent()){
            result.add(AuctionMapper.toGetListDto(auction,
                    depositService.getNowParticipation(auction.getId()),
                    productService.get(auction.getProductId()).getCategoryName(),
                    fileService.getThumbnail(auction.getProductId()).getFilePath()
                    ));
        }
        return AuctionMapper.toGetPageDto(result, auctions.getTotalPages());
    }

    /**
     * 종료된 경매 리스트 조회
     * @param page 현재 페이지 인덱스
     * @param size 리스트 사이즈
     * @param email 조회할 경매 판매자 이메일
     * @return 종료된 해당 경매 정보
     */
    @Override
    public AuctionDTO.GetPage getMyEndedAuctions(int page, int size, String email) {
        Page<Auction> auctions = auctionRepository.findEndAuctionsBySeller(LocalDateTime.now(), email, toPageable(page, size, SORT_DEFAULT));
        List<AuctionDTO.GetList> result = new ArrayList<>();
        for(Auction auction : auctions.getContent()){
            result.add(AuctionMapper.toGetListDto(auction,
                    depositService.getNowParticipation(auction.getId()),
                    productService.get(auction.getProductId()).getCategoryName(),
                    fileService.getThumbnail(auction.getProductId()).getFilePath()
            ));
        }
        return AuctionMapper.toGetPageDto(result, auctions.getTotalPages());
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

    private Pageable toPageable(int page, int size, String target){
        return PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, target));
    }
}
