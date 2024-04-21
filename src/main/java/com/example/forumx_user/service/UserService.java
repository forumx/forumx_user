package com.example.forumx_user.service;

import com.example.forumx_user.entity.UserEntity;
import com.example.forumx_user.exception.NotFoundException;
import com.example.forumx_user.model.UserModel;
import com.example.forumx_user.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity convertUserModelToEntity(UserModel userModel) {
        return new ModelMapper().map(userModel, UserEntity.class);
    }

    public void createUser(UserModel userModel) {
        if (userRepository.findByEmail(userModel.getEmail()) != null) {
            throw new NotFoundException("Email already exists");
        }
        if (userRepository.findByUsername(userModel.getUsername()) != null) {
            throw new NotFoundException("Username already exists");
        }
        userRepository.save(convertUserModelToEntity(userModel));
    }

    public UserModel fetchUser(Long userId) {
        return new ModelMapper().map(userRepository.findById(userId).orElse(null), UserModel.class);
    }

    public UserModel updateUser(UserModel userModel, Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("User does not exist");
        }
        UserEntity userEntity = userRepository.findByEmail(userModel.getEmail());
        if (userEntity != null && !userEntity.getId().equals(userId)) {
            throw new NotFoundException("Email already exists");
        }
        userEntity = userRepository.findByUsername(userModel.getUsername());
        if (userEntity != null && !userEntity.getId().equals(userId)) {
            throw new NotFoundException("Username already exists");
        }
        userModel.setId(userId);
        userModel.setEnabled(true);
        return new ModelMapper().map(userRepository.save(convertUserModelToEntity(userModel)), UserModel.class);
    }

    public void deleteUser(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("User does not exist");
        }
        UserEntity userEntity = userRepository.findById(userId).orElse(null);
        assert userEntity != null;
        userEntity.setEnabled(false);
    }

    public List<UserModel> getUsersById(List<Long> userIds) {
        return userRepository.findAllById(userIds).stream().map(userEntity -> new ModelMapper().map(userEntity, UserModel.class)).toList();
    }

    public UserModel getUserByUsername(String username) {
        UserEntity userEntity = userRepository.findByUsername(username);
        if (userEntity == null) {
            throw new NotFoundException("User does not exist");
        }
        return new ModelMapper().map(userEntity, UserModel.class);
    }

    public UserModel getUserByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null) {
            throw new NotFoundException("User does not exist");
        }
        return new ModelMapper().map(userEntity, UserModel.class);
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(email);
        if(userEntity != null){
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();

            authorities.add(new SimpleGrantedAuthority(userEntity.getRole()));
            return new User(email, "", authorities);
        }else{
            throw new UsernameNotFoundException("User not found");
        }
    }

//    public UserDetails loadUserById(String id){
//        UserEntity userEntity = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found " + id));
//        return UserPrincipal.create(userEntity);
//    }
}



