package com.example.forumx_user.service;

import com.example.forumx_user.entity.UserEntity;
import com.example.forumx_user.model.IdTokenModel;
import com.example.forumx_user.model.UserModel;
import com.example.forumx_user.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class AccountService {
    private final UserRepository userRepository;
    private final TokenService tokenService;

    @Autowired
    public AccountService(UserRepository userRepository, TokenService tokenService){
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @Transactional
    public UserEntity createOrUpdateUser(UserModel userModel) {
        UserEntity existingUser = userRepository.findByEmail(userModel.getEmail());
        if (existingUser == null) {
            UserEntity newUser = new UserEntity();
            newUser.setRole("USER");
            newUser.setEnabled(true);
            newUser.setEmail(userModel.getEmail());
            newUser.setUsername(userModel.getEmail());
            if(StringUtils.hasText(userModel.getName())) {
                newUser.setName(userModel.getName());
            }
            if(StringUtils.hasText(userModel.getImg_url())) {
                newUser.setImg_url(userModel.getImg_url());
            }
            return userRepository.save(newUser);

        }else{
            existingUser.setName(userModel.getName());
            if(StringUtils.hasText(userModel.getImg_url())) {
                existingUser.setImg_url(userModel.getImg_url());}
            return userRepository.save(existingUser);
        }
    }

    //TODO: check thong tin va map lai
    public UUID findOrRegisterAccount(
            @NonNull String socialUserId,
            @NonNull String socialUserProvider,
            @NonNull Map<String, Object> socialUserInfo
    ) {
        log.info("Looking up or registering social user; id={}; provider={}; info={}", socialUserId, socialUserProvider, socialUserInfo);
        return UUID.randomUUID();
    }
}
