package com.example.utils;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;

import com.example.utils.interfaces.ErrorHandler;

public class ErrorManager implements ErrorHandler {
    private final Map<String, String> errorMap;

    public ErrorManager(Map<String, String> errorMap) {
        this.errorMap = errorMap;
    }

    @Override
    public void logError(Entry<String, String> error) {
        errorMap.put(error.getKey(), error.getValue());
    }

    @Override
    public boolean retryOperation(Runnable operation, int maxRetries) {
        int attempts = 0;
        while (attempts < maxRetries) {
            try {
                operation.run();
                return true;
            } catch (Exception e) {
                attempts++;
            }
        }
        return false;
    }

    @Override
    public void removeError(String errorName) {
        errorMap.remove(errorName);
    }

    @Override
    public String getError(String errorName) {
        return errorMap.get(errorName);
    }

    @Override
    public Map.Entry<String, String> createErrorBody(String key, String message) {
        return new AbstractMap.SimpleEntry<>(key, message);
    }
}
