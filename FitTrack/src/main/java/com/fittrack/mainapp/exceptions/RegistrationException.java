package com.fittrack.mainapp.exceptions;

import com.fittrack.mainapp.model.dto.UserRegistrationDto;
import lombok.Getter;

@Getter
public class RegistrationException extends RuntimeException {

    private final UserRegistrationDto registrationDto;

    public RegistrationException(String message, UserRegistrationDto registrationDto) {
        super(message);
        this.registrationDto = registrationDto;
    }
}