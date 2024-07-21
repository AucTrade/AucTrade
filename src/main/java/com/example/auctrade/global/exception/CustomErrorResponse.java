package com.example.auctrade.global.exception;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CustomErrorResponse {
    String statusCode;
    String requestUrl;
    String message;
    String resultCode;

    List<CustomError> errorList;
}