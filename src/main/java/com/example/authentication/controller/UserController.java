package com.example.authentication.controller;

import com.example.authentication.Util;
import com.example.authentication.entity.Task;
import com.example.authentication.entity.User;
import com.example.authentication.repository.TaskRepository;
import com.example.authentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @PostMapping("/signIn")
    public User signIn(@RequestBody User user) {
        User userFromDB = userRepository.findByEmailIgnoreCaseAndPassword(user.getEmail(),
                user.getPassword());

        return userExists(userFromDB);
    }

    @GetMapping("/signIn/{apiKey}")
    public User signInWithApiKey(@PathVariable String apiKey) {
        User userFromDB = userRepository.findByApiKey(apiKey);

        return userExists(userFromDB);
    }

    @PostMapping("/signUp")
    public User signUp(@RequestBody User user) {
        user.setApiKey(Util.hash("tartiflette_" + System.currentTimeMillis()));
        return userRepository.save(user);
    }

    @PutMapping("/users/{userId}")
    public User update(@PathVariable Long userId,
                       @RequestParam String apiKey,
                       @RequestBody User userUpdate) {
        User userFromDB = userAllowed(userId, apiKey);

        if (userUpdate.getEmail() != null && !userUpdate.getEmail().isEmpty()) {
            userFromDB.setEmail(userUpdate.getEmail());
        }
        if (userUpdate.getPassword() != null && !userUpdate.getPassword().isEmpty()) {
            userFromDB.setPassword(userUpdate.getPassword());
        }
        return userRepository.save(userFromDB);
    }

    @PostMapping("/users/{userId}/tasks")
    public Task addTask(@PathVariable Long userId,
                        @RequestBody Task task,
                        @RequestParam String apiKey) {
        User userFromDB = userAllowed(userId, apiKey);
        task.setUser(userFromDB);
        return taskRepository.save(task);
    }

    @GetMapping("/users/{userId}/tasks")
    public List<Task> addTask(@PathVariable Long userId,
                              @RequestParam String apiKey) {
        User userFromDB = userAllowed(userId, apiKey);
        return userFromDB.getTasks();
    }

    @DeleteMapping("/users/{userId}/tasks/{taskId}")
    public boolean removeTask(@PathVariable Long userId,
                              @PathVariable Long taskId,
                              @RequestParam String apiKey) {
        userAllowed(userId, apiKey);
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (!optionalTask.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Task not found"
            );
        }
        taskRepository.deleteById(taskId);
        return true;
    }

    private User userAllowed(Long userId, String apiKey) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (!optionalUser.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
            );
        }
        User userFromDB = optionalUser.get();
        if (!userFromDB.getApiKey().equals(apiKey)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User not allowed"
            );
        }
        return  userFromDB;
    }

    private User userExists(User user) {
        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
            );
        }
        return user;
    }
}
