package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.domain.auction.dto.BidDTO;
import com.example.auctrade.domain.auction.dto.DepositDTO;
import com.example.auctrade.domain.auction.mapper.AuctionMapper;
import com.example.auctrade.domain.auction.mapper.BidMapper;
import com.example.auctrade.domain.product.dto.ProductDTO;
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

    /**
     * 경매 등록
     * @param request 등록할 경매 정보
     * @param files 경매 상품 관련 첨부파일
     * @param email 경매를 등록한 회원 이메일
     * @return 생성된 경매 정보
     */
    @Override
    public AuctionDTO.Result createAuction(AuctionDTO.Create request, MultipartFile[] files, String email) throws IOException {
        long productId = productService.create(ProductDTO.Create.builder()
                .saleUsername(email)
                .productCategoryId(request.getProductCategoryId())
                .name(request.getProductName())
                .detail(request.getProductDetail())
                .build());

        if(Boolean.FALSE.equals(fileService.uploadFile(files, productId)))
            throw new CustomException(ErrorCode.WRONG_MULTIPARTFILE);

        return AuctionMapper.toResultDto(auctionService.createAuction(request, productId, email) != null);
    }
    /**
     * 예치금을 넣은 경매방 리스트 조회
     * @param page 요청할 page 인덱스
     * @param size page 사이즈
     * @param email 경매에 예치금을 넣은 회원 이메일
     * @return 예치금을 넣은 경매방 리스트
     */
    @Override
    public AuctionDTO.AfterStartList getMyAuctionPage(int page, int size, String email){
        List<AuctionDTO.GetList> auctions = auctionService.getMyAuctions(
                depositService.getMyAuctions(toPageable(page,size,"startDate"), email));

        List<AuctionDTO.My> result = auctions.stream()
                .map(data -> AuctionMapper.toMyDto(
                        data,
                        productService.get(data.getProductId()).getCategoryName(),
                        fileService.getThumbnail(data.getProductId()).getFilePath(),
                        depositService.getCurrentPersonnel(data.getId()),
                        getBidInfo(data.getId()).getPrice())).toList();

        return AuctionMapper.toMyAuctionPage(result, depositService.getMyDepositSize(email));
    }

    @Override
    public AuctionDTO.OpeningAuctionsList getMyOpeningAuctionPage(int page, int size, String email) {
        List<AuctionDTO.GetList> auctions = auctionService.getMyOpeningAuctions(
                this.toPageable(page, size, "startDate"), email
        );

        log.info("개설 경매 리스트: {}", auctions.stream().map(AuctionDTO.GetList::getTitle).toList());

        List<AuctionDTO.My> result = auctions.stream()
                .map(data -> AuctionMapper.toMyDto(
                        data,
                        productService.get(data.getProductId()).getCategoryName(),
                        fileService.getThumbnail(data.getProductId()).getFilePath(),
                        depositService.getCurrentPersonnel(data.getId()),
                        getBidInfo(data.getId()).getPrice())).toList();

        return AuctionMapper.toOpeningAuctionPage(result, result.size());
    }

    @Override
    public AuctionDTO.Enter enterAuction(Long id, String email) {
        AuctionDTO.Get auction = auctionService.findById(id);
        return AuctionMapper.toEnterDto(auction, productService.get(auction.getProductId()), fileService.getFiles(id));
    }

    @Override
    public BidDTO.Result bidPrice(BidDTO.Create request) {
        long auctionId = request.getAuctionId();

        if(bidService.getBidPrice(auctionId) == -1L && request.getPrice() < auctionService.getMinimumPrice(auctionId))
            return BidMapper.toBidResultDto(request,false);

        return bidService.updateBidPrice(request);
    }

    @Override
    public DepositDTO.Result depositPrice(DepositDTO.Create requestDto, String email) {
        long auctionId = requestDto.getAuctionId();
        return depositService.depositPrice(
                requestDto, email, auctionService.getMaxPersonnel(auctionId), auctionService.getStartAt(auctionId));
    }
    /**
     * 아직 시작하지 않은 경매 리스트를 반환
     * @param page 현재 페이지 인덱스
     * @param size 리스트 사이즈
     * @return 경매 리스트
     */
    @Override
    public List<AuctionDTO.BeforeStart> getBeforeStartPage(int page, int size) {
        List<AuctionDTO.BeforeStart> result = new ArrayList<>();
        for(AuctionDTO.GetList data : auctionService.getDepositList(toPageable(page, size, "createdAt"))){
            result.add(AuctionMapper.toBeforeStartDto(
                    data,
                    depositService.getDeposit(data.getId(), data.getMaxPersonnel()),
                    productService.get(data.getProductId()).getCategoryName(),
                    fileService.getThumbnail(data.getProductId()).getFilePath()));
        }
        return result;
    }

    @Override
    public List<Long> findAllActiveAuctionIds() {
        return auctionService.findAllActiveAuctionIds();
    }
    /**
     * 현재 경매방의 최대 입찰금
     * @param auctionId 조회 대상 경매 id
     * @return 최대 입찰금
     */

    @Override
    public BidDTO.Get getBidInfo(Long auctionId) {
        BidDTO.Get load = bidService.getBid(auctionId);
        if(load.getPrice() == -1L) load.updatePrice(auctionService.getMinimumPrice(auctionId));
        return load;
    }

    @Override
    public void processBids(long id) {
        bidService.processBids(id);
    }

    @Override
    public void startAuction(Long id) {
        auctionService.startAuction(id);
    }

    @Override
    public void endAuction(Long id) {
        auctionService.endAuction(id);
    }

    private Pageable toPageable(int page, int size, String target){
        return PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, target));
    }
}
