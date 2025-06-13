package com.sig.todaysnews.security.filter;

import com.sig.todaysnews.redis.RedisService;
import com.sig.todaysnews.security.TokenProvider;
import com.sig.todaysnews.security.TokenState;
import com.sig.todaysnews.security.util.AuthenticationUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    public static final String AUTHORIZATION_HEADER = "Authorization";
    private final TokenProvider tokenProvider;
    private final RedisService redisService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException, AuthenticationException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String jwt = resolveToken(httpServletRequest);

        if (StringUtils.hasText(jwt)) {
            TokenState tokenState = tokenProvider.validateToken(jwt);

            if (tokenState == TokenState.SUCCESS && !redisService.existsAccessTokenInRedis(jwt)) {
                Authentication authentication = tokenProvider.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            else if (tokenState == TokenState.EXPIRED) {
                Cookie[] rc = ((HttpServletRequest) servletRequest).getCookies();
                jwt = tokenRefresh(rc);

                if (jwt != null) {
                    Authentication authentication = tokenProvider.getAuthentication(jwt);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    ((HttpServletResponse) servletResponse).addHeader(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
                }
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    private String tokenRefresh(Cookie[] rc) {
        String jwt = null;
        for (Cookie cookie : rc) {
            if (cookie.getName().equals("refresh-token")) {
                String refreshToken = cookie.getValue();
                if (tokenProvider.validateToken(refreshToken) != TokenState.SUCCESS) {
                    break;
                }
                Authentication authentication = tokenProvider.getAuthentication(refreshToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                String username = AuthenticationUtil.getCurrentUsername().get();
                SecurityContextHolder.clearContext();

                if (refreshToken.equals(redisService.getRefreshTokenByRedis(username))) {
                    jwt = tokenProvider.createToken(authentication);
                }
                break;
            }
        }
        return jwt;
    }

}