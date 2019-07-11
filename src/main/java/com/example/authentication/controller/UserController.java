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

import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
public class UserController {

    @GetMapping("/date")
    public static void test() {
        Date date = new Date();
        System.out.println(date.getTime());
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @PostMapping("/signIn")
    public User signIn(@RequestBody User userBody,
                       HttpServletResponse response) {
        User user = userRepository.findByEmailIgnoreCaseAndPassword(
                userBody.getEmail(), userBody.getPassword());
        userExists(user);
        user = refreshToken(user);
        response.setHeader("Authentication", user.getToken());
        return userRepository.save(user);
    }

    @GetMapping("/signIn/{apiKey}")
    public User signInWithApiKey(@PathVariable String apiKey,
                                 HttpServletResponse response) {
        User user = userRepository.findByApiKey(apiKey);
        userExists(user);
        user = refreshToken(user);
        response.setHeader("Authentication", user.getToken());
        return userRepository.save(user);
    }

    @PostMapping("/signUp")
    public User signUp(@RequestBody User user,
                       HttpServletResponse response) {
        user.setApiKey(Util.hash("tartiflette_" + System.currentTimeMillis()));
        user = refreshToken(user);
        response.setHeader("Authentication", user.getToken());
        return userRepository.save(user);
    }

    private User refreshToken(User user) {
        user.setToken(Util.hash("tacos_" + System.currentTimeMillis() + "" + Math.random()));
        Date currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        c.add(Calendar.HOUR, 48);
        user.setTokenValidity(c.getTimeInMillis());
        return user;
    }

    @PutMapping("/users/{userId}")
    public User update(@PathVariable Long userId,
                       @RequestParam String apiKey,
                       @RequestBody User userUpdate,
                       @RequestHeader("Authentication") String token) {
        User user = userAllowed(userId, apiKey, token);

        if (userUpdate.getEmail() != null && !userUpdate.getEmail().isEmpty()) {
            user.setEmail(userUpdate.getEmail());
        }
        if (userUpdate.getPassword() != null && !userUpdate.getPassword().isEmpty()) {
            user.setPassword(userUpdate.getPassword());
        }
        return userRepository.save(user);
    }

    @PostMapping("/users/{userId}/tasks")
    public Task addTask(@PathVariable Long userId,
                        @RequestBody Task task,
                        @RequestParam String apiKey,
                        @RequestHeader("Authentication") String token) {
        User user = userAllowed(userId, apiKey, token);
        task.setUser(user);
        return taskRepository.save(task);
    }

    @GetMapping("/users/{userId}/tasks")
    public List<Task> addTask(@PathVariable Long userId,
                              @RequestParam String apiKey,
                              @RequestHeader("Authentication") String token) {
        User user = userAllowed(userId, apiKey, token);
        return user.getTasks();
    }

    @DeleteMapping("/users/{userId}/tasks/{taskId}")
    public boolean removeTask(@PathVariable Long userId,
                              @PathVariable Long taskId,
                              @RequestParam String apiKey,
                              @RequestHeader("Authentication") String token) {
        userAllowed(userId, apiKey, token);
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

    private User userAllowed(Long userId, String apiKey, String token) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (!optionalUser.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
            );
        }
        User user = optionalUser.get();
        if (!user.getApiKey().equals(apiKey)
                || !user.getToken().equals(token)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User not allowed"
            );
        }
        Date now = new Date();
        Date tokenValidity = new Date(user.getTokenValidity());
        if (now.after(tokenValidity)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Session expired"
            );
        }
        return user;
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
