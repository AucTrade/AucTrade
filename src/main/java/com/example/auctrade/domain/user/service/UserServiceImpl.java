package com.example.auctrade.domain.user.service;

import com.example.auctrade.domain.user.dto.UserDTO;
import com.example.auctrade.domain.user.entity.User;
import com.example.auctrade.domain.user.entity.UserDetailsImpl;
import com.example.auctrade.domain.user.mapper.UserMapper;
import com.example.auctrade.domain.user.repository.UserRepository;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.example.auctrade.global.constant.Constants.REDIS_REFRESH_KEY;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedissonClient redissonClient;

    /**
     * 회원가입
     * @param userDto 대상 정보
     * @return 회원 가입 성공 여부
     */
    @Override
    public UserDTO.Result createUser(UserDTO.Create userDto) {
        if (existUserEmail(userDto.getEmail())) throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        userRepository.save(UserMapper.CreateDTOToEntity(userDto, passwordEncoder.encode(userDto.getPassword())));
        return UserMapper.CreateResultDTO(true);
    }

    /**
     * DB 내부 유저 정보 반환
     * @param email 대상 이메일
     * @return 조회된 회원 정보
     */
    @Override
    public UserDTO.Info getUserInfo(String email) {
        return UserMapper.EntityToInfoDTO(findUserByEmail(email));
    }

    /**
     * 로그아웃 요청
     * @param email 대상 이메일
     * @return 로그아웃 성공 여부
     */
    @Override
    public UserDTO.Result logoutUser(String email) {
        // RBucket 사용하여 Redis에서 데이터 삭제
        RBucket<String> refreshTokenBucket = redissonClient.getBucket(REDIS_REFRESH_KEY + email);
        boolean isDeleted = refreshTokenBucket.delete();

        if (!isDeleted) {
            throw new InternalAuthenticationServiceException(ErrorCode.REDIS_INTERNAL_ERROR.getMessage());
        }

        return new UserDTO.Result(true);
    }

    /**
     * 이메일 존재 여부
     * @param email 대상 이메일
     * @return 해당 이메일 존재 여부
     */
    @Override
    public boolean existUserEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public boolean addPoint(Integer point, String email) {
        return findUserByEmail(email).addPoint(point);
    }

    @Override
    public boolean subPoint(Integer point, String email) {
        return findUserByEmail(email).subPoint(point);
    }

    @Override
    public int getPoint(String email) {
        return userRepository.findPointByEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new UserDetailsImpl(userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage())));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
