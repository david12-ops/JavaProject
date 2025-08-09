package com.example.utils;

import java.util.EnumSet;

import com.example.model.repository.MessageRepository;
import com.example.model.repository.UserRepository;
import com.example.utils.enums.EnvironmentType;

public class RepositoryFactory {
    private static UserRepository userRepository;
    private static MessageRepository messageRepository;

    private RepositoryFactory() {

    }

    public static UserRepository getUserRepository(EnvironmentType environmentTypeForRepo) {
        if (environmentTypeForRepo == null
                || !EnumSet.of(EnvironmentType.PRODUCTION, EnvironmentType.TEST).contains(environmentTypeForRepo)) {
            System.err.println("❌ Critical Error: Invalid environment type provided.");
            Thread.dumpStack();
            System.exit(1);
        }

        userRepository = new UserRepository(environmentTypeForRepo);
        return userRepository;
    }

    public static MessageRepository getMessageRepository(EnvironmentType environmentTypeForRepo) {
        if (environmentTypeForRepo == null
                || !EnumSet.of(EnvironmentType.PRODUCTION, EnvironmentType.TEST).contains(environmentTypeForRepo)) {
            System.err.println("❌ Critical Error: Invalid environment type provided.");
            Thread.dumpStack();
            System.exit(1);
        }

        messageRepository = new MessageRepository(environmentTypeForRepo);
        return messageRepository;
    }
}