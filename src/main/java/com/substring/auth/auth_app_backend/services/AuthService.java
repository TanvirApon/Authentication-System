package com.substring.auth.auth_app_backend.services;

import com.substring.auth.auth_app_backend.dtos.Userdto;

public interface AuthService {
    Userdto register(Userdto userdto);
}
