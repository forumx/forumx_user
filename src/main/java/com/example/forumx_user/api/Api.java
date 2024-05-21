package com.example.forumx_user.api;

import com.example.forumx_user.model.UserModel;
import com.example.forumx_user.service.AccountService;
import com.example.forumx_user.service.TokenService;
import com.example.forumx_user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@CrossOrigin
@Slf4j
public class Api {
    private final AccountService accountService;

    private final TokenService tokenService;

    private final UserService userService;

    @Value("${frontend.domain:localhost}")
    private String domain;

    @Autowired
    public Api(AccountService accountService, TokenService tokenService, UserService userService){
        this.accountService = accountService;
        this.tokenService = tokenService;
        this.userService = userService;
    }

    @GetMapping("/api/getMe")
    public UserModel getMe(Principal p){
        log.info(p.toString());
        return userService.getUserByUsername(p.getName());
    }

    @GetMapping("/api/renewJwt")
    public ResponseEntity<String> login(Principal p){
        return ResponseEntity.ok().header("Set-Cookie","AUTH_TOKEN="+ tokenService.createTokenFromUserName(p.getName()) +"; Domain = "+domain+"; Path=/; HttpOnly;").build();
    }

    @GetMapping("/hello")
    public ResponseEntity<String> hello(){
        return ResponseEntity.ok("Hello");
    }
}
