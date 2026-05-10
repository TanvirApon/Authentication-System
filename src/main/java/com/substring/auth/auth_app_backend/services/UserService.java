package com.substring.auth.auth_app_backend.services;

import com.substring.auth.auth_app_backend.dtos.Userdto;

public interface UserService {

    Userdto createUser(Userdto userdto);

    Userdto getUserByEmail(String email);

    Userdto updateUser(Userdto userdto,String userId);

    void deleteUser(String userId);

    Userdto getUserById(String userId);

    Iterable<Userdto> getAllUsers();
}
