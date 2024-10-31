package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.domain.auction.dto.BidDTO;
import com.example.auctrade.domain.auction.dto.DepositDTO;
import com.example.auctrade.domain.auction.mapper.AuctionMapper;
import com.example.auctrade.domain.auction.mapper.BidMapper;
import com.example.auctrade.domain.product.mapper.ProductMapper;
import com.example.auctrade.domain.product.service.FileService;
import com.example.auctrade.domain.product.service.ProductService;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Service
@Slf4j(topic = "AuctionTotalService")
@RequiredArgsConstructor
public class AuctionTotalServiceImpl implements AuctionTotalService{
    private final AuctionService auctionService;
    private final BidService bidService;
    private final DepositService depositService;
    private final ProductService productService;
    private final FileService fileService;
    private static final String SORT_DEFAULT = "start_time";

    /**
     * 경매 등록
     * @param request 등록할 경매 정보
     * @param files 경매 상품 관련 첨부파일
     * @param email 경매를 등록한 회원 이메일
     * @return 생성된 경매 정보
     */
    @Override
    public AuctionDTO.Result createAuction(AuctionDTO.Create request, MultipartFile[] files, String email) throws IOException {

        long productId = productService.create(ProductMapper.toDTO(request, email));

        if(Boolean.FALSE.equals(fileService.uploadFile(files, productId)))
            throw new CustomException(ErrorCode.WRONG_MULTIPARTFILE);

        return AuctionMapper.toResultDto(auctionService.createAuction(request, productId, email) != null);
    }


    /**
     * 경매 조회
     * @param id 조회활 Auction Id
     * @param email 조회환 회원 이메일
     * @return 조회된 경매 정보
     */
    @Override
    public AuctionDTO.Enter getAuctionInfo(Long id, String email) {
        AuctionDTO.Get auction = auctionService.getAuctionById(id);
        return AuctionMapper.toEnterDto(auction, productService.get(auction.getProductId()), fileService.getFiles(id));
    }
    
    /**
     * 경매 입찰
     * @param request 입찰 요청 정보
     * @return 입찰 성공 여부
     */
    @Override
    public BidDTO.Result placeBid(BidDTO.Create request) {
        long auctionId = request.getAuctionId();

        if(bidService.getBidPrice(auctionId) == -1L && request.getPrice() < auctionService.getMinimumPrice(auctionId))
            return BidMapper.toBidResultDto(request,false);

        return bidService.placeBid(request);
    }

    /**
     * 경매 예치금 등록
     * @param request 예치금 요청 정보
     * @param email 예치금 요청한 유저 email
     * @return 예치금 등록 성공 여부
     */
    @Override
    public DepositDTO.Result registerDeposit(AuctionDTO.PutDeposit request, String email) {
        return depositService.registerDeposit(AuctionMapper.toDepositDto(request, email));
    }

    /**
     * 아직 시작하지 않은 경매 리스트를 반환
     * @param page 현재 페이지 인덱스
     * @param size 리스트 사이즈
     * @return 경매 리스트
     */
    @Override
    public List<AuctionDTO.BeforeStart> getNotStartedAuctions(int page, int size) {
        List<AuctionDTO.BeforeStart> result = new ArrayList<>();
        for(AuctionDTO.GetList data : auctionService.getNotStartedAuctions(toPageable(page, size, "createdAt"))){
            result.add(AuctionMapper.toBeforeStartDto(
                    data,
                    depositService.getDepositInfo(data.getId()),
                    productService.get(data.getProductId()).getCategoryName(),
                    fileService.getThumbnail(data.getProductId()).getFilePath()));
        }
        return result;
    }

    /**
     * 현재 경매방의 입찰 정보 조회
     * @param auctionId 조회 대상 경매 id
     * @return 입찰 정보
     */
    @Override
    public BidDTO.Get getBidInfo(Long auctionId) {
        return bidService.getCurrentBid(auctionId);
    }

    /**
     * 예치금을 넣은 경매방 리스트 조회
     * @param page 요청할 page 인덱스
     * @param size page 사이즈
     * @param email 경매에 예치금을 넣은 회원 이메일
     * @return 예치금을 넣은 경매방 리스트
     */
    @Override
    public AuctionDTO.GetPage getMyAuctionsByStatus(int page, int size, String status, String email){
        if(status.equals("open")) return getMyActiveAuctions(page, size, email);
        if(status.equals("closed")) return getMyEndedAuctions(page, size, email);
        return getAllMyAuctions(page, size, email);
    }

    private AuctionDTO.GetPage getMyActiveAuctions(int page, int size, String email){
        AuctionDTO.GetPage auctions = auctionService.getMyActiveAuctions(
                this.toPageable(page, size, SORT_DEFAULT), email);

        List<AuctionDTO.GetList> result = auctions.getAuctions().stream()
                .map(data -> {
                    data.updateProductInfo(productService.get(data.getProductId()).getCategoryName(),
                            fileService.getThumbnail(data.getProductId()).getFilePath());
                    data.updateCurPersonnel(depositService.getNowParticipation(data.getId()));
                    data.updateMinimumPrice(getBidInfo(data.getId()).getPrice());
                    return data;
                }).toList();

        return AuctionMapper.toMyAuctionPage(result, auctions.getMaxPage());
    }

    private AuctionDTO.GetPage getMyEndedAuctions(int page, int size, String email){
        AuctionDTO.GetPage auctions = auctionService.getMyEndedAuctions(
                this.toPageable(page, size, SORT_DEFAULT), email);

        List<AuctionDTO.GetList> result = auctions.getAuctions().stream()
                .map(data -> {
                    data.updateProductInfo(productService.get(data.getProductId()).getCategoryName(),
                            fileService.getThumbnail(data.getProductId()).getFilePath());
                    data.updateCurPersonnel(depositService.getNowParticipation(data.getId()));
                    data.updateMinimumPrice(getBidInfo(data.getId()).getPrice());
                    return data;
                }).toList();

        return AuctionMapper.toMyAuctionPage(result, auctions.getMaxPage());
    }

    private AuctionDTO.GetPage getAllMyAuctions(int page, int size, String email){
        List<AuctionDTO.GetList> auctions = auctionService.getAllMyAuctions(this.toPageable(page, size, SORT_DEFAULT), email);

        List<AuctionDTO.GetList> result = auctions.stream()
                .map(data -> {
                            data.updateProductInfo(productService.get(data.getProductId()).getCategoryName(),
                                    fileService.getThumbnail(data.getProductId()).getFilePath());
                            data.updateCurPersonnel(depositService.getNowParticipation(data.getId()));
                            data.updateMinimumPrice(getBidInfo(data.getId()).getPrice());
                            return data;
                        }).toList();

        return AuctionMapper.toMyAuctionPage(result, auctions.size());
    }

    private Pageable toPageable(int page, int size, String target){
        return PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, target));
    }
}
