package com.example.auctrade.domain.user.mapper;

import com.example.auctrade.domain.user.dto.UserDto;
import com.example.auctrade.domain.user.entity.User;

public class UserMapper {
    private  UserMapper(){}
    public static User toEntity(UserDto.Create createDto, String password) {
        return (createDto == null) ? null :
                User.builder()
                        .email(createDto.getEmail())
                        .password(password)
                        .phone(createDto.getPhone())
                        .address(createDto.getAddress())
                        .birth(createDto.getBirth())
                        .role(createDto.getRole())
                        .postcode(createDto.getPostcode())
                        .build();
    }

    public static UserDto.Login toLoginDto(User user) {
        return (user == null) ? null : new UserDto.Login(user.getEmail(), user.getPassword(), user.getRole());
    }

    public static UserDto.Info toInfoDto(User user){
        return (user == null) ? null : new UserDto.Info(user.getId(), user.getEmail(), user.getRole());
    }

    public static UserDto.Point toPointDto(User user){
        return (user == null) ? null : new UserDto.Point(user.getId(),user.getPoint());
    }

    public static UserDto.Result toResultDto(Long userId, Boolean isSuccess){
        return new UserDto.Result(userId, isSuccess);
    }
}
