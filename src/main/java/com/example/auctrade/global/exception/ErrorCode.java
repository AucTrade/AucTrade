package com.example.auctrade.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    // 400 BAD_REQUEST: 잘못된 요청
    INVALID_REFRESH_TOKEN(BAD_REQUEST, "리프레시 토큰이 유효하지 않습니다"),
    MISMATCH_REFRESH_TOKEN(BAD_REQUEST, "리프레시 토큰의 유저 정보가 일치하지 않습니다"),
    WRONG_MULTIPARTFILE(BAD_REQUEST, "Multipartfile에 문제가 있습니다"),
    WRONG_USERINFO(BAD_REQUEST,"유저 정보를 다시 확인해주세요"),
    WRONG_TIME_FORMAT(BAD_REQUEST, "잘못된 시간형식입니다"),
    WRONG_PLAN_DATE(BAD_REQUEST, "과거날짜로 플랜을 작성할 수는 없습니다"),
    WRONG_DEPOSIT(BAD_REQUEST, "최소 예치금 보다 낮을 수 없습니다."),
    TRADE_PROCESS_FAILED(BAD_REQUEST, "거래 처리에 실패했습니다."),
    PURCHASE_FAILED(BAD_REQUEST, "구매 처리에 실패했습니다."),


    // 401 UNAUTHORIZED: 인증되지 않은 사용자
    INVALID_AUTH_TOKEN(UNAUTHORIZED, "권한 정보가 없는 토큰입니다"),
    STATUS_NOT_LOGIN(UNAUTHORIZED, "로그인 상태가 아닙니다."),
    UNAUTHORIZED_MEMBER(UNAUTHORIZED, "존재하지 않는 회원입니다."),
    INTERNAL_AUTH(UNAUTHORIZED,"내부 시스템 문제로 로그인할 수 없습니다. 관리자에게 문의하세요."),

    // 403 FORBIDDEN: 권한이 없는 접근
    POST_GET_NOT_PERMISSION(FORBIDDEN, "해당 게시물을 조회할 권한이 없습니다."),
    POST_UPDATE_NOT_PERMISSION(FORBIDDEN, "해당 게시물을 수정할 권한이 없습니다."),
    POST_DELETE_NOT_PERMISSION(FORBIDDEN, "해당 게시물을 삭제할 권한이 없습니다."),
    POST_ACCESS_NOT_PERMISSION(FORBIDDEN, "접근할 수 없는 게시물입니다."),

    // 404 NOT_FOUND: 잘못된 리소스 접근
    ACCESS_TOKEN_NOT_FOUND(NOT_FOUND, "Access 토큰이 없습니다"),
    REFRESH_TOKEN_NOT_FOUND(NOT_FOUND, "로그아웃 된 사용자입니다"),
    MEMBER_NOT_FOUND(NOT_FOUND, "해당 회원 정보를 찾을 수 없습니다."),
    AUCTION_NOT_FOUND(NOT_FOUND, "해당 경매를 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(NOT_FOUND, "해당 물품을 찾을 수 없습니다."),
    PRODUCT_CATEGORY_NOT_FOUND(NOT_FOUND, "해당 물품 카테고리를 찾을 수 없습니다."),
    POST_IMAGE_NOT_FOUND(NOT_FOUND, "해당 게시글의 이미지를 찾을 수 없습니다."),
    USER_NOT_FOUND(NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    USER_ID_MISMATCH(NOT_FOUND, "데이터베이스의 사용자 식별자와 불일치한 사용자 정보입니다."),
    LIMIT_NOT_FOUND(NOT_FOUND, "해당 한정 판매 정보를 찾을 수 없습니다."),

    // 409 CONFLICT: 중복된 리소스 (요청이 현재 서버 상태와 충돌될 때)
    DUPLICATE_EMAIL(CONFLICT, "이미 존재하는 이메일입니다."),
    DUPLICATE_NICKNAME(CONFLICT, "이미 존재하는 닉네임입니다."),
    USER_ALREADY_LOGGED_IN(CONFLICT, "이미 로그인된 사용자입니다."),
    REDIS_INTERNAL_ERROR(CONFLICT, "레디스의 내부 정보와 차이가 있습니다."),
    INSUFFICIENT_STOCK(CONFLICT, "재고가 부족합니다."),
    USER_LIMIT_EXCEEDED(CONFLICT, "구매 한도를 초과했습니다."),
    AUCTION_NOT_ENDED(CONFLICT, "경매가 아직 종료되지 않았습니다."),

    // 500 INTERNAL SERVER ERROR
    SERVER_ERROR(INTERNAL_SERVER_ERROR, "내부 서버 에러입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
