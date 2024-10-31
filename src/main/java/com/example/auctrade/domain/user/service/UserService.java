package com.example.auctrade.domain.user.service;

import com.example.auctrade.domain.user.dto.UserDTO;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    UserDTO.Result createUser(UserDTO.Create userDto);
    UserDTO.Info getUserInfo(String email);
    UserDTO.Result logoutUser(String email);
    boolean existUserEmail(String email);
    boolean addPoint(Integer point, String email);
    boolean subPoint(Integer point, String email);
    int getPoint(String email);
}
