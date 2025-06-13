package com.sig.todaysnews.sevice;

import com.sig.todaysnews.dto.UserDto;
import com.sig.todaysnews.persistence.entity.User;


public interface UserService {
    default UserDto entityToDto(User user) {
        //TODO 권한 추가
        UserDto userDto = UserDto.builder()
                .username(user.getUsername())
                .password("")
                .build();

        return userDto;
    }
}