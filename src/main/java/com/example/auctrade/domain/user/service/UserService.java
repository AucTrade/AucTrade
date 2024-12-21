package com.example.auctrade.domain.user.service;

import com.example.auctrade.domain.user.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    UserDto.Info createUser(UserDto.Create userDto);

    UserDto.Info getUserInfo(String email);

    UserDto.Info getUserInfo(Long userId);

    UserDto.Result logoutUser(String email);

    Boolean existUserEmail(String email);

    Boolean addPoint(Long userId, Integer amount);

    Boolean subPoint(Long userId, Integer amount);

    UserDto.Point getPoint(String email);
}
