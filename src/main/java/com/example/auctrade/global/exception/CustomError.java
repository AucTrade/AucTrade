package com.example.auctrade.global.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomError {
    private String field;
    private String message;
    private String invalidValue;
}
