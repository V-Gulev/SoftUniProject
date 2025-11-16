package com.fittrack.mainapp.model.dto;

import com.fittrack.mainapp.model.entity.Role;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class UserDto {

    private UUID id;

    private String username;

    private String email;

    private boolean isBlocked;

    private Set<Role> roles;
}