package com.example.forumx_user.api;

import com.example.forumx_user.model.IdTokenModel;
import com.example.forumx_user.service.AccountService;
import com.example.forumx_user.service.UserService;
import com.google.common.net.HttpHeaders;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@CrossOrigin
public class Api {
    private final AccountService accountService;

    public Api(AccountService accountService){
        this.accountService = accountService;
    }

    @GetMapping("/api/getMe")
    public String getMe(Principal p){
        return p.getName();
    }

//    @PostMapping("oauth/login")
//    public ResponseEntity<String> loginWithGoogleOauth2(@RequestBody IdTokenModel requestBody, HttpServletResponse response) {
//        String authToken = accountService.loginOAuthGoogle(requestBody);
//
//        return ResponseEntity.ok(authToken);
//    }
}
