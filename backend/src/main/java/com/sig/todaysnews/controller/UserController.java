package com.sig.todaysnews.controller;

import com.sig.todaysnews.dto.UserDto;
import com.sig.todaysnews.sevice.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserServiceImpl userService;

    @GetMapping
    public ResponseEntity<UserDto> getMyUserInfo() {
        UserDto res = userService.getMyUserWithAuthorities();
        return ResponseEntity.ok(res);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMyUserInfo() {
        userService.deleteMyUserWithAuthorities();
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> updateMyUserInfo(
            @Valid @RequestBody UserDto userDto
    ) {
        userService.updateMyUserWithAuthorities(userDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserDto> getUserInfo(
            @PathVariable String username
    ) {
        userService.getUserWithAuthorities(username);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteUserInfo(
            @PathVariable String username
    ) {
        userService.deleteUserWithAuthorities(username);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{username}")
    public ResponseEntity<Void> updateUserInfo(
            @PathVariable String username,
            @Valid @RequestBody UserDto userDto
    ) {
        userService.updateUserWithAuthorities(username, userDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(
            @Valid @RequestBody UserDto userDto
    ) {
        return ResponseEntity.ok(userService.signup(userDto));
    }

    @PostMapping("/dup-check")
    public ResponseEntity<Boolean> dupCheck(
            @RequestBody UserDto userDto
    ) {
        boolean res = userService.dupCheck(userDto.getUsername());
        if (res)
            return ResponseEntity.ok(true);
        else
            return ResponseEntity.ok(false);
    }
}
