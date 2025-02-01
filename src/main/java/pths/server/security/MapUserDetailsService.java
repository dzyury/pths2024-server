package pths.server.security;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User;
import pths.server.error.HttpException;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.http.HttpStatus.CONFLICT;

@Service
@RequiredArgsConstructor
public class MapUserDetailsService implements UserDetailsService {
    private final PasswordEncoder encoder;
    private final ConcurrentHashMap<String, User> map = new ConcurrentHashMap<>();

//    @PostConstruct
//    public void init() {
//        add("kit", "cat");
//        add("kot", "cat");
//    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        val user = map.get(username);
        if (user == null) throw new UsernameNotFoundException(username);
        return new User(user.getUsername(), user.getPassword(), user.getAuthorities());
    }

    public void add(String username, String password) {
        val encryptedPassword = encoder.encode(password);
        val user = new User(username, encryptedPassword, List.of());
        val message = "User is already registered";
        map.compute(username, (name, v) -> {
            if (v == null) return user;
            throw new HttpException(message, CONFLICT);
        });
    }

    // for testing only
    public void clear() {
        map.clear();
    }
}