package com.example.utils;

import java.util.EnumSet;

import com.example.model.repository.MessageRepository;
import com.example.model.repository.UserRepository;
import com.example.utils.enums.EnvironmentType;
import com.example.utils.services.SessionService;

public class RepositoryFactory {
    // 'volatile' ensures changes to this variable are visible across all threads
    private static volatile RepositoryFactory instance;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    private RepositoryFactory(EnvironmentType environmentType) {
        if (environmentType == null
                || !EnumSet.of(EnvironmentType.PRODUCTION, EnvironmentType.TEST).contains(environmentType)) {
            System.err.println("❌ Critical Error: Invalid environment type provided.");
            Thread.dumpStack();
            System.exit(1);
        }

        this.userRepository = new UserRepository(environmentType);
        this.messageRepository = new MessageRepository(environmentType);
    }

    /**
     * Returns the singleton instance of SessionService. Uses double-checked locking
     * for thread safety and performance.
     */
    public static RepositoryFactory getInstance(EnvironmentType environmentType) {
        // First check (without locking) to improve performance
        if (instance == null) {
            // Synchronize only when the instance is null (rare after initialization)
            synchronized (SessionService.class) {
                // Second check inside synchronized block to ensure only one instance is created
                if (instance == null) {
                    instance = new RepositoryFactory(environmentType); // Create the singleton instance
                }
            }
        }

        // Return the singleton instance
        return instance;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public MessageRepository getMessageRepository() {
        return messageRepository;
    }
}