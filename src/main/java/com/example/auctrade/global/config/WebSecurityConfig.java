package com.example.auctrade.global.config;

import com.example.auctrade.domain.user.service.UserService;
import com.example.auctrade.global.auth.exception.JwtAccessDenyHandler;
import com.example.auctrade.global.auth.exception.JwtAuthenticationEntryPoint;
import com.example.auctrade.global.auth.filter.CustomLoginFilter;
import com.example.auctrade.global.auth.filter.JwtAuthenticationFilter;
import com.example.auctrade.global.auth.filter.JwtAuthorizationFilter;
import com.example.auctrade.global.auth.filter.JwtExceptionFilter;
import com.example.auctrade.global.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity(debug = true)
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final RedisTemplate<String, String> redisTemplate;
    // 필터단 예외
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint; // 인증 예외 커스텀 메시지 던지기
    private final JwtAccessDenyHandler jwtAccessDenyHandler; // 인가 예외 커스텀 메시지 던지기(역할별 접근권한같은)
    private final JwtExceptionFilter jwtExceptionFilter;

    // 인증 매니저 생성
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CustomLoginFilter customLoginFilter() throws Exception {
        CustomLoginFilter filter = new CustomLoginFilter(jwtUtil, userService, redisTemplate);
        filter.setAuthenticationManager(authenticationManager(authenticationConfiguration));
        return filter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF(사이트 간 요청 위조) 설정 비활성화
        http.csrf(AbstractHttpConfigurer::disable);

        // Security 의 기본 설정인 Session 방식이 아닌 JWT 방식을 사용하기 위한 설정
        http.sessionManagement(sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.authorizeHttpRequests(authorizeHttpRequests ->
                        authorizeHttpRequests
                       .requestMatchers( "/error").permitAll()
                                .anyRequest().authenticated()
        );
        //만약 권한이 없는 상태에서 바로 권한 요청을 하는 경우 처리
        http.exceptionHandling(e ->
                e.authenticationEntryPoint(jwtAuthenticationEntryPoint).accessDeniedHandler(jwtAccessDenyHandler));

        // JWT 방식의 REST API 서버이기 때문에 FormLogin 방식, HttpBasic 방식 비활성화(논의 필요)
        // 로그인 사용
        http.formLogin(formLogin ->
                formLogin
                        .loginPage("/login")
                        .loginProcessingUrl("/api/users/login")
                        // 로그인 처리 후 성공 시 URL alwaysUse를 false로 작성해 다른곳에서 요청이 들어왔을때 항상 같은곳으로 가면안된다.
                        .defaultSuccessUrl("/auctions",false)
                        .failureUrl("/login")
                        .permitAll()
        );

        // 필터 체인에 필터 추가 및 순서 지정
        http.addFilterBefore(new JwtAuthorizationFilter(jwtUtil, userDetailsService, userService, redisTemplate),
                CustomLoginFilter.class);
        http.addFilterBefore(new JwtAuthenticationFilter(jwtUtil,userDetailsService, userService, redisTemplate), JwtAuthorizationFilter.class);
        http.addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter.class);
        http.addFilterBefore(customLoginFilter(), UsernamePasswordAuthenticationFilter.class);

        http.logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .deleteCookies(jwtUtil.getAuthorizationHeader())
                );

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return web ->
            web.ignoring()
                    .requestMatchers("/api/users/login")
                    .requestMatchers("/api/users/signup")
                    .requestMatchers("/header")
                    .requestMatchers("/footer")
                    .requestMatchers("/login")
                    .requestMatchers("/img/**")
                    .requestMatchers("/icon/**")
                    .requestMatchers("/error")
                    .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }
}
