package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.auction.repository.AuctionRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY,
        connection = EmbeddedDatabaseConnection.H2)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class AuctionServiceImplTest {
    @Autowired
    AuctionService auctionService;

    @Autowired
    AuctionRepository auctionRepository;

    List<Auction> dataList = new ArrayList<>();


    @BeforeAll
    void createData(){

        for(int i = 0 ; i < 10; i++){
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime;
            LocalDateTime endTime;

            if (i < 4) {
                // 0, 1, 2, 3: 시작하지 않은 경매
                startTime = now.plusDays(2);
                endTime = startTime.plusDays(1);
            } else if (i < 9) {
                // 4, 5, 6, 7, 8: 진행 중인 경매
                startTime = now.minusDays(1);
                endTime = now.plusDays(1);
            } else {
                // 9: 이미 끝난 경매
                startTime = now.minusDays(2);
                endTime = now.minusDays(1);
            }

            dataList.add(auctionRepository.save(Auction.builder()
                    .title("제목" + i)
                    .description("내용" + i)
                    .startTime(startTime)
                    .endTime(endTime)
                    .maxParticipants(50 + i)
                    .minimumPrice(1000 + i)
                    .productId(i)
                    .sellerEmail("test" + (i % 3) + "@test.com")
                    .build()));
        }
    }

    @AfterAll
    void deleteData(){
    }

    @Test
    @DisplayName("경매 page 조회 기능 TEST")
    void getAuctions() {
        List<AuctionDTO.GetList> result = auctionService.getAuctions(toPageable(1,4,"createdAt"));

        Assertions.assertNotNull(result);
        Assertions.assertEquals("제목9",result.get(0).getTitle());

        List<AuctionDTO.GetList> result2 = auctionService.getAuctions(toPageable(2,4,"createdAt"));
        Assertions.assertNotNull(result2);
        Assertions.assertEquals("제목5",result2.get(0).getTitle());

    }

    @Test
    @DisplayName("경매 id 조회 기능 TEST")
    void getAuctionById() {
        Auction target = dataList.get(0);
        AuctionDTO.Get result = auctionService.getAuctionById(target.getId());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(target.getId(), result.getId());
    }

    @Test
    @DisplayName("시작전 경매 확인")
    void getNotStartedAuctions() {

        List<AuctionDTO.GetList> result = auctionService.getNotStartedAuctions(toPageable(1,10,"createdAt"));

        Assertions.assertEquals(4, result.size());
    }
    @Test
    @DisplayName("내가 생성한 모든 경매 확인")
    void getAllMyAuctions() {
        List<AuctionDTO.GetList> result = auctionService.getAllMyAuctions(toPageable(1,10,"createdAt"), "test1@test.com");

        Assertions.assertEquals(3, result.size());
    }

    @Test
    @DisplayName("내가 생성한 경매 중 시작하지 않은 리스트 확인")
    void getMyNotStartedAuctions() {
        AuctionDTO.GetPage result = auctionService.getMyNotStartedAuctions(toPageable(1,10,"createdAt"), "test0@test.com");

        Assertions.assertEquals(2, result.getAuctions().size());
    }

    @Test
    @DisplayName("내가 생성한 경매 중 진행중인 리스트 확인")
    void getMyActiveAuctions() {
        AuctionDTO.GetPage result = auctionService.getMyActiveAuctions(toPageable(1,10,"createdAt"), "test0@test.com");

        Assertions.assertEquals(1, result.getAuctions().size());
    }

    @Test
    @DisplayName("내가 생성한 경매 중 종료된 리스트 확인")
    void getMyEndedAuctions() {
        AuctionDTO.GetPage result = auctionService.getMyEndedAuctions(toPageable(1,10,"createdAt"), "test0@test.com");

        Assertions.assertEquals(1, result.getAuctions().size());
    }

    @Test
    @DisplayName("경매 최대 인원 조회")
    void getMaxParticipation() {
        Assertions.assertEquals(53, auctionService.getMaxParticipation(dataList.get(3).getId()));
        Assertions.assertEquals(55, auctionService.getMaxParticipation(dataList.get(5).getId()));
    }

    @Test
    @DisplayName("경매 최소 입찰금 조회")
    void getMinimumPrice() {
        Assertions.assertEquals(1003, auctionService.getMinimumPrice(dataList.get(3).getId()));
        Assertions.assertEquals(1005, auctionService.getMinimumPrice(dataList.get(5).getId()));
    }

    @Test
    @DisplayName("경매 시작 시간 조회")
    void getStartAt() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

        String time1 = dataList.get(3).getStartTime().format(formatter);
        Assertions.assertEquals(time1, auctionService.getStartAt(dataList.get(3).getId()).substring(0, 23));

        String time2 = dataList.get(5).getEndTime().format(formatter);
        Assertions.assertNotEquals(time2, auctionService.getStartAt(dataList.get(5).getId()).substring(0, 23));
    }


    private Pageable toPageable(int page, int size, String target){
        return PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, target));
    }
}