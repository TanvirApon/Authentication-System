package com.substring.auth.auth_app_backend.services.Impl;

import com.substring.auth.auth_app_backend.dtos.Userdto;
import com.substring.auth.auth_app_backend.services.AuthService;
import com.substring.auth.auth_app_backend.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService; // for using create user method from User Service
    private final PasswordEncoder passwordEncoder; //  use for saving the password Encoded in database

    @Override
    public Userdto register(Userdto userdto) {
        // Encode the passwor before setting ing
        userdto.setPassword(passwordEncoder.encode(userdto.getPassword()));
        Userdto userdto1 = userService.createUser(userdto); // used ServiceServiceImpl-> create user method
        return userdto1;
    }
}
