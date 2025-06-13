package com.example.utils.services;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.example.dto.UserDTO;
import com.example.model.UserToken;

public class SessionService {

    // syncronized getInstance/hasMap for multi acces - prevent race conditions
    private ConcurrentMap<String, UserToken> activeSessions;

    // 'volatile' ensures changes to this variable are visible across all threads
    private static volatile SessionService instance;

    private SessionService() {
        this.activeSessions = new ConcurrentHashMap<>();
    }

    /**
     * Returns the singleton instance of SessionService. Uses double-checked locking
     * for thread safety and performance.
     */
    public static SessionService getInstance() {
        // First check (without locking) to improve performance
        if (instance == null) {
            // Synchronize only when the instance is null (rare after initialization)
            synchronized (SessionService.class) {
                // Second check inside synchronized block to ensure only one instance is created
                if (instance == null) {
                    instance = new SessionService(); // Create the singleton instance
                }
            }
        }

        // Return the singleton instance
        return instance;
    }

    public String createSessionId(UserDTO userDTO) {
        String sessionId = UUID.randomUUID().toString();
        this.activeSessions.put(sessionId,
                new UserToken(userDTO.getUserId(), userDTO.getGroupId(), userDTO.getMailAccount()));
        return sessionId;
    }

    public UserToken getUserTokenBySessionId(String id) {
        if (id != null && !id.isBlank()) {
            return this.activeSessions.get(id);
        }
        return null;
    }

    public boolean isUserLoggedIn(String userId) {
        return this.activeSessions.values().stream().anyMatch(user -> user.getUserId().equals(userId));
    }

    public void clearAllSessions() {
        this.activeSessions.clear();
    }

    public void removeSession(String id) {
        if (id != null && !id.isBlank()) {
            this.activeSessions.remove(id);
        }
    }
}
