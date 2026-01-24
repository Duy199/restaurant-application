package com.example.RestaurantApplication.module.user.service;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.example.RestaurantApplication.module.user.model.User;
import com.example.RestaurantApplication.module.user.repository.UserRepository;
import com.example.RestaurantApplication.module.user.utils.Exceptions.BusinessException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User loadUserByUsername(String username) {
        return userRepository.findByUserName(username)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found with username " + username, HttpStatus.NOT_FOUND));
    }

    public List<User> loadAllUsers() {
        return userRepository.findAll();
    }

    public User loadUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found with id " + id, HttpStatus.NOT_FOUND));
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

}
