package pths.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Board {
    private int id;
    private BoardStatus status;
    private String details;
    private List<User> users;
}
