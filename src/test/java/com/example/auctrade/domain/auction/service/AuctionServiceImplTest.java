package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.domain.product.dto.ProductCategoryDTO;
import com.example.auctrade.domain.product.service.ProductCategoryService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
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
    ProductCategoryService productCategoryService;
    List<MultipartFile[]> mockFiles = new ArrayList<>();

    @BeforeAll
    void createData(){
        productCategoryService.create(new ProductCategoryDTO("잡화"));

        MockMultipartFile mockFile = new MockMultipartFile(
                "file", // 파라미터 이름
                "testfile.png", // 파일 이름
                "image/png", // 파일 타입
                "Hello World".getBytes() // 파일 내용
        );
        
        mockFiles.add(new MultipartFile[]{mockFile});
    }

    @AfterAll
    void deleteData(){
    }

    @Test
    @DisplayName("경매 id 조회 기능 TEST")
    void getAuctionById() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.plusDays(2);
        LocalDateTime endTime = startTime.plusDays(1);

        AuctionDTO.Create request = new AuctionDTO.Create("제목1",
                "내용", startTime, endTime,50 ,1000, "제품1" ,"상세",1L);

        auctionService.createAuction(request, mockFiles.get(0),"test@test.com");

        AuctionDTO.GetPage data = auctionService.getAllMyAuctions(1, 10, "test@test.com");
        Assertions.assertNotEquals(0, data.getAuctions().size());
        
        AuctionDTO.Enter result = auctionService.getAuctionById(data.getAuctions().get(0).getId());
        Assertions.assertEquals("제목1", result.getTitle());
        Assertions.assertEquals("제품1", result.getProductName());
        Assertions.assertEquals("잡화", result.getProductCategory());
    }

    @Test
    @DisplayName("시작전 경매 확인")
    void getNotStartedAuctions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.plusDays(2);
        LocalDateTime endTime = startTime.plusDays(1);

        AuctionDTO.Create request = new AuctionDTO.Create("제목2",
                "내용", startTime, endTime,50 ,1000, "제품2" ,"상세",1L);

        auctionService.createAuction(request, mockFiles.get(0),"test2@test.com");

        request = new AuctionDTO.Create("제목3",
                "내용", startTime, endTime,50 ,1000, "제품3" ,"상세",1L);
        auctionService.createAuction(request, mockFiles.get(0),"test2@test.com");

        List<AuctionDTO.BeforeStart> result = auctionService.getNotStartedAuctions(1,10);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("제목3", result.get(0).getTitle());
        Assertions.assertEquals("제목2", result.get(1).getTitle());
    }
    @Test
    @DisplayName("내가 생성한 모든 경매 확인")
    void getAllMyAuctions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.plusDays(2);
        LocalDateTime endTime = startTime.plusDays(1);

        AuctionDTO.Create request = new AuctionDTO.Create("시작하기전 경매",
                "내용", startTime, endTime,50 ,1000, "제품1" ,"상세",1L);

        auctionService.createAuction(request, mockFiles.get(0),"test3@test.com");

        startTime = now.minusDays(1);
        endTime = now.plusDays(1);

        request = new AuctionDTO.Create("진행중 경매",
                "내용", startTime, endTime,50 ,1000, "제품2" ,"상세",1L);
        auctionService.createAuction(request, mockFiles.get(0),"test3@test.com");

        startTime = now.minusDays(2);
        endTime = now.minusDays(1);

        request = new AuctionDTO.Create("끝난 경매",
                "내용", startTime, endTime,50 ,1000, "제품3" ,"상세",1L);
        auctionService.createAuction(request, mockFiles.get(0),"test3@test.com");

        AuctionDTO.GetPage result = auctionService.getAllMyAuctions(1,10,"test3@test.com");
        Assertions.assertEquals(3, result.getAuctions().size());
        Assertions.assertEquals("시작하기전 경매", result.getAuctions().get(0).getTitle());
        Assertions.assertEquals("진행중 경매", result.getAuctions().get(1).getTitle());
        Assertions.assertEquals("끝난 경매", result.getAuctions().get(2).getTitle());
    }

    @Test
    @DisplayName("내가 생성한 경매 중 시작하지 않은 리스트 확인")
    void getMyNotStartedAuctions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.plusDays(2);
        LocalDateTime endTime = startTime.plusDays(1);

        AuctionDTO.Create request = new AuctionDTO.Create("시작하기전 경매",
                "내용", startTime, endTime,50 ,1000, "제품1" ,"상세",1L);

        auctionService.createAuction(request, mockFiles.get(0),"test4@test.com");

        request = new AuctionDTO.Create("시작하기전 경매2",
                "내용", startTime, endTime,50 ,1000, "제품2" ,"상세",1L);

        auctionService.createAuction(request, mockFiles.get(0),"test5@test.com");

        startTime = now.minusDays(1);
        endTime = now.plusDays(1);

        request = new AuctionDTO.Create("진행중 경매",
                "내용", startTime, endTime,50 ,1000, "제품3" ,"상세",1L);
        auctionService.createAuction(request, mockFiles.get(0),"test4@test.com");

        startTime = now.minusDays(2);
        endTime = now.minusDays(1);

        request = new AuctionDTO.Create("끝난 경매",
                "내용", startTime, endTime,50 ,1000, "제품4" ,"상세",1L);
        auctionService.createAuction(request, mockFiles.get(0),"test4@test.com");


        AuctionDTO.GetPage result = auctionService.getMyNotStartedAuctions(1,10, "test4@test.com");
        Assertions.assertEquals(1, result.getAuctions().size());
        Assertions.assertEquals("시작하기전 경매", result.getAuctions().get(0).getTitle());
    }

    @Test
    @DisplayName("내가 생성한 경매 중 진행중인 리스트 확인")
    void getMyActiveAuctions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.plusDays(2);
        LocalDateTime endTime = startTime.plusDays(1);

        AuctionDTO.Create request = new AuctionDTO.Create("시작하기전 경매",
                "내용", startTime, endTime,50 ,1000, "제품1" ,"상세",1L);

        auctionService.createAuction(request, mockFiles.get(0),"test6@test.com");

        startTime = now.minusDays(1);
        endTime = now.plusDays(1);

        request = new AuctionDTO.Create("진행중 경매",
                "내용", startTime, endTime,50 ,1000, "제품2" ,"상세",1L);
        auctionService.createAuction(request, mockFiles.get(0),"test6@test.com");

        startTime = now.minusDays(1);
        endTime = now.plusDays(1);

        request = new AuctionDTO.Create("진행중 경매2",
                "내용", startTime, endTime,50 ,1000, "제품3" ,"상세",1L);
        auctionService.createAuction(request, mockFiles.get(0),"test7@test.com");


        startTime = now.minusDays(2);
        endTime = now.minusDays(1);

        request = new AuctionDTO.Create("끝난 경매",
                "내용", startTime, endTime,50 ,1000, "제품4" ,"상세",1L);
        auctionService.createAuction(request, mockFiles.get(0),"test6@test.com");

        AuctionDTO.GetPage result = auctionService.getMyActiveAuctions(1,10,"test6@test.com");
        Assertions.assertEquals(1, result.getAuctions().size());
        Assertions.assertEquals("진행중 경매", result.getAuctions().get(0).getTitle());
    }

    @Test
    @DisplayName("내가 생성한 경매 중 종료된 리스트 확인")
    void getMyEndedAuctions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.plusDays(2);
        LocalDateTime endTime = startTime.plusDays(1);

        AuctionDTO.Create request = new AuctionDTO.Create("시작하기전 경매",
                "내용", startTime, endTime,50 ,1000, "제품1" ,"상세",1L);

        auctionService.createAuction(request, mockFiles.get(0),"test8@test.com");

        startTime = now.minusDays(1);
        endTime = now.plusDays(1);

        request = new AuctionDTO.Create("진행중 경매",
                "내용", startTime, endTime,50 ,1000, "제품2" ,"상세",1L);
        auctionService.createAuction(request, mockFiles.get(0),"test8@test.com");

        startTime = now.minusDays(2);
        endTime = now.minusDays(1);

        request = new AuctionDTO.Create("끝난 경매",
                "내용", startTime, endTime,50 ,1000, "제품3" ,"상세",1L);
        auctionService.createAuction(request, mockFiles.get(0),"test9@test.com");

        request = new AuctionDTO.Create("끝난 경매2",
                "내용", startTime, endTime,50 ,1000, "제품4" ,"상세",1L);
        auctionService.createAuction(request, mockFiles.get(0),"test8@test.com");

        AuctionDTO.GetPage result = auctionService.getMyEndedAuctions(1,10, "test8@test.com");
        Assertions.assertEquals(1, result.getAuctions().size());
        Assertions.assertEquals("끝난 경매2", result.getAuctions().get(0).getTitle());

    }

    @Test
    @DisplayName("경매 최대 인원 조회")
    void getMaxParticipation() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.plusDays(2);
        LocalDateTime endTime = startTime.plusDays(1);

        AuctionDTO.Create request = new AuctionDTO.Create("시작하기전 경매",
                "내용", startTime, endTime,30 ,1000, "제품1" ,"상세",1L);


        auctionService.createAuction(request, mockFiles.get(0),"test10@test.com");

        startTime = now.minusDays(1);
        endTime = now.plusDays(1);

        request = new AuctionDTO.Create("진행중 경매",
                "내용", startTime, endTime,20 ,1000, "제품2" ,"상세",1L);
        auctionService.createAuction(request, mockFiles.get(0),"test11@test.com");

        AuctionDTO.GetPage data1 = auctionService.getAllMyAuctions(1, 10, "test10@test.com");
        Assertions.assertEquals(30, auctionService.getMaxParticipation(data1.getAuctions().get(0).getId()));

        AuctionDTO.GetPage data2 = auctionService.getAllMyAuctions(1, 10, "test11@test.com");
        Assertions.assertEquals(20, auctionService.getMaxParticipation(data2.getAuctions().get(0).getId()));
    }

    @Test
    @DisplayName("경매 최소 입찰금 조회")
    void getMinimumPrice() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.plusDays(2);
        LocalDateTime endTime = startTime.plusDays(1);

        AuctionDTO.Create request = new AuctionDTO.Create("시작하기전 경매",
                "내용", startTime, endTime,30 ,3000, "제품1" ,"상세",1L);


        auctionService.createAuction(request, mockFiles.get(0),"test12@test.com");

        startTime = now.minusDays(1);
        endTime = now.plusDays(1);

        request = new AuctionDTO.Create("진행중 경매",
                "내용", startTime, endTime,20 ,2000, "제품2" ,"상세",1L);
        auctionService.createAuction(request, mockFiles.get(0),"test13@test.com");

        AuctionDTO.GetPage data1 = auctionService.getAllMyAuctions(1, 10, "test12@test.com");
        Assertions.assertEquals(3000, auctionService.getMinimumPrice(data1.getAuctions().get(0).getId()));

        AuctionDTO.GetPage data2 = auctionService.getAllMyAuctions(1, 10, "test13@test.com");
        Assertions.assertEquals(2000, auctionService.getMinimumPrice(data2.getAuctions().get(0).getId()));
    }

    @Test
    @DisplayName("경매 시작 시간 조회")
    void getStartAt() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime1 = now.plusDays(2);
        LocalDateTime endTime1 = startTime1.plusDays(1);

        AuctionDTO.Create request = new AuctionDTO.Create("시작하기전 경매",
                "내용", startTime1, endTime1,30 ,3000, "제품1" ,"상세",1L);


        auctionService.createAuction(request, mockFiles.get(0),"test14@test.com");

        LocalDateTime startTime2 = now.minusDays(1);
        LocalDateTime endTime2 = now.plusDays(1);

        request = new AuctionDTO.Create("진행중 경매",
                "내용", startTime2, endTime2,20 ,2000, "제품2" ,"상세",1L);
        auctionService.createAuction(request, mockFiles.get(0),"test15@test.com");


        String time1 = startTime1.format(formatter);
        AuctionDTO.GetPage data1 = auctionService.getAllMyAuctions(1, 10, "test14@test.com");
        Assertions.assertEquals(time1, auctionService.getStartAt(data1.getAuctions().get(0).getId()).substring(0, 23));

        String time2 = endTime1.format(formatter);
        Assertions.assertNotEquals(time2, auctionService.getStartAt(data1.getAuctions().get(0).getId()).substring(0, 23));
    }
}