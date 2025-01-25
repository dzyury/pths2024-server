package pths.server.controller.board.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pths.server.model.User;
import pths.server.service.board.user.UserService;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/board/1/user")
    public void create(@RequestBody User user) {
        userService.add(1, user);
    }
}
