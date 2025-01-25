package pths.server;

import lombok.val;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ServerApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void start() throws Exception {
        mockMvc.perform(post("/board"))
                .andExpect(status().isOk());
//                .andDo(print());

        String turn = """
                {
                    "details": "x________"
                }""";
        try {
            mockMvc.perform(patch("/board/1")
                            .content(turn)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print());
            Assertions.fail("Ожидается ошибка");
        } catch (Exception e) {
            // OK, перед началом игры должно быть два игрока (X, O)
        }
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
        var turn = lines.substring(1, lines.length() - 1)
                .replace("|", "")
                .replace(' ', '_');

        if (turn.isBlank()) {
            mockMvc.perform(post("/board"))
                    .andExpect(status().isOk());

            val userX = """
                    {
                      "name": "kit",
                      "position": "X"
                    }
                    """;
            mockMvc.perform(post("/board/1/user")
                            .content(userX)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            val userO = """
                    {
                      "name": "kot",
                      "position": "O"
                    }
                    """;
            mockMvc.perform(post("/board/1/user")
                            .content(userO)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        if (!turn.isBlank()) {
            String request = """
                    {"details": "D"}
                    """.replace("D", turn);
            mockMvc.perform(patch("/board/1")
                            .content(request)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        val expected = turn.isBlank() ? "_________" : turn;
        mockMvc.perform(get("/board/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status", Matchers.is(status)))
                .andExpect(jsonPath("details", Matchers.is(expected)))
                .andDo(print());
    }
}
