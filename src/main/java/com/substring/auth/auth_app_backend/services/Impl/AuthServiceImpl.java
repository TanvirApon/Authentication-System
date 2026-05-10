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

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Userdto register(Userdto userdto) {
        // Encode the passwor before setting ing
        userdto.setPassword(passwordEncoder.encode(userdto.getPassword()));
        Userdto userdto1 = userService.createUser(userdto);
        return userdto1;
    }
}
