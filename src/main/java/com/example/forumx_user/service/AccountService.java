package com.example.forumx_user.service;

import com.example.forumx_user.entity.UserEntity;
import com.example.forumx_user.model.UserModel;
import com.example.forumx_user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

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
    public Long createOrUpdateUser(UserModel userModel, String mode) {
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
            return userRepository.save(newUser).getId();

        }else if(mode.equals("update")){
            if(userModel.getName().equals(userModel.getEmail())) {
                existingUser.setName(userModel.getName());
            }
            if(StringUtils.hasText(userModel.getImg_url())) {
                existingUser.setImg_url(userModel.getImg_url());}
            return userRepository.save(existingUser).getId();
        }
        return existingUser.getId();
    }

    //TODO: check thong tin va map lai
    public Long findOrRegisterAccount(
            @NonNull String socialUserId,
            @NonNull String socialUserProvider,
            @NonNull Map<String, Object> socialUserInfo
    ) {
        log.info("Looking up or registering social user; id={}; provider={}; info={}", socialUserId, socialUserProvider, socialUserInfo);
        UserModel userModel = new UserModel();
        userModel.setUsername((String) socialUserInfo.get("email"));
        userModel.setEmail((String) socialUserInfo.get("email"));
        userModel.setImg_url((String) socialUserInfo.get("picture"));
        userModel.setName((String) socialUserInfo.get("name"));
        String mode;
        if(socialUserProvider.equals("auth0")){
            mode = "add";
        }else{
            mode = "update";
        }
        return createOrUpdateUser(userModel, mode);
    }
}
