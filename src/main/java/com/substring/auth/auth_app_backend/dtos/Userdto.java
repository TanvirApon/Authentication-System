package com.substring.auth.auth_app_backend.dtos;

import com.substring.auth.auth_app_backend.entities.Provider;
import lombok.*;


import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Userdto {
    private UUID id;
    private String email;
    private String name;
    private String password;
    private String image;
    private boolean enabled = true;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
    private Provider provider = Provider.LOCAL;
    private Set<Roledto> roles = new HashSet<>();
}
