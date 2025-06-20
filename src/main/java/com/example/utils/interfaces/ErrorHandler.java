package com.example.utils.interfaces;

import java.util.Map;
import java.util.Map.Entry;

public interface ErrorHandler {
    void logError(Entry<String, String> error);

    boolean retryOperation(Runnable operation, int maxRetries);

    void removeError(String errorName);

    String getError(String errorName);

    Map.Entry<String, String> createErrorBody(String key, String message);
}
