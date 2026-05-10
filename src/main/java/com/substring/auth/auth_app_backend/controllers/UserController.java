package com.substring.auth.auth_app_backend.controllers;

import com.substring.auth.auth_app_backend.dtos.Userdto;
import com.substring.auth.auth_app_backend.services.UserService;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    // ----------- API for SIMPLE USER BASED CURD OPERATION---------------------

    // API for creating user
    @PostMapping
    public ResponseEntity<Userdto>createUser(@RequestBody Userdto userdto){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userdto));
    }

    // API for GET all the user from the User Service
    @GetMapping
    public ResponseEntity<Iterable<Userdto>>getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // API for GET/search the user by email
    @GetMapping("/email/{email}")
    public ResponseEntity<Userdto>getUserByEmail(@PathVariable String email){
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    // API for GET/search by ID
    @GetMapping("/{userId}")
    public ResponseEntity<Userdto>getUserById(@PathVariable("userId") String userId){
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    // API for Update a user searching by userID
    @PutMapping("/{userId}")
    public ResponseEntity<Userdto>updateUser(@PathVariable("userId") String userId, @RequestBody Userdto userdto){
        return ResponseEntity.ok(userService.updateUser(userdto,userId));
    }

    // API for deleting the user searching by userID
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable("userId") String userId){
        userService.deleteUser(userId);
    }

}
