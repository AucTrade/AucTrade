package com.example.auctrade.domain.point.service;

import com.example.auctrade.domain.point.entity.Point;
import com.example.auctrade.domain.point.entity.PointStatus;
import com.example.auctrade.domain.point.entity.PointType;
import com.example.auctrade.domain.point.repository.PointRepository;
import com.example.auctrade.domain.point.vo.PointInfoVo;
import com.example.auctrade.domain.point.vo.PointVo;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY,
        connection = EmbeddedDatabaseConnection.H2)
@Transactional
class PointLogServiceImplTest {
    @Autowired
    PointLogService pointLogService;
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
    @DisplayName("포인트 정상 충전 요청시 저장된 amount 값 반환")
    void chargePoint() {
        //Given
        User user = userRepository.save(generateUser());
        final int AMOUNT = 4000;
        PointVo pointVo = generatePointVo(user.getId(), AMOUNT, user.getPoint());

        //When
        Long result = pointLogService.chargePoint(pointVo);

        //Then
        Point point = pointRepository.findById(result).orElse(null);
        assertNotNull(point);
        Assertions.assertAll(
                () -> assertEquals(PointType.CHARGE, point.getType(), "조회된 type 이 일치하지 않습니다."),
                () -> assertEquals(PointStatus.CREATED, point.getStatus(), "조회된 status 가 일치하지 않습니다.")
        );
    }

    @Test
    @DisplayName("포인트 정상 환불 요청시 저장된 amount 값 반환")
    void exchangePoint() {
        //Given
        User user = generateUser();
        final int AMOUNT = 4000;
        user.addPoint(AMOUNT);
        user = userRepository.save(user);
        PointVo pointVo = generatePointVo(user.getId(), AMOUNT, user.getPoint());

        //When
        Long result = pointLogService.exchangePoint(pointVo);

        //Then
        Point point = pointRepository.findById(result).orElse(null);
        assertNotNull(point);
        Assertions.assertAll(
                () -> assertEquals(PointType.EXCHANGE, point.getType(), "조회된 type 이 일치하지 않습니다."),
                () -> assertEquals(PointStatus.CREATED, point.getStatus(), "조회된 status 가 일치하지 않습니다.")
        );
    }

