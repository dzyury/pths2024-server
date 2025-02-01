package pths.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pths.server.model.UserInfo;
import pths.server.security.MapUserDetailsService;

@RestController
@RequiredArgsConstructor
public class UserInfoController {
    private final MapUserDetailsService authService;

    @PostMapping("/user")
    public UserInfo create(@RequestBody UserInfo user) {
        authService.add(user.getName(), user.getPassword());
        return user;
    }
}