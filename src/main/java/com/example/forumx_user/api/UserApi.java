package com.example.forumx_user.api;

import com.example.forumx_user.model.UserModel;
import com.example.forumx_user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class UserApi {
    private final UserService userService;

    @Autowired
    public UserApi(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    public void createUser(UserModel userModel) {
        userService.createUser(userModel);
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<UserModel> updateUser(UserModel userModel, @PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userService.updateUser(userModel, userId));
    }

    @DeleteMapping("/users/{userId}")
    public void deleteUser(@PathVariable("userId") Long userId) {
        userService.deleteUser(userId);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserModel> fetchUser(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userService.fetchUser(userId));
    }

    @GetMapping("/users/email")
    public ResponseEntity<UserModel> fetchUserByEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping("/users/username")
    public ResponseEntity<UserModel> fetchUserByUsername(@RequestParam("username") String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @PostMapping("/users/batch")
    public ResponseEntity<List<UserModel>> getBatchUsers(@RequestBody List<Long> userIds) {
        return ResponseEntity.ok(userService.getUsersById(userIds));
    }
}
