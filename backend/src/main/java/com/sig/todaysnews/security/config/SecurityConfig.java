package com.sig.todaysnews.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.sig.todaysnews.redis.RedisService;
import com.sig.todaysnews.security.handler.JwtAccessDeniedHandler;
import com.sig.todaysnews.security.handler.JwtAuthenticationEntryPoint;
import com.sig.todaysnews.security.TokenProvider;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final RedisService redisService;

    // PasswordEncoder는 BCryptPasswordEncoder를 사용
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement((sessionManagement) -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/auth/login").permitAll() // 로그인 api
                        .requestMatchers("/user/dup-check").permitAll() // 회원가입 api
                        .requestMatchers("/user/signup").permitAll() // 회원가입 api
                        .requestMatchers("/news/proposal").permitAll() // 개인화 api
                        .requestMatchers("/news/section").permitAll() // 섹션 조회 api
                        .requestMatchers("/news/hottopic").permitAll() // 핫토픽 조회 api
                        .requestMatchers("/news/cluster").permitAll() // 클러스터 조회 api
                        .requestMatchers("/favicon.ico").permitAll()
                        .anyRequest().authenticated() // 그 외 인증 없이 접근X
                )
                .exceptionHandling((exceptionConfig) -> exceptionConfig
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .apply(new JwtSecurityConfig(tokenProvider, redisService)); // JwtFilter를 addFilterBefore로 등록했던 JwtSecurityConfig class 적용

        return httpSecurity.build();
    }
}
