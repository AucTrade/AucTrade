package com.example.auctrade.domain.user.service;

import com.example.auctrade.domain.user.dto.UserDTO;
import com.example.auctrade.domain.user.entity.User;
import com.example.auctrade.domain.user.mapper.UserMapper;
import com.example.auctrade.domain.user.repository.UserRepository;
import com.example.auctrade.global.auth.util.JwtUtil;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.example.auctrade.global.exception.ErrorCode.USER_ID_MISMATCH;


@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisRefreshToken;


    public UserDTO createUser(UserDTO userDto) {
        if (existUserEmail(userDto.getEmail())) throw new CustomException(ErrorCode.DUPLICATE_EMAIL);

        String password = passwordEncoder.encode(userDto.getPassword());
        return UserMapper.toDTO(userRepository.save(UserMapper.toEntity(userDto, password)));
    }


    public UserDTO.Login getUserInfo(String email) {
        return new UserDTO.Login(findUserByEmail(email));
    }


    public UserDTO logoutUser(User user) {
        String refreshTokenKey = jwtUtil.getRefreshTokenKey() + user.getEmail();
        redisRefreshToken.delete(refreshTokenKey);

        if (redisRefreshToken.opsForValue().get(refreshTokenKey) != null) {
            throw new CustomException(USER_ID_MISMATCH);
        }

        return UserMapper.toDTO(user);
    }

    public boolean existUserEmail(String email){
        return userRepository.findByEmail(email).isPresent();
    }
    private User findUserByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
