package com.example.auctrade.domain.user.service;

import com.example.auctrade.domain.user.dto.UserDTO;
import com.example.auctrade.domain.user.entity.User;
import com.example.auctrade.domain.user.mapper.UserMapper;
import com.example.auctrade.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDTO createUser(UserDTO userDto) {

        log.info("가입한 이메일: " + userDto.getEmail());
        log.info("이미 가입되어 있는가: " + userRepository.findByEmail(userDto.getEmail()).isPresent());

        // 이메일 중복 처리 예외
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("해당 이메일은 이미 가입되어 있습니다");
        }

        log.info("가입한 이메일2: " + userDto.getEmail());
        User user = userRepository.save(UserMapper.toEntity(userDto));

        return UserMapper.toDTO(user);
    }
}
