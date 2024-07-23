package com.example.auctrade.domain.user.service;

import com.example.auctrade.domain.user.dto.UserDTO;
import com.example.auctrade.domain.user.entity.User;
import com.example.auctrade.domain.user.mapper.UserMapper;
import com.example.auctrade.domain.user.repository.UserRepository;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    public UserDTO createUser(UserDTO userDto) {
        if (existUserEmail(userDto.getEmail())) throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        return UserMapper.toDTO(userRepository.save(UserMapper.toEntity(userDto)));
    }

    private boolean existUserEmail(String email){
        return userRepository.findByEmail(email).isPresent();
    }
}
