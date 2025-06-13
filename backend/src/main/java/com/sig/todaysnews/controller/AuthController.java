package com.sig.todaysnews.controller;


import com.sig.todaysnews.dto.LoginDto;
import com.sig.todaysnews.dto.UserDto;
import com.sig.todaysnews.redis.RedisService;
import com.sig.todaysnews.security.TokenProvider;
import com.sig.todaysnews.security.filter.JwtFilter;
import com.sig.todaysnews.security.util.AuthenticationUtil;
import com.sig.todaysnews.sevice.UserServiceImpl;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserServiceImpl userService;
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final RedisService redisService;

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@Valid @RequestBody LoginDto loginDto) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.createToken(authentication);
        String refreshToken = tokenProvider.createReFreshToken(authentication);
        ResponseCookie responseCookie = ResponseCookie.from("refresh-token", refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(tokenProvider.getRefreshTokenValidityInSeconds())
                .build();

        redisService.addRefreshTokenByRedis(
                loginDto.getUsername(),
                refreshToken,
                Duration.ofMillis(tokenProvider.getRefreshTokenValidityInSeconds())
        );

        UserDto userDto = userService.getMyUserWithAuthorities();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
        httpHeaders.add(HttpHeaders.SET_COOKIE, responseCookie.toString());
        return new ResponseEntity<>(userDto, httpHeaders, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(ServletRequest servletRequest) {
        redisService.deleteRefreshTokenByRedis(AuthenticationUtil.getCurrentUsername().get());

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String jwt = resolveToken(httpServletRequest);

        redisService.addAccessTokenByRedis(
                AuthenticationUtil.getCurrentUsername().get(),
                jwt,
                Duration.ofMillis(tokenProvider.getTokenValidityInMilliseconds())
        );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "logout");
        return new ResponseEntity<>(httpHeaders, HttpStatus.OK);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(JwtFilter.AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}