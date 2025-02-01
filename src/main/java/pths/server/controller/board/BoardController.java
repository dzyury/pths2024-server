package pths.server.controller.board;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pths.server.model.Board;
import pths.server.model.Turn;
import pths.server.service.board.BoardService;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @GetMapping("/board/1")
    public Board get() {
        return boardService.get(1);
    }

    @PatchMapping("/board/1")
    public Board turn(@RequestBody Turn turn) {
        return boardService.turn(1, turn.getDetails());
    }

    @PostMapping("/board")
    public Board create(Principal principal) {
        System.out.println(principal.getName());
        return boardService.create();
    }
}
