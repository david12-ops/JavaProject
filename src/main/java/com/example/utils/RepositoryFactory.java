package com.example.utils;

import com.example.model.repository.MessageRepository;
import com.example.model.repository.UserRepository;
import com.example.utils.enums.EnvironmentType;

public class RepositoryFactory {
    private static final UserRepository userRepository = new UserRepository(EnvironmentType.PRODUCTION);
    private static final MessageRepository messageRepository = new MessageRepository(EnvironmentType.PRODUCTION);

    private RepositoryFactory() {

    }

    public static UserRepository getUserRepository() {
        return userRepository;
    }

    public static MessageRepository getMessageRepository() {
        return messageRepository;
    }
}