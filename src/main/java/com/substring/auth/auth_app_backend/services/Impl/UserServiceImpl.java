package com.substring.auth.auth_app_backend.services.Impl;

import com.substring.auth.auth_app_backend.dtos.Userdto;
import com.substring.auth.auth_app_backend.entities.Provider;
import com.substring.auth.auth_app_backend.entities.User;
import com.substring.auth.auth_app_backend.exceptions.ResourceNotFoundException;
import com.substring.auth.auth_app_backend.helpers.UserHelper;
import com.substring.auth.auth_app_backend.repositories.UserRepository;
import com.substring.auth.auth_app_backend.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Userdto createUser(Userdto userdto) {

        if(userdto.getEmail() == null || userdto.getEmail().isBlank()){
            throw new IllegalArgumentException("Email is required");
        }

        if(userRepository.findByEmail(userdto.getEmail()).isPresent()){
            throw new IllegalArgumentException("Email already exists");
        }

        User user = modelMapper.map(userdto,User.class);
        user.setProvider(userdto.getProvider()!=null ? userdto.getProvider(): Provider.LOCAL);
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser,Userdto.class);
    }

    @Override
    public Userdto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException("User not found with email : " + email));
        return modelMapper.map(user,Userdto.class);
    }

    @Override
    public Userdto updateUser(Userdto userdto, String userId) {
        UUID user_Id = UserHelper.parseUUID(userId);
        User existingUser = userRepository
                .findById(user_Id).
                orElseThrow(()-> new ResourceNotFoundException("User not found on this id: "+ userId));
        if(userdto.getName() != null) existingUser.setName(userdto.getName());
        if(userdto.getImage() != null) existingUser.setImage(userdto.getImage());
        if(userdto.getProvider() != null) existingUser.setProvider(userdto.getProvider());
        // TODO: change password updating logic
        if(userdto.getPassword() != null) existingUser.setPassword(userdto.getPassword());
        existingUser.setEnabled(userdto.isEnabled());
        existingUser.setUpdatedAt(Instant.now());
        User updateUser = userRepository.save(existingUser);
        return modelMapper.map(updateUser,Userdto.class);
    }

    @Override
    public void deleteUser(String userId) {
        UUID user_Id = UserHelper.parseUUID(userId);
        User user = userRepository.findById(user_Id).orElseThrow(()-> new ResourceNotFoundException("User not found on this id: "+ userId));
        userRepository.delete(user);
    }

    @Override
    public Userdto getUserById(String userId) {
        UUID user_Id = UserHelper.parseUUID(userId);
        User user = userRepository
                .findById(user_Id)
                 .orElseThrow(()-> new ResourceNotFoundException("User not found on this id: "+ userId));
        return modelMapper.map(user,Userdto.class);
    }

    @Override
    @Transactional
    public Iterable<Userdto> getAllUsers() {
       return userRepository
               .findAll()
               .stream()
               .map(user->modelMapper.map(user,Userdto.class)).toList();
    }
}
