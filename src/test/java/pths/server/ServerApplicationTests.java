package pths.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pths.server.model.Board;
import pths.server.model.BoardStatus;
import pths.server.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pths.server.model.Position.O;
import static pths.server.model.Position.X;

@SpringBootTest
@AutoConfigureMockMvc
class ServerApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private static Board board;

    @Test
    public void startWithNoUsers() throws Exception {
        val board = createBoard();
        try {
            val details = "x________";
            turn(board.getId(), details);
            Assertions.fail("Ожидается ошибка");
        } catch (Exception e) {
            // OK, перед началом игры должно быть два игрока (X, O)
        }
    }

    @Test
    public void startWithOnlyXUser() throws Exception {
        val board = createBoard();
        attachUser(board.getId(), new User("kit", X));
        try {
            val details = "x________";
            turn(board.getId(), details);
            Assertions.fail("Ожидается ошибка");
        } catch (Exception e) {
            // OK, перед началом игры должно быть два игрока (X, O)
        }
    }

    @Test
    public void startWithOnlyOUser() throws Exception {
        val board = createBoard();
        attachUser(board.getId(), new User("kot", O));
        try {
            val details = "x________";
            turn(board.getId(), details);
            Assertions.fail("Ожидается ошибка");
        } catch (Exception e) {
            // OK, перед началом игры должно быть два игрока (X, O)
        }
    }

    @Test
    public void reattachOnX() throws Exception {
        val board = createBoard();
        attachUser(board.getId(), new User("kit", X));
        try {
            attachUser(board.getId(), new User("kot", X));
            Assertions.fail("Ожидается ошибка");
        } catch (Exception e) {
            // OK, нельзя сесть на занятое место
        }
    }

    @Test
    public void reattach() throws Exception {
        val board = createBoard();
        attachUser(board.getId(), new User("kit", X)); // сесть за крестиков
        attachUser(board.getId(), new User("kit", O)); // пересесть за ноликов
        attachUser(board.getId(), new User("kit", X)); // снова сесть за крестиков

        attachUser(board.getId(), new User("kot", O));
        val details = "x________";
        turn(board.getId(), details);
    }

    @ParameterizedTest
    @CsvSource({
            "X_TURN, []",
            "O_TURN, [x  |   |   ]",
            "X_TURN, [xo |   |   ]",
            "O_TURN, [xo |x  |   ]",
            "X_TURN, [xo |xo |   ]",
            "X_WON,  [xo |xo |x  ]"
    })
    public void gameX(String status, String lines) throws Exception {
        var details = lines.substring(1, lines.length() - 1)
                .replace("|", "")
                .replace(' ', '_');

        Board newBoard;
        if (details.isBlank()) {
            board = createBoard();
            attachUser(board.getId(), new User("kit", X));
            attachUser(board.getId(), new User("kot", O));
            newBoard = getBoard(board.getId());
        } else {
            newBoard = turn(board.getId(), details);
        }

        val expected = details.isBlank() ? "_________" : details;
        assertEquals(BoardStatus.valueOf(status), newBoard.getStatus());
        assertEquals(expected, newBoard.getDetails());
    }

    @ParameterizedTest
    @CsvSource({
            "X_TURN, []",
            "O_TURN, [x  |   |   ]",
            "X_TURN, [xo |   |   ]",
            "O_TURN, [xo |x  |   ]",
            "X_TURN, [xo |xo |   ]",
            "O_TURN, [xox|xo |   ]",
            "O_WON,  [xox|xo | o ]"
    })
    public void gameO(String status, String lines) throws Exception {
        var details = lines.substring(1, lines.length() - 1)
                .replace("|", "")
                .replace(' ', '_');

        Board newBoard;
        if (details.isBlank()) {
            board = createBoard();
            attachUser(board.getId(), new User("kit", X));
            attachUser(board.getId(), new User("kot", O));
            newBoard = getBoard(board.getId());
        } else {
            newBoard = turn(board.getId(), details);
        }

        val expected = details.isBlank() ? "_________" : details;
        assertEquals(BoardStatus.valueOf(status), newBoard.getStatus());
        assertEquals(expected, newBoard.getDetails());
    }

    @ParameterizedTest
    @CsvSource({
            "X_TURN, []",
            "O_TURN, [x  |   |   ]",
            "X_TURN, [xo |   |   ]",
            "O_TURN, [xo |x  |   ]",
            "X_TURN, [xo |xo |   ]",
            "O_TURN, [xox|xo |   ]",
            "X_TURN, [xox|xo |o  ]",
            "O_TURN, [xox|xox|o  ]",
            "X_TURN, [xox|xox|o o]",
            "DRAW,   [xox|xox|oxo]",
    })
    public void gameD(String status, String lines) throws Exception {
        var details = lines.substring(1, lines.length() - 1)
                .replace("|", "")
                .replace(' ', '_');

        Board newBoard;
        if (details.isBlank()) {
            board = createBoard();
            attachUser(board.getId(), new User("kit", X));
            attachUser(board.getId(), new User("kot", O));
            newBoard = getBoard(board.getId());
        } else {
            newBoard = turn(board.getId(), details);
        }

        val expected = details.isBlank() ? "_________" : details;
        assertEquals(BoardStatus.valueOf(status), newBoard.getStatus());
        assertEquals(expected, newBoard.getDetails());
    }

    @ParameterizedTest
    @CsvSource({
            "[   |   |   ]",
            "[o  |   |   ]",
            "[x  |x  |   ]",
            "[   |xo |   ]"
    })
    public void gameI(String lines) throws Exception {
        var details = lines.substring(1, lines.length() - 1)
                .replace("|", "")
                .replace(' ', '_');

        val board = createBoard();
        attachUser(board.getId(), new User("kit", X));
        attachUser(board.getId(), new User("kot", O));
        try {
            turn(board.getId(), details);
            Assertions.fail();
        } catch (Exception e) {
            // OK, невалидный ход из начального положения
        }
    }

    private Board getBoard(int id) throws Exception {
        val board = mockMvc.perform(get("/board/" + id))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return mapper.readValue(board, Board.class);
    }

    private Board createBoard() throws Exception {
        val board = mockMvc.perform(post("/board"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return mapper.readValue(board, Board.class);
    }

    private void attachUser(int id, User user) throws Exception {
        val body = mapper.writeValueAsString(user);
        mockMvc.perform(post("/board/" + id + "/user")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private Board turn(int id, String details) throws Exception {
        val body = String.format("{\"details\": \"%s\"}", details);
        mockMvc.perform(patch("/board/" + id)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
//                .andDo(MockMvcResultHandlers.print());
        return getBoard(id);
    }
}
