package com.example.auctrade.global.config;

import com.example.auctrade.domain.user.service.UserService;
import com.example.auctrade.global.auth.exception.JwtAccessDenyHandler;
import com.example.auctrade.global.auth.exception.JwtAuthenticationEntryPoint;
import com.example.auctrade.global.auth.filter.CustomLoginFilter;
import com.example.auctrade.global.auth.filter.JwtAuthenticationFilter;
import com.example.auctrade.global.auth.filter.JwtAuthorizationFilter;
import com.example.auctrade.global.auth.filter.JwtExceptionFilter;
import com.example.auctrade.global.auth.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import static com.example.auctrade.global.constant.Constants.COOKIE_AUTH_HEADER;


@Configuration
@EnableWebSecurity(debug = false)
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final JwtTokenService jwtTokenService;
    private final AuthenticationConfiguration authenticationConfiguration;
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
        CustomLoginFilter filter = new CustomLoginFilter(jwtTokenService);
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
                        .defaultSuccessUrl("/limits",false)
                        .failureUrl("/login")
                        .permitAll()
        );

        // 필터 체인에 필터 추가 및 순서 지정
        http.addFilterBefore(new JwtAuthorizationFilter(),
                CustomLoginFilter.class);
        http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenService, userService, userDetailsService), JwtAuthorizationFilter.class);
        http.addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter.class);
        http.addFilterBefore(customLoginFilter(), UsernamePasswordAuthenticationFilter.class);

        http.logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .deleteCookies(COOKIE_AUTH_HEADER)
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
