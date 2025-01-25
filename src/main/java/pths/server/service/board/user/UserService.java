package pths.server.service.board.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pths.server.model.User;
import pths.server.service.board.BoardService;

@Service
@RequiredArgsConstructor
public class UserService {
    private final BoardService boardService;

    public void add(int boardId, User user) {
        boardService.addUser(boardId, user);
    }
}