    @Test
    @DisplayName("잘못된 유저 id 입력 포인트 충전 취소 요청시 POINT_USER_NOT_EQUAL 메시지 발생")
    void cancelPointInvalidUserId() {
        //Given
        User user = userRepository.save(generateUser());
        final int AMOUNT = 5000;

        PointVo pointVo = generatePointVo(user.getId(), AMOUNT, user.getPoint());
        pointLogService.chargePoint(pointVo);
        Point point = pointRepository.findAllByUserId(user.getId()).get(0);

        //When
        CustomException exception = assertThrows(CustomException.class,
                ()-> pointLogService.cancelPoint(point.getId(), -1L));

        //Then
        assertEquals(ErrorCode.POINT_USER_NOT_EQUAL.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("정상 포인트 충전 내역 취소 요청시 해당 포인트 량 반환")
    void cancelChargePoint() {
        //Given
        User user = userRepository.save(generateUser());
        final int AMOUNT = 5000;

        PointVo pointVo = generatePointVo(user.getId(), AMOUNT, user.getPoint());
        Long pointId = pointLogService.chargePoint(pointVo);

        //When
        Integer result = pointLogService.cancelPoint(pointId, user.getId());

        //Then
        Point point = pointRepository.findAllByUserId(user.getId()).get(0);
        Assertions.assertAll(
                () -> assertEquals(AMOUNT, result, "취소된 포인트 량이 일치하지 않습니다."),
                () -> assertEquals(PointType.CHARGE, point.getType(), "조회된 type 이 일치하지 않습니다."),
                () -> assertEquals(PointStatus.CANCELED, point.getStatus(), "조회된 status 가 일치하지 않습니다.")
        );
    }

    @Test
    @DisplayName("정상 포인트 환불 내역 취소 요청시 해당 포인트 량 반환")
    void cancelExchangePoint() {
        //Given
        User user = userRepository.save(generateUser());
        final int AMOUNT = 5000;

        PointVo pointVo = generatePointVo(user.getId(), AMOUNT, user.getPoint());
        Long pointId = pointLogService.exchangePoint(pointVo);

        //When
        Integer result = pointLogService.cancelPoint(pointId, user.getId());

        //Then
        Point point = pointRepository.findAllByUserId(user.getId()).get(0);
        Assertions.assertAll(
                () -> assertEquals(AMOUNT, result, "취소된 포인트 량이 일치하지 않습니다."),
                () -> assertEquals(PointType.EXCHANGE, point.getType(), "조회된 type 이 일치하지 않습니다."),
                () -> assertEquals(PointStatus.CANCELED, point.getStatus(), "조회된 status 가 일치하지 않습니다.")
        );
    }
    @Test
    @DisplayName("존재하지 않는 pointId 조회 요청시 POINT_NOT_FOUND 메시지 발생")
    void getPointLogInvalidPointId() {
        //Given
        User user = userRepository.save(generateUser());
        final int AMOUNT = 5000;

        PointVo pointVo = generatePointVo(user.getId(), AMOUNT, user.getPoint());
        pointLogService.chargePoint(pointVo);

        //When
        CustomException exception = assertThrows(CustomException.class,
                ()-> pointLogService.getPointLog(-1L));

        //Then
        assertEquals(ErrorCode.POINT_NOT_FOUND.getMessage(),exception.getMessage());
    }

    @Test
    @DisplayName("정상 포인트 로그 조회 요청시 해당 로그 정보 반환")
    void getPointLog() {
        //Given
        User user = userRepository.save(generateUser());
        final int AMOUNT = 5000;

        PointVo pointVo = generatePointVo(user.getId(), AMOUNT, user.getPoint());
        Long pointId = pointLogService.chargePoint(pointVo);

        //When
       PointInfoVo result = pointLogService.getPointLog(pointId);

        //Then
        Assertions.assertAll(
                () -> assertEquals(pointId, result.getId(), "조회된 ID 가 일치하지 않습니다."),
                () -> assertEquals(pointVo.getAmount(), result.getAmount(), "조회된 포인트 량이 일치하지 않습니다."),
                () -> assertEquals(pointVo.getAccount(), result.getAccount(), "조회된 계좌 정보가 일치하지 않습니다."),
                () -> assertEquals(PointType.CHARGE, result.getType(), "조회된 type이 일치하지 않습니다."),
                () -> assertEquals(PointStatus.CREATED, result.getStatus(), "조회된 status 가 일치하지 않습니다.")
        );
    }

    @Test
    @DisplayName("존재하지 않는 pointId 포인트 내역 조회 요청시 빈 리스트 반환")
    void getAllPointLogInvalidPointId() {
        //Given
        User user = userRepository.save(generateUser());
        final int AMOUNT = 5000;
        for(int i = 0 ; i < 3 ; i++){
            pointLogService.chargePoint(generatePointVo(user.getId(), AMOUNT, user.getPoint()));
        }

        //When
        List<PointInfoVo> result = pointLogService.getAllPointLog(1, 20,-1L);

        //Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("정상 포인트 내역 조회 요청시 생성 시각 내림차순으로 반환")
    void getAllPointLog() {
        //Given
        User user = userRepository.save(generateUser());
        final int AMOUNT = 5000;
        List<PointVo> pointVoList = new ArrayList<>();
        List<Long> pointIdList = new ArrayList<>();

        for(int i = 0 ; i < 3 ; i++){
            PointVo pointVo = generatePointVo(user.getId(), AMOUNT, user.getPoint());
            pointIdList.add(pointLogService.chargePoint(pointVo));
            pointVoList.add(pointVo);
        }

        //When
        List<PointInfoVo> result = pointLogService.getAllPointLog(1, 20,user.getId());

        //Then
        for(int i = 0 ; i < 3 ; i++){
            int pointVoIdx = pointVoList.size() - 1 - i;
            assertEquals(pointIdList.get(pointVoIdx), result.get(i).getId(), "조회된 ID 가 일치하지 않습니다.");
            assertEquals(pointVoList.get(pointVoIdx).getAmount(), result.get(i).getAmount(), "조회된 포인트 량이 일치하지 않습니다.");
            assertEquals(pointVoList.get(pointVoIdx).getAccount(), result.get(i).getAccount(), "조회된 계좌 정보가 일치하지 않습니다.");
            assertEquals(PointType.CHARGE, result.get(i).getType(), "조회된 type이 일치하지 않습니다.");
            assertEquals(PointStatus.CREATED, result.get(i).getStatus(), "조회된 status 가 일치하지 않습니다.");
        }
    }

    private PointVo generatePointVo(Long userId, Integer amount, Integer balance) {
        return PointVo.builder()
                .userId(userId)
                .amount(amount)
                .balance(balance)
                .account("123-11113-2121")
                .build();
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