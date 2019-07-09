package com.example.authentication.controller;

import com.example.authentication.entity.User;
import com.example.authentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/signIn")
    public User signIn(@RequestBody User user) {
        return userRepository.findByEmailIgnoreCaseAndPassword(user.getEmail(),
                user.getPassword());
    }

    @PostMapping("/signUp")
    public User signUp(@RequestBody User user) {
        return userRepository.save(user);
    }
}
