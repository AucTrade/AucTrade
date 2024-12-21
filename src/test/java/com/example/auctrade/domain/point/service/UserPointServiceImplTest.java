package com.example.auctrade.domain.point.service;

import com.example.auctrade.domain.point.dto.PointDto;
import com.example.auctrade.domain.point.entity.Point;
import com.example.auctrade.domain.point.repository.PointRepository;
import com.example.auctrade.domain.user.entity.User;
import com.example.auctrade.domain.user.entity.UserRoleEnum;
import com.example.auctrade.domain.user.repository.UserRepository;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY,
        connection = EmbeddedDatabaseConnection.H2)
@Transactional
class UserPointServiceImplTest {
    @Autowired
    UserPointService userPointService;
    @Autowired
    PointRepository pointRepository;
    @Autowired
    UserRepository userRepository;

    @AfterEach
    void deleteEachData(){
        userRepository.deleteAllInBatch();
        pointRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("잘못된 이메일(JWT 토큰 값)로 포인트 충전 요청시 USER_NOT_FOUND 메시지 발생")
    void chargePointInvalidEmail() {
        //Given
        User user = userRepository.save(generateUser());
        final int AMOUNT = 4000;

        //When
        CustomException exception = assertThrows(CustomException.class,
                ()-> userPointService.chargePoint(generatePointDto(AMOUNT), ""));

        //Then
        assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), exception.getMessage());
        assertEquals(0 , user.getPoint(), "충전 실패한 포인트 량이 반영 되었습니다.");
    }

    @Test
    @DisplayName("포인트 정상 충전 요청시 저장된 Point ID 값 반환")
    void chargePoint() {
        //Given
        User user = userRepository.save(generateUser());
        final int AMOUNT = 4000;

        //When
        PointDto.Result result = userPointService.chargePoint(generatePointDto(AMOUNT), user.getEmail());

        //Then
        Assertions.assertAll(
                () -> assertNotNull( result.getPointId()),
                () -> assertTrue(result.getSuccess())
        );

        assertEquals(AMOUNT, user.getPoint(), "충전된 포인트량이 유저 Point에 반영되지 않았습니다.");
    }

    @Test
    @DisplayName("잘못된 이메일(JWT 토큰 값)로 포인트 환전 요청시 USER_NOT_FOUND 메시지 발생")
    void exchangePointInvalidEmail() {
        //Given
        User user = userRepository.save(generateUser());
        final int AMOUNT = 4000;

        //When
        CustomException exception = assertThrows(CustomException.class,
                ()-> userPointService.exchangePoint(generatePointDto(AMOUNT), ""));

        //Then
        assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), exception.getMessage());
        assertEquals(0 , user.getPoint(), "충전 실패한 포인트 량이 반영 되었습니다.");
    }


    @Test
    @DisplayName("유저 포인트가 환전할 포인트보다 적을 시 EXCEEDED_POINT_REQUEST 메시지 발생")
    void exchangePointInvalidAmount() {
        //Given
        User user = userRepository.save(generateUser());
        final int AMOUNT = 4000;

        //When
        CustomException exception = assertThrows(CustomException.class,
                ()-> userPointService.exchangePoint(generatePointDto(AMOUNT), user.getEmail()));

        //Then
        assertEquals(ErrorCode.EXCEEDED_POINT_REQUEST.getMessage(), exception.getMessage());
        assertEquals(0 , user.getPoint(), "충전 실패한 포인트 량이 반영 되었습니다.");
    }

    @Test
    @DisplayName("포인트 정상 환전 요청시 저장된 Point ID 값 반환")
    void exchangePoint() {
        //Given
        User user = userRepository.save(generateUser());
        final int AMOUNT = 4000;
        user.addPoint(AMOUNT);
        user = userRepository.save(user);

        //When
        PointDto.Result result = userPointService.exchangePoint(generatePointDto(AMOUNT), user.getEmail());

        //Then
        Assertions.assertAll(
                () -> assertNotNull(result.getPointId()),
                () -> assertTrue(result.getSuccess())
        );
        assertEquals(0, user.getPoint(), "환전된 포인트량이 유저 Point에 반영되지 않았습니다.");
    }

    @Test
    @DisplayName("잘못된 이메일(JWT 토큰 값)로 포인트 충전 취소 요청시 USER_NOT_FOUND 메시지 발생")
    void cancelChargePointInvalidEmail() {
        //Given
        User user = userRepository.save(generateUser());
        final int AMOUNT = 4000;

        userPointService.chargePoint(generatePointDto(AMOUNT), user.getEmail());
        List<Point> points = pointRepository.findAllByUserId(user.getId());

        //When
        CustomException exception = assertThrows(CustomException.class,
                ()-> userPointService.cancelPoint(points.get(0).getId(), ""));

        //Then
        assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), exception.getMessage());
        assertEquals(AMOUNT, user.getPoint(), "취소 실패한 포인트 량이 반영 되었습니다.");
    }

    @Test
    @DisplayName("정상 포인트 충전 취소 요청시 해당 pointId 및 true 반환")
    void cancelChargePoint() {
        //Given
        User user = userRepository.save(generateUser());
        final int AMOUNT = 4000;
        Long pointId = userPointService.chargePoint(generatePointDto(AMOUNT), user.getEmail()).getPointId();
        List<Point> points = pointRepository.findAllByUserId(user.getId());

        //When
        PointDto.Result result = userPointService.cancelPoint(points.get(0).getId(), user.getEmail());

        //Then
        assertEquals(pointId, result.getPointId(), "취소된 pointId 가 일치하지 않습니다.");
        assertTrue(result.getSuccess());
        assertEquals(0 , user.getPoint(), "취소한 포인트 량이 반영 되지 않았습니다.");
    }

    @Test
    @DisplayName("정상 포인트 환전 취소 요청시 해당 pointId 및 true 반환")
    void cancelExchangePoint() {
        //Given
        User user = userRepository.save(generateUser());
        final int AMOUNT = 4000;
        userPointService.chargePoint(generatePointDto(AMOUNT), user.getEmail());
        Long pointId = userPointService.exchangePoint(generatePointDto(AMOUNT), user.getEmail()).getPointId();
        List<Point> points = pointRepository.findAllByUserId(user.getId());

        //When
        PointDto.Result result = userPointService.cancelPoint(points.get(1).getId(), user.getEmail());

        //Then
        assertEquals(pointId, result.getPointId(), "취소된 pointId 가 일치하지 않습니다.");
        assertTrue(result.getSuccess());
        assertEquals(AMOUNT , user.getPoint(), "취소한 포인트 량이 반영 되지 않았습니다.");
    }

    private PointDto.Create generatePointDto(Integer amount) {
        return new PointDto.Create(amount, "111-1234-11211");
    }

    private User generateUser() {
        return User.builder()
                .email(UUID.randomUUID()+"@test.com")
                .password("123")
                .phone("010-0000-0000")
                .postcode("12341")
                .birth(LocalDate.now())
                .address("address")
                .role(UserRoleEnum.USER)
                .build();
    }
}