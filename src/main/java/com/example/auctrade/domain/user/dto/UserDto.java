package com.example.auctrade.domain.user.dto;

import com.example.auctrade.domain.user.entity.UserRoleEnum;
import com.example.auctrade.global.valid.UserValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDate;


public class UserDto {
    private UserDto(){}
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Create{
        @Pattern(regexp ="^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$", groups = UserValidationGroups.EmailPatternGroup.class)
        private String email;
        @NotBlank(message = "비밀번호를 입력해주세요.", groups = UserValidationGroups.PasswordBlankGroup.class)
        private String password;
        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", groups = UserValidationGroups.PhonePatternGroup.class)
        private String phone;
        private String address;
        private LocalDate birth;
        private UserRoleEnum role;
        private String postcode;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Login {
        private String email;
        private String password;
        private UserRoleEnum role;
    }
    @Getter
    @AllArgsConstructor
    public static class Info {
        private Long userId;
        private String email;
        private UserRoleEnum role;
    }
    @Getter
    @AllArgsConstructor
    public static class Point{
        private Long userId;
        private Integer point;
    }

    @Getter
    @AllArgsConstructor
    public static class Result {
        private final Long userId;
        private final Boolean success;
    }
}
