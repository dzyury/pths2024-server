package pths.server.service.board;

import lombok.val;
import org.springframework.stereotype.Service;
import pths.server.model.Board;
import pths.server.model.User;

import java.util.ArrayList;
import java.util.List;

import static pths.server.model.BoardStatus.WAITING;

@Service
public class BoardService {
    private int lastUsedId = 0;
    private final List<Board> boards = new ArrayList<>();

    public synchronized Board create() {
        val id = ++lastUsedId;
        val board = new Board(id, WAITING, "_________", new ArrayList<>());
        boards.add(board);
        return board;
    }

    public synchronized Board get(int id) {
        return boards.get(id - 1);
    }

    public synchronized void addUser(int boardId, User user) {
        val board = boards.get(boardId - 1);
        board.getUsers().add(user);

        // Тут если есть два игрока (X, O) то статус переводим в X_TURN
    }

    public synchronized Board turn(int id, String details) {
        val board = boards.get(id - 1);
        // Тут надо прописать логику хода
        // Желательно заинжектить уже существующий класс из игры
        // и использовать его метод(ы)
        return board;
    }
}
