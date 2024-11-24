package com.example.auctrade.global.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


@RestControllerAdvice
@Slf4j
public class GlobalControllerAdvice {

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<String> handleDataAccessException(DataAccessException ex) {
        log.error("DataAccessException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database connection error. Please try again later.");
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity methodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest httpServletRequest) {
        log.error("MethodArgumentNotValidException: {}",e.getMessage());
        List<CustomError> errorList = new ArrayList<>();
        BindingResult bindingResult = e.getBindingResult();
        bindingResult.getAllErrors().forEach(error -> {
            FieldError field = (FieldError) error;
            String invalidValue = "";

            if (field.getRejectedValue() != null) {
                invalidValue = field.getRejectedValue().toString();
            }

            errorList.add(CustomError.builder().field(field.getField())
                    .message(field.getDefaultMessage())
                    .invalidValue(invalidValue)
                    .build());
        });

        CustomErrorResponse errorResponse = CustomErrorResponse.builder()
                .errorList(errorList)
                .message("잘못된 입력")
                .requestUrl(httpServletRequest.getRequestURI())
                .statusCode(HttpStatus.BAD_REQUEST.toString())
                .resultCode("FAIL").build();

        return ResponseEntity.status((HttpStatus.BAD_REQUEST)).body(errorResponse);
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity constraintViolationException(ConstraintViolationException e, HttpServletRequest httpServletRequest) {
        log.error("MethodArgumentNotValidException: {}",e.getMessage());
        List<CustomError> errorList = new ArrayList<>();

        e.getConstraintViolations().forEach(error -> {
            Stream<Path.Node> stream = StreamSupport.stream(error.getPropertyPath().spliterator(), false);
            List<Path.Node> list = stream.collect(Collectors.toList());

            errorList.add(CustomError.builder()
                    .field(list.get(list.size() - 1).getName())
                    .message(error.getMessage())
                    .invalidValue(error.getInvalidValue().toString()).build());
        });

        CustomErrorResponse errorResponse = CustomErrorResponse.builder()
                .errorList(errorList)
                .message("")
                .requestUrl(httpServletRequest.getRequestURI())
                .statusCode(HttpStatus.BAD_REQUEST.toString())
                .resultCode("FAIL")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public ResponseEntity missingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest httpServletRequest) {
        log.error("MissingServletRequestParameterException: {}", e.getMessage());
        List<CustomError> errorList = new ArrayList<>();

        errorList.add(CustomError.builder()
                .field(e.getParameterName())
                .message(e.getMessage()).build());

        CustomErrorResponse errorResponse = CustomErrorResponse.builder()
                .errorList(errorList)
                .message("")
                .requestUrl(httpServletRequest.getRequestURI())
                .statusCode(HttpStatus.BAD_REQUEST.toString())
                .resultCode("FAIL").build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(value = JsonProcessingException.class)
    public ResponseEntity jsonProcessingException(JsonProcessingException e, HttpServletRequest httpServletRequest) {
        log.error("jsonProcessingException: {}", e.getMessage());
        List<CustomError> errorList = new ArrayList<>();

        errorList.add(CustomError.builder()
                .message(e.getMessage()).build());

        CustomErrorResponse errorResponse = CustomErrorResponse.builder()
                .errorList(errorList)
                .message("")
                .requestUrl(httpServletRequest.getRequestURI())
                .statusCode(HttpStatus.BAD_REQUEST.toString())
                .resultCode("FAIL").build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(value = CustomException.class)
    public ResponseEntity customException(CustomException e, HttpServletRequest httpServletRequest) {
        log.error("CustomException: {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();

        CustomErrorResponse errorResponse = CustomErrorResponse.builder()
                .message(errorCode.getMessage())
                .requestUrl(httpServletRequest.getRequestURI())
                .statusCode(errorCode.getHttpStatus().toString())
                .resultCode(errorCode.name())
                .build();

        return ResponseEntity.status(errorCode.getHttpStatus()).body(errorResponse);
    }
}
