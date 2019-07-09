package com.example.authentication.controller;

import com.example.authentication.entity.User;
import com.example.authentication.repository.TaskRepository;
import com.example.authentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @PostMapping("/signIn")
    public User signIn(@RequestBody User user) {
        return userRepository.findByEmailIgnoreCaseAndPassword(user.getEmail(),
                user.getPassword());
    }

    @PostMapping("/signUp")
    public User signUp(@RequestBody User user) {
        // TODO : add apikey
        return userRepository.save(user);
    }

    // TODO : update only if user is allowed

    // TODO : add task only if user is allowed

    // TODO : list tasks only if user is allowed

    // TODO : remove task only if user is allowed
}
