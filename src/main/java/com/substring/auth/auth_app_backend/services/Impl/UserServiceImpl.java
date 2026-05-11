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
    @Transactional // Transactional ensures rollback if something fails during database operations
    public Userdto createUser(Userdto userdto) {

        // check if the entered email is null or email section is blank or not
        if(userdto.getEmail() == null || userdto.getEmail().isBlank()){
            throw new IllegalArgumentException("Email is required");
        }

        // check if the user is already registered or not
        if(userRepository.findByEmail(userdto.getEmail()).isPresent()){
            throw new IllegalArgumentException("Email already exists");
        }

        // for mapping the Entity and User DTO
        User user = modelMapper.map(userdto,User.class);
        // set default provider to LOCAL if provider is not mentioned
        user.setProvider(userdto.getProvider()!=null ? userdto.getProvider(): Provider.LOCAL);
        User savedUser = userRepository.save(user); // save to database
        return modelMapper.map(savedUser,Userdto.class);
    }

    @Override
    public Userdto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException("User not found with email : " + email)); // used custom Exception with parameter
        return modelMapper.map(user,Userdto.class);
    }

    @Override
    @Transactional // Transactional ensures rollback if something fails during database operations
    public Userdto updateUser(Userdto userdto, String userId) {
        UUID user_Id = UserHelper.parseUUID(userId); // convert user UUID to String

        // check the user exits or not and used custom exception
        User existingUser = userRepository
                .findById(user_Id).
                orElseThrow(()-> new ResourceNotFoundException("User not found on this id: "+ userId));

        // check if null or not then updated
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
        UUID user_Id = UserHelper.parseUUID(userId); // convert UUID to string using Helper
        User user = userRepository.findById(user_Id).orElseThrow(()-> new ResourceNotFoundException("User not found on this id: "+ userId));
        userRepository.delete(user);
    }

    @Override
    public Userdto getUserById(String userId) {
        UUID user_Id = UserHelper.parseUUID(userId); // convert UUID to string using Helper
        // check the user exits or not and used custom exception
        User user = userRepository
                .findById(user_Id)
                 .orElseThrow(()-> new ResourceNotFoundException("User not found on this id: "+ userId));
        return modelMapper.map(user,Userdto.class);
    }

    @Override
    @Transactional // Transactional ensures rollback if something fails during database operations
    public Iterable<Userdto> getAllUsers() {
       return userRepository
               .findAll()
               .stream()
               .map(user->modelMapper.map(user,Userdto.class)).toList();
    }
}
