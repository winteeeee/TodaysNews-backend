package com.sig.todaysnews.sevice;

import com.sig.todaysnews.dto.UserDto;
import com.sig.todaysnews.persistence.entity.Authority;
import com.sig.todaysnews.persistence.entity.User;
import com.sig.todaysnews.persistence.entity.UserAuthority;
import com.sig.todaysnews.persistence.repository.UserAuthorityRepository;
import com.sig.todaysnews.persistence.repository.UserRepository;
import com.sig.todaysnews.security.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
@Component
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserAuthorityRepository userAuthorityRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDto signup(UserDto userDto) {
        if (userRepository.findOneWithAuthoritiesByUsername(userDto.getUsername()).orElse(null) != null) {
            throw new RuntimeException("이미 가입되어 있는 유저입니다.");
        }

        Authority authority = Authority.builder()
                .authorityName("ROLE_USER")
                .build();

        User user = User.builder()
                .username(userDto.getUsername())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .activated(true)
                .build();
        userRepository.save(user);
        UserAuthority userAuthority = UserAuthority.builder()
                .userId(user.getUserId())
                .authorityName(authority.getAuthorityName()).build();
        userAuthorityRepository.save(userAuthority);

        userDto.setPassword("");
        return userDto;
    }

    public boolean dupCheck(String username) {
        return userRepository.findOneWithAuthoritiesByUsername(username).orElse(null) != null;
    }

    public UserDto getMyUserWithAuthorities() {
        User user = userRepository.findOneWithAuthoritiesByUsername(AuthenticationUtil.getCurrentUsername().get()).get();
        UserDto userDto = null;

        if(user != null) {
            userDto = entityToDto(user);
        }
        return userDto;
    }

    public UserDto getUserWithAuthorities(String username) {
        User user = userRepository.findOneWithAuthoritiesByUsername(username).get();
        UserDto userDto = null;

        if(user != null) {
            userDto = entityToDto(user);
        }

        return userDto;
    }

    public UserDto updateMyUserWithAuthorities(UserDto userDto) {
        UserDto res = null;
        if (userDto.getUsername().equals(AuthenticationUtil.getCurrentUsername().get())) {
            User user = userRepository.findOneWithAuthoritiesByUsername(userDto.getUsername()).get();
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            userRepository.save(user);
            res = entityToDto(user);
        }
        return res;
    }

    public UserDto updateUserWithAuthorities(String username, UserDto userDto) {
        User user = userRepository.findOneWithAuthoritiesByUsername(username).get();
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userRepository.save(user);
        return entityToDto(user);
    }

    public boolean deleteMyUserWithAuthorities() {
        User user = userRepository.findOneWithAuthoritiesByUsername(AuthenticationUtil.getCurrentUsername().get()).get();
        userRepository.delete(user);
        return userRepository.findOneWithAuthoritiesByUsername(AuthenticationUtil.getCurrentUsername().get()).orElse(null) == null;
    }
    public boolean deleteUserWithAuthorities(String username) {
        User user = userRepository.findOneWithAuthoritiesByUsername(username).get();
        userRepository.delete(user);
        return userRepository.findOneWithAuthoritiesByUsername(AuthenticationUtil.getCurrentUsername().get()).orElse(null) == null;    }
}

