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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class AccountService {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final GoogleIdTokenVerifier verifier;

    @Autowired
    public AccountService(@Value("${app.googleClientId}") String clientId, UserRepository userRepository,
                          TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        NetHttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

//    public UserEntity getAccount(Long id) {
//        return userRepository.findById(id).orElse(null);
//    }

    //trong nay la idToken
    public String loginOAuthGoogle(IdTokenModel requestBody) {
        UserModel userModel = verifyIDToken(requestBody.getIdToken());
        if (userModel == null||!StringUtils.hasText(userModel.getEmail())) {
            throw new IllegalArgumentException();
        }
        UserEntity userEntity = createOrUpdateUser(userModel);
        return tokenService.createToken(userEntity);
    }


    //TODO check lai payload va map lai
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

    private UserModel verifyIDToken(String idToken) {
        try {
            GoogleIdToken idTokenObj = verifier.verify(idToken);
            if (idTokenObj == null) {
                return null;
            }
            GoogleIdToken.Payload payload = idTokenObj.getPayload();

            UserModel userModel = new UserModel();
            userModel.setEmail(payload.getEmail());
            userModel.setUsername(payload.getEmail());
            userModel.setName((String) payload.get("name"));
            userModel.setImg_url((String) payload.get("picture"));
            return userModel;
        } catch (GeneralSecurityException | IOException e) {
            return null;
        }
    }
}
