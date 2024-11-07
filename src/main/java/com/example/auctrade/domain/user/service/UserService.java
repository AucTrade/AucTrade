package com.example.auctrade.domain.user.service;

import com.example.auctrade.domain.user.dto.UserDTO;
import com.example.auctrade.domain.user.entity.User;

import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    UserDTO.Result createUser(UserDTO.Create userDto);
    UserDTO.Info getUserInfo(String email);
    UserDTO.Result logoutUser(String email);
    boolean existUserEmail(String email);
    boolean updatePoint(Long point, String email);
    int getUserPoint(Long userId);
    boolean updatePointById(Long point, Long userId);
    Long getUserIdByEmail(String email);

}
