package com.example.auctrade.global.auth.filter;

import com.example.auctrade.domain.user.entity.User;
import com.example.auctrade.domain.user.repository.UserRepository;
import com.example.auctrade.global.auth.util.JwtUtil;
import com.example.auctrade.global.auth.util.TokenPayload;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "JWT 검증 및 인가")
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService; // 사용자가 있는지 확인
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisRefreshToken;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        /**
         1. 토큰의 타입부터 확인한다
         2-1. 엑세스토큰이 확인됐다
         2-2. 유효성 판별 후 필터단 넘긴다
         2-3. 만약 만료된 토큰?
         3-1. 리프레쉬토큰이 확인됐다
         3-2. 유효한 리프레쉬토큰이면 리스폰 헤더에 새로 발급한 엑세스토큰 담기
         3-3. 만료된 리프레쉬토큰이면 그냥 예외 반환
         */

        String accessTokenValue = jwtUtil.getAccessTokenFromRequestCookie(request); // -> 요 놈을 써야 해

        if (StringUtils.hasText(accessTokenValue)) {
            log.info("쿠키로부터 갖고 온 엑세스토큰: " + accessTokenValue);
            String accessToken = jwtUtil.substringToken(accessTokenValue);

            /**

             신버전
             1. 쿠키로부터 엑세스 토큰을 갖고온다
             2. if (StringUtils.hasText(accessToken))
             3. 시간 만료를 제외한 유효성 검사부터 먼저 수행한다.
             4. 유효성 검사 통과하면 시간 만료 여부를 확인한다.
             5. 시간이 만료됐을 때, 리프레쉬토큰과의 비교 작업 처리해서 둘을 재발급한다.

             */

            // 엑세스토큰에서 이메일 정보 추출(만료됐든 아니든)
            /**
             *
             *  이 부분 수정하기
             *  1. <String, String> 이런 식으로 RedisTemplate 업데이트해보기
             *  2. 시간 만료가 됐을 때는 만료로 처리를 하는 것이 논리적으로 올을지
             *  3. requestMatchers 메소드여도 필터는 타게 되는 건지(맞는 거 같은데 어케했노)
             *
             * */
            String email = jwtUtil.getUsernameFromAnyToken(accessToken);

            // 이메일로부터 회원 객체 조회
            User user = userRepository.findByEmail(email).orElseThrow(
                    () -> new CustomException(ErrorCode.USER_NOT_FOUND)
            );

            // 이메일로 기존의 리프레쉬토큰 조회
            // redis에 저장된 리프레쉬토큰 갖고오기
            String refreshToken = redisRefreshToken.opsForValue().get(email);

            // 리프레쉬 토큰 만료 확인
            if (jwtUtil.isTokenExpired(refreshToken)) {
                throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
            }

            // 데이터베이스에 저장되어있는지 확인하기
            if (refreshToken == null) {
                throw new CustomException(ErrorCode.USER_ID_MISMATCH);
            }

            // 만약 재발급이 필요없으면 그냥 블랙리스트 여부만 판별 및 확인
            // 발급일자 비교를 통한 블랙리스트 여부 확인
            Date iatAccessToken = jwtUtil.getTokenIat(accessToken);
            Date iatRefreshToken = jwtUtil.getTokenIat(refreshToken);

            log.info("엑세스토큰 발급시간: " + iatAccessToken);
            log.info("리프레쉬토큰 발급시간: " + iatRefreshToken);

            if (!iatRefreshToken.equals(iatAccessToken)) {
                throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
            }

            // 날짜 만료 확인
            // 만약 엑세스토큰이 만료됐으면
            // 새로운 액세스토큰과 리프레쉬토큰을 발급해야 됨
            if (jwtUtil.isTokenExpired(accessToken)) {

                log.info("토큰이 만료됨");
                // 위에까지 전부 통과됐으면 이제 엑세스토큰과 리프레쉬토큰 갱신
                // 인덱스 0: accessTokenPayload, 인덱스 1: refreshTokenPayload
                List<TokenPayload> tokenPayloads = jwtUtil.createTokenPayloads(user.getEmail(), user.getRole());

                String newAccessToken = jwtUtil.createAccessToken(tokenPayloads.get(0));
                String newRefreshToken = jwtUtil.createRefreshToken(tokenPayloads.get(1));
                String refreshTokenKey = JwtUtil.REFRESH_TOKEN_KEY + email;

                log.info("새로운 엑세스토큰: " + newAccessToken);
                log.info("새로운 리프레쉬토큰: " + newRefreshToken);

                // 새로운 리프레쉬토큰 업데이트 저장
                // 7일 + 1시간을 시한으로 설정
                long expirationTime = 24 * 7 + 1;
                redisRefreshToken.opsForValue().set(refreshTokenKey, newRefreshToken.substring(7), expirationTime, TimeUnit.HOURS);

                try {
                    // username 담아주기
                    setAuthentication(email);
                    // 리스폰스 쿠키에 새로운 엑세스토큰 담기
                    jwtUtil.addJwtToCookie(newAccessToken, response);
                } catch (Exception e) {
                    log.error(e.getMessage());
                    return;
                }
            } else {
                try {
                    // username 담아주기
                    setAuthentication(email);
                } catch (Exception e) {
                    log.error(e.getMessage());
                    return;
                }
            }
            // 날짜 만료 제외한 나머지 엑세스 토큰 자체 유효성 판별은 이미 JwtException 필터에서 수행
        }

        // 다음 필터로 넘어가라는 의미
        // 이걸 이용해서 리프레쉬 토큰에 대한 로직 짜면 될 것 같은데
        filterChain.doFilter(request, response);
    }

    // 인증 처리
    public void setAuthentication(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(username);
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

    // 인증 객체 생성
    private Authentication createAuthentication(String username) { // Authentication 인증 객체 만듦
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()); // UPAT 생성
    }
}
