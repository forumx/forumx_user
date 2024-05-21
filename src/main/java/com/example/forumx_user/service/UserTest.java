package com.example.forumx_user.service;

import com.example.forumx_user.entity.UserEntity;
import com.example.forumx_user.exception.NotFoundException;
import com.example.forumx_user.model.UserModel;
import com.example.forumx_user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Component
public class UserTest {
    @Autowired
    private ApplicationContext context;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private ModelMapper modelMapper = new ModelMapper();

    private UserModel userModel;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userModel = new UserModel();
        userModel.setId(1L);
        userModel.setEmail("test@example.com");
        userModel.setUsername("testuser");
        userModel.setEnabled(true);

        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("test@example.com");
        userEntity.setUsername("testuser");
        userEntity.setEnabled(true);
    }

    @Test
    public void testUserService() {
        Assertions.assertTrue(context.getBean(UserService.class) != null);
    }

    @Test
    void testCreateUser_Success() {
        when(userRepository.findByEmail(userModel.getEmail())).thenReturn(null);
        when(userRepository.findByUsername(userModel.getUsername())).thenReturn(null);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        userService.createUser(userModel);

        verify(userRepository, times(1)).save(any(UserEntity.class));
    }
    @Test
    void testCreateUser_EmailExists() {
        when(userRepository.findByEmail(userModel.getEmail())).thenReturn(userEntity);

        Assertions.assertThrows(NotFoundException.class, () -> {
            userService.createUser(userModel);
        });

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testCreateUser_UsernameExists() {
        when(userRepository.findByUsername(userModel.getUsername())).thenReturn(userEntity);

        Assertions.assertThrows(NotFoundException.class, () -> {
            userService.createUser(userModel);
        });

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testFetchUser_Success() {
        when(userRepository.findById(userModel.getId())).thenReturn(Optional.of(userEntity));

        UserModel fetchedUser = userService.fetchUser(userModel.getId());

        Assertions.assertEquals(userModel.getEmail(), fetchedUser.getEmail());
        Assertions.assertEquals(userModel.getUsername(), fetchedUser.getUsername());
    }



    @Test
    void testUpdateUser_Success() {
        when(userRepository.findById(userModel.getId())).thenReturn(Optional.of(userEntity));
        when(userRepository.findByEmail(userModel.getEmail())).thenReturn(null);
        when(userRepository.findByUsername(userModel.getUsername())).thenReturn(null);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        UserModel updatedUser = userService.updateUser(userModel, userModel.getId());

        Assertions.assertEquals(userModel.getEmail(), updatedUser.getEmail());
        Assertions.assertEquals(userModel.getUsername(), updatedUser.getUsername());
    }

    @Test
    void testUpdateUser_NotFound() {
        when(userRepository.findById(userModel.getId())).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> {
            userService.updateUser(userModel, userModel.getId());
        });

        verify(userRepository, never()).save(any(UserEntity.class));
    }


    @Test
    void testDeleteUser_NotFound() {
        when(userRepository.findById(userModel.getId())).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> {
            userService.deleteUser(userModel.getId());
        });

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testGetUserByUsername_Success() {
        when(userRepository.findByUsername(userModel.getUsername())).thenReturn(userEntity);

        UserModel fetchedUser = userService.getUserByUsername(userModel.getUsername());

        Assertions.assertEquals(userModel.getUsername(), fetchedUser.getUsername());
    }

    @Test
    void testGetUserByUsername_NotFound() {
        when(userRepository.findByUsername(userModel.getUsername())).thenReturn(null);

        Assertions.assertThrows(NotFoundException.class, () -> {
            userService.getUserByUsername(userModel.getUsername());
        });
    }




}
