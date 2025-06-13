package com.example;

import java.util.*;
import java.util.Map.Entry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.example.utils.ErrorManager;
import com.example.utils.interfaces.ErrorHandler;

import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ErrorManagerTest {

    private ErrorHandler errorHandler;

    @BeforeEach
    void setup() {
        this.errorHandler = new ErrorManager(new HashMap<>());
    }

    private List<Entry<String, String>> sampleErrors() {
        return Arrays.asList(new AbstractMap.SimpleEntry<>("loginError", "Invalid username or password"),
                new AbstractMap.SimpleEntry<>("addAccount", "Email already exists"),
                new AbstractMap.SimpleEntry<>("formValidation", "Required fields are missing"));
    }

    @Test
    @DisplayName("Should log error with correct key and message")
    void testLogErrorStoresEntry() {
        for (Entry<String, String> error : sampleErrors()) {
            errorHandler.logError(error);
        }

        for (Entry<String, String> error : sampleErrors()) {
            assertNotNull(errorHandler.getError(error.getKey()));
        }

        assertNull(errorHandler.getError("registerError"));
        assertNull(errorHandler.getError("fetchError"));
        assertNull(errorHandler.getError("updateError"));
    }

    @Test
    @DisplayName("Should remove error by name")
    void testRemoveErrorDeletesEntry() {
        for (Entry<String, String> error : sampleErrors()) {
            errorHandler.logError(error);
        }

        for (Entry<String, String> error : sampleErrors()) {
            errorHandler.removeError(error.getKey());
            assertNull(errorHandler.getError(error.getKey()));
        }
    }

    @Test
    @DisplayName("Should retrieve correct error message")
    void testGetErrorReturnsCorrectValue() {
        for (Entry<String, String> error : sampleErrors()) {
            errorHandler.logError(error);
        }

        assertEquals("Invalid username or password", errorHandler.getError("loginError"));
        assertEquals("Email already exists", errorHandler.getError("addAccount"));
        assertEquals("Required fields are missing", errorHandler.getError("formValidation"));

        assertNotEquals("Required fiare missing", errorHandler.getError("formValidation"));
        assertNotEquals("Refiare missing", errorHandler.getError("addAccount"));
    }

    @Test
    @DisplayName("Should create error body with key and message")
    void testCreateErrorBodyProducesEntry() {
        Entry<String, String> loginErr = errorHandler.createErrorBody("loginError", "Invalid username or password");
        Entry<String, String> addAccountErr = errorHandler.createErrorBody("addAccount", "Email already exists");
        Entry<String, String> formValidation = errorHandler.createErrorBody("formValidation",
                "Required fields are missing");

        assertEquals("loginError", loginErr.getKey());
        assertEquals("addAccount", addAccountErr.getKey());
        assertEquals("formValidation", formValidation.getKey());
        assertNotEquals("formValin", formValidation.getKey());

        assertEquals("Invalid username or password", loginErr.getValue());
        assertEquals("Email already exists", addAccountErr.getValue());
        assertEquals("Required fields are missing", formValidation.getValue());
        assertNotEquals("formValin", formValidation.getValue());
    }
}
