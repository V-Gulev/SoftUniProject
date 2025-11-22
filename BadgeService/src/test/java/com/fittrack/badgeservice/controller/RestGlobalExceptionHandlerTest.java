package com.fittrack.badgeservice.controller;

import com.fittrack.badgeservice.model.dto.ErrorResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RestGlobalExceptionHandlerTest {

    private RestGlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new RestGlobalExceptionHandler();
    }

    @Test
    void testHandleValidationExceptions_ShouldReturnBadRequestWithErrors() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError error1 = new FieldError("badgeAwardDto", "name", "must not be blank");
        FieldError error2 = new FieldError("badgeAwardDto", "userId", "must not be null");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(error1, error2));

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleValidationExceptions(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals("Validation Failed", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("name: must not be blank"));
        assertTrue(response.getBody().getMessage().contains("userId: must not be null"));
    }

    @Test
    void testHandleGenericException_ShouldReturnInternalServerError() {
        Exception exception = new RuntimeException("Unexpected database error");

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleGenericException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("An unexpected error occurred.", response.getBody().getMessage());
    }
}
