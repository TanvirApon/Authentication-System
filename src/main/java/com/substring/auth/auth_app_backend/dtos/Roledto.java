package com.substring.auth.auth_app_backend.dtos;

import lombok.*;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Roledto {
    private UUID uuid =  UUID.randomUUID();
    private String name;
}
