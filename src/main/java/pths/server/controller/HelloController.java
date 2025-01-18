package pths.server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pths.server.model.Cat;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        return "hello.txt";
    }

    @GetMapping("/cat")
    public Cat get() {
        return new Cat("kot", 2);
    }

    @PostMapping("/cat")
    public Cat post(@RequestBody Cat cat) {
        System.out.println(cat);
        return new Cat("kot", 2);
    }
}
