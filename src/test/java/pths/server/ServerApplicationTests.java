package pths.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pths.server.model.Board;
import pths.server.model.BoardStatus;
import pths.server.model.User;
import pths.server.model.UserInfo;
import pths.server.security.MapUserDetailsService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
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

    @Autowired
    private MapUserDetailsService authService;

    private static Board board;
    private static final Map<String, UserInfo> users = new HashMap<>();

    @BeforeEach
    public void setup() throws Exception {
        authService.clear();
        registerUser("kit");
        registerUser("kot");
        registerUser("kod");
    }

    @Test
    public void startWithNoUsers() throws Exception {
        val board = createBoard();

        val details = "x________";
        turn(board.getId(), "kot", details, UNPROCESSABLE_ENTITY, "Перед началом игры должно быть два игрока");
    }

    @Test
    public void attachWrongUser() throws Exception {
        val board = createBoard();

        val body = mapper.writeValueAsString(new User("kit", X));
        mockMvc.perform(post("/board/" + board.getId() + "/user")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", auth("kot")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("message", is("Игрок не может сесть за поле")));
    }

    @Test
    public void startWithOnlyXUser() throws Exception {
        val board = createBoard();
        attachUser(board.getId(), new User("kit", X));

        val details = "x________";
        turn(board.getId(), "kot", details, UNPROCESSABLE_ENTITY, "Перед началом игры должно быть два игрока");
    }

    @Test
    public void startWithOnlyOUser() throws Exception {
        val board = createBoard();
        attachUser(board.getId(), new User("kot", O));

        val details = "x________";
        turn(board.getId(), "kot", details, UNPROCESSABLE_ENTITY, "Перед началом игры должно быть два игрока");
    }

    @Test
    public void reattachOnX() throws Exception {
        val board = createBoard();
        attachUser(board.getId(), new User("kit", X));
        attachUser(board.getId(), new User("kot", X), UNPROCESSABLE_ENTITY);
    }

    @Test
    public void reattach() throws Exception {
        val board = createBoard();
        attachUser(board.getId(), new User("kit", X)); // сесть за крестиков
        attachUser(board.getId(), new User("kit", O)); // пересесть за ноликов
        attachUser(board.getId(), new User("kit", X)); // снова сесть за крестиков

        attachUser(board.getId(), new User("kot", O));
        val details = "x________";
        turn(board.getId(), "kit", details);
    }

    @Test
    public void gameForbidden0() throws Exception {
        board = createBoard();
        attachUser(board.getId(), new User("kit", X));
        attachUser(board.getId(), new User("kot", O));

        val details = "x________";
        turn(board.getId(), "kod", details, FORBIDDEN, "Игрок не может сделать ход");
    }

    @Test
    public void gameForbidden1() throws Exception {
        board = createBoard();
        attachUser(board.getId(), new User("kit", X));
        attachUser(board.getId(), new User("kot", O));
        attachUser(board.getId(), new User("kod", null));

        val details = "x________";
        turn(board.getId(), "kod", details, FORBIDDEN, "Игрок не может сделать ход");
    }

    @Test
    public void gameForbidden2() throws Exception {
        board = createBoard();
        attachUser(board.getId(), new User("kit", X));
        attachUser(board.getId(), new User("kot", O));

        val details = "x________";
        turn(board.getId(), "kot", details, FORBIDDEN, "Игрок не может сделать ход");
    }

    @Test
    public void gameForbidden3() throws Exception {
        board = createBoard();
        attachUser(board.getId(), new User("kit", X));
        attachUser(board.getId(), new User("kot", O));

        var details = "x________";
        turn(board.getId(), "kit", details);
        details = "xo_______";
        turn(board.getId(), "kit", details, FORBIDDEN, "Игрок не может сделать ход");
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
            val username = "X_TURN".equals(status) ? "kot" : "kit";
            newBoard = turn(board.getId(), username, details);
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
            val username = "O_TURN".equals(status) ? "kit" : "kot";
            newBoard = turn(board.getId(), username, details);
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
            val username = "X_TURN".equals(status) ? "kot" : "kit";
            newBoard = turn(board.getId(), username, details);
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
        turn(board.getId(), "kit", details, UNPROCESSABLE_ENTITY, "Неправильный ход");
    }

    private Board getBoard(int id) throws Exception {
        val board = mockMvc.perform(get("/board/" + id)
                        .header("Authorization", auth("kit")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return mapper.readValue(board, Board.class);
    }

    private Board createBoard() throws Exception {
        val board = mockMvc.perform(post("/board")
                        .header("Authorization", auth("kit")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return mapper.readValue(board, Board.class);
    }

    private void attachUser(int id, User user, HttpStatus status) throws Exception {
        val body = mapper.writeValueAsString(user);
        mockMvc.perform(post("/board/" + id + "/user")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", auth(user.getName())))
                .andExpect(status().is(status.value()))
                .andExpect(jsonPath("message", is("Место уже занято")));
    }

    private void attachUser(int id, User user) throws Exception {
        val body = mapper.writeValueAsString(user);
        mockMvc.perform(post("/board/" + id + "/user")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", auth(user.getName())))
                .andExpect(status().isOk());
    }

    private Board turn(int id, String username, String details, HttpStatus status, String error) throws Exception {
        val body = String.format("{\"details\": \"%s\"}", details);
        mockMvc.perform(patch("/board/" + id)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", auth(username)))
                .andExpect(status().is(status.value()))
                .andExpect(jsonPath("message", is(error)));
//                .andDo(MockMvcResultHandlers.print());
        return getBoard(id);
    }

    private Board turn(int id, String username, String details) throws Exception {
        val body = String.format("{\"details\": \"%s\"}", details);
        mockMvc.perform(patch("/board/" + id)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", auth(username)))
                .andExpect(status().isOk());
//                .andDo(MockMvcResultHandlers.print());
        return getBoard(id);
    }

    private void registerUser(String name) throws Exception {
        val user = new UserInfo(name, "cat");
        users.put(user.getName(), user);

        val body = mapper.writeValueAsString(user);
        mockMvc.perform(post("/user")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private String auth(String username) {
        return "Basic " + users.get(username).getCredentials();
    }
}
