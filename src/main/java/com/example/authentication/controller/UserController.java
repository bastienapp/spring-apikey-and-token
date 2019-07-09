package com.example.authentication.controller;

import com.example.authentication.Util;
import com.example.authentication.entity.Task;
import com.example.authentication.entity.User;
import com.example.authentication.repository.TaskRepository;
import com.example.authentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/signIn/{apiKey}")
    public User signInWithApiKey(@PathVariable String apiKey) {
        return userRepository.findByApiKey(apiKey);
    }

    @PostMapping("/signUp")
    public User signUp(@RequestBody User user) {
        user.setApiKey(Util.hash("tartiflette_" + System.currentTimeMillis()));
        return userRepository.save(user);
    }

    // TODO : update only if user is allowed
    @PutMapping("/users/{userId}")
    public User update(@PathVariable Long userId,
            @RequestParam String apiKey,
            @RequestBody User userUpdate) {
        User userFromDB = userRepository.findById(userId).get();
        if (!userFromDB.getApiKey().equals(apiKey)) {
            return null;
        }

        if (userUpdate.getEmail() != null && !userUpdate.getEmail().isEmpty()) {
            userFromDB.setEmail(userUpdate.getEmail());
        }
        if (userUpdate.getPassword() != null && !userUpdate.getPassword().isEmpty()) {
            userFromDB.setPassword(userUpdate.getPassword());
        }
        return userRepository.save(userFromDB);
    }

    // TODO : add task only if user is allowed
    @PostMapping("/users/{userId}/tasks")
    public Task addTask(@PathVariable Long userId,
                        @RequestBody Task task,
                        @RequestParam String apiKey) {
        User user = userRepository.findById(userId).get();
        if (!user.getApiKey().equals(apiKey)) {
            return null;
        }
        task.setUser(user);
        return taskRepository.save(task);
    }

    // TODO : list tasks only if user is allowed

    // TODO : remove task only if user is allowed
}
