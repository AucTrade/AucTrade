package com.example.auctrade.domain.user.service;

import com.example.auctrade.domain.user.dto.UserDTO;
import com.example.auctrade.domain.user.entity.User;

public interface UserService {

    UserDTO createUser(UserDTO userDto);

}
