package com.example.auctrade.domain.user.service;

import com.example.auctrade.domain.user.dto.UserDto;
import com.example.auctrade.domain.user.entity.User;
import com.example.auctrade.domain.user.entity.UserDetailsImpl;
import com.example.auctrade.domain.user.mapper.UserMapper;
import com.example.auctrade.domain.user.repository.UserRepository;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.auctrade.global.constant.Constants.REDIS_REFRESH_KEY;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedissonClient redissonClient;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, RedissonClient redissonClient){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.redissonClient = redissonClient;
    }

    /**
     * 회원가입
     * @param userDto 대상 정보
     * @return 회원 가입 성공 여부
     */
    @Override
    @Transactional
    public UserDto.Info createUser(UserDto.Create userDto) {
        if (existUserEmail(userDto.getEmail())) throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        return UserMapper.toInfoDto(userRepository.save(UserMapper.toEntity(userDto, passwordEncoder.encode(userDto.getPassword()))));
    }

    /**
     * DB 내부 유저 정보 반환
     * @param email 대상 이메일
     * @return 조회된 회원 정보
     */
    @Override
    public UserDto.Info getUserInfo(String email) {
        return UserMapper.toInfoDto(findUserByEmail(email));
    }

    /**
     * DB 내부 유저 정보 반환
     * @param userId 대상 유저 ID
     * @return 조회된 회원 정보
     */
    @Override
    public UserDto.Info getUserInfo(Long userId) {
        return UserMapper.toInfoDto(findUserById(userId));
    }

    /**
     * 로그아웃 요청
     * @param email 대상 이메일
     * @return 로그아웃 성공 여부
     */
    @Override
    public UserDto.Result logoutUser(String email) {
        RBucket<String> refreshTokenBucket = redissonClient.getBucket(REDIS_REFRESH_KEY + email);
        boolean isDeleted = refreshTokenBucket.delete();

        if (!isDeleted) {
            throw new InternalAuthenticationServiceException(ErrorCode.REDIS_INTERNAL_ERROR.getMessage());
        }

        return new UserDto.Result(findUserByEmail(email).getId(), true);
    }

    /**
     * 이메일 존재 여부
     * @param email 대상 이메일
     * @return 해당 이메일 존재 여부
     */
    @Override
    public Boolean existUserEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
    
    /**
     * 유저 포인트 추가 요청
     * @param userId 대상 유저 ID
     * @param amount 추가할 포인트 량
     */
    @Override
    @Transactional
    public Boolean addPoint(Long userId, Integer amount) {
        findUserById(userId).addPoint(amount);
        return true;
    }

    /**
     * 유저 포인트 감소 요청
     * @param userId 대상 유저 ID
     * @param amount 감소할 포인트 량
     */
    @Override
    @Transactional
    public Boolean subPoint(Long userId, Integer amount) {
        User user = findUserById(userId);
        if(user.getPoint() < amount) throw new CustomException(ErrorCode.EXCEEDED_POINT_REQUEST);
        user.subPoint(amount);
        return true;
    }
    
    /**
     * 유저 포인트 정보 조회
     * @param email 대상 유저 Email
     * @return 포인트 정보
     */
    @Override
    public UserDto.Point getPoint(String email) {
        return UserMapper.toPointDto(findUserByEmail(email));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new UserDetailsImpl(userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage())));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
