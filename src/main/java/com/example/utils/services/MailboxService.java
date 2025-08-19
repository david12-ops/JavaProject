package com.example.utils.services;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.mindrot.jbcrypt.BCrypt;

import com.example.dto.MessageDTO;
import com.example.dto.UserDTO;
import com.example.model.User;
import com.example.model.UserToken;
import com.example.model.repository.MessageRepository;
import com.example.model.repository.UserRepository;
import com.example.utils.RepositoryFactory;
import com.example.utils.ValidationContext;
import com.example.utils.enums.ValidationMode;
import com.example.utils.enums.ViewLevel;
import com.example.utils.enums.EnvironmentType;
import com.example.utils.enums.MessageStatus;
import com.example.utils.enums.OperationType;
import com.example.utils.interfaces.ErrorHandler;
import com.example.utils.interfaces.MailService;
import com.example.utils.interfaces.MessageValidator;

public class MailboxService implements MailService {
    private final ValidationContext validationContext = new ValidationContext(ValidationMode.MESSAGE);
    private final MessageRepository messageRepository;
    private final List<UserDTO> userDTOs;
    private final UserRepository userRepository;

    private final ErrorHandler errorHandler;
    private final MessageValidator messageValidator;

    public MailboxService(EnvironmentType environmentType) {
        this.userRepository = RepositoryFactory.getUserRepository(environmentType);
        this.messageRepository = RepositoryFactory.getMessageRepository(environmentType);
        errorHandler = validationContext.getMessageValidationBundle().getErrorManager();
        messageValidator = validationContext.getMessageValidationBundle().getValidator();

        if (environmentType == EnvironmentType.TEST)
            setTestUsersList(userRepository);

        this.userDTOs = userRepository.getAllUserDtos();
    }

    private record LabeledValue(String label, Object value) {
    }

    // Support Methods
    private void setTestUsersList(UserRepository userRepository) {
        List<User> users = new ArrayList<>();

        users.add(new User("1", "groupA", "alice@example.com", BCrypt.hashpw("hashedPassword1!", BCrypt.gensalt()),
                null));
        users.add(
                new User("2", "groupA", "bob@example.com", BCrypt.hashpw("hashedPassword2!", BCrypt.gensalt()), null));
        users.add(new User("3", "groupB", "charlie@example.com", BCrypt.hashpw("hashedPassword3!", BCrypt.gensalt()),
                null));
        users.add(
                new User("4", "groupB", "dave@example.com", BCrypt.hashpw("hashedPassword4!", BCrypt.gensalt()), null));
        users.add(
                new User("5", "groupC", "eve@example.com", BCrypt.hashpw("hashedPassword5!", BCrypt.gensalt()), null));

        userRepository.setTestData(users);
    }

    private MessageDTO createMessageDTO(String messageId, String senderId, String senderMailAccount, String recevierId,
            String recevierMailAccount, String subject, String message, LocalDateTime timestamp,
            List<String> attachedBase64Files, Map<String, EnumSet<MessageStatus>> statuses, List<File> attachedFiles) {
        return new MessageDTO(messageId, senderId, senderMailAccount, recevierId, recevierMailAccount, subject, message,
                timestamp, attachedBase64Files, statuses, attachedFiles);
    }

    private boolean containsDataNull(String location, LabeledValue... labeledValues) {
        for (LabeledValue labeledValue : labeledValues) {
            if (labeledValue.value == null) {
                errorHandler.logError(errorHandler.createErrorBody(labeledValue.label(),
                        "Invalid " + labeledValue.label() + " argument in " + location + "."));
                return true;
            }
        }
        return false;
    }

    private String resolveUserIdByEmail(String email) {
        if (userDTOs == null || userDTOs.size() == 0 || email == null)
            return null;

        for (UserDTO userDTO : userDTOs) {
            String userEmailFromDTO = userDTO.getMailAccount();
            if (userEmailFromDTO != null && userEmailFromDTO.equals(email))
                return userDTO.getUserId();
        }
        return null;
    }

    private String resolveEmailByUserId(String userId) {
        if (userDTOs == null || userDTOs.size() == 0 || userId == null)
            return null;

        for (UserDTO userDTO : userDTOs) {
            String userIdFromDTO = userDTO.getUserId();
            if (userIdFromDTO != null && userIdFromDTO.equals(userId))
                return userDTO.getMailAccount();
        }
        return null;
    }

    private void clearErrors(ErrorHandler errorHandler, String... errorkeys) {
        for (String key : errorkeys)
            errorHandler.removeError(key);
    }

    private UserToken checkUserToken(UserToken userToken) {
        if (userToken == null || userToken.getMailAccount() == null || userToken.getUserId() == null) {
            return null;
        }

        return userToken;
    }

    private List<MessageDTO> filterMessageByUserId(UserToken userToken, List<MessageDTO> messageDTOs) {
        return messageDTOs.stream()
                .filter(messageDTO -> messageDTO.getRecevierId().equals(userToken.getUserId())
                        || messageDTO.getSenderId().equals(userToken.getUserId()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Map<MessageStatus, List<MessageDTO>> aggregateMessageDTOs(UserToken userToken,
            MessageStatus messageStatusFromUI, List<MessageDTO> messageDTOs) {
        List<MessageDTO> matchedMessageDTOsByStatus = new ArrayList<>();
        Map aggregatedByStatusList = new HashMap();

        if (messageDTOs.isEmpty()) {
            aggregatedByStatusList.put(messageStatusFromUI, List.of());
            return aggregatedByStatusList;
        }

        for (MessageDTO messageDTO : messageDTOs) {
            Map<String, EnumSet<MessageStatus>> messageStatusesByUser = messageDTO.getStatuses();
            EnumSet<MessageStatus> messageStatuses = messageStatusesByUser.get(userToken.getUserId());

            if (messageStatuses.contains(messageStatusFromUI)) {
                matchedMessageDTOsByStatus.add(messageDTO);
            }
        }

        aggregatedByStatusList.put(messageStatusFromUI, matchedMessageDTOsByStatus);
        return aggregatedByStatusList;
    }

    private List<MessageDTO> filterMessageDTOsByTokenAndStatus(List<MessageDTO> messageDTOs, UserToken userToken,
            EnumSet<MessageStatus> messageStatusesFromUI) {
        List<MessageDTO> matchedMessageDTOs = new ArrayList<>();
        List<MessageDTO> filteredList = new ArrayList<>();
        filteredList = filterMessageByUserId(userToken, messageDTOs);

        if (!messageStatusesFromUI.contains(MessageStatus.TRASH)) {
            filteredList.removeIf(
                    messageDTO -> messageDTO.getStatuses().get(userToken.getUserId()).contains(MessageStatus.TRASH));
        }

        if (messageStatusesFromUI.size() == 1) {
            matchedMessageDTOs = aggregateMessageDTOs(userToken, messageStatusesFromUI.iterator().next(), filteredList)
                    .get(messageStatusesFromUI.iterator().next());
        } else {
            for (MessageStatus messageStatusFromUI : messageStatusesFromUI) {
                matchedMessageDTOs.addAll(
                        aggregateMessageDTOs(userToken, messageStatusFromUI, filteredList).get(messageStatusFromUI));
                Set<MessageDTO> nonDuplicateDTOs = new HashSet<>(matchedMessageDTOs);
                matchedMessageDTOs = new ArrayList<>(nonDuplicateDTOs);
            }
        }

        return matchedMessageDTOs;
    }

    @Override
    public void sendMessage(UserToken userToken, String recevierEmail, String subject, String message,
            List<File> files) {
        if (containsDataNull("sendMessage function", new LabeledValue("token", checkUserToken(userToken))))
            return;

        Map<String, EnumSet<MessageStatus>> messageStatuses = new HashMap<>();
        boolean isValid = messageValidator.validMessageData(recevierEmail, subject, message)
                && messageValidator.validFiles(files);
        String resolvedRecevierId = resolveUserIdByEmail(recevierEmail);

        if (isValid && !containsDataNull("sendMessage function", new LabeledValue("recevierId", resolvedRecevierId))) {
            /*
             * In this case, i accept to send message myself and it is represented by id
             * (key) - status (value)
             */
            if (userToken.getUserId().equals(resolvedRecevierId)) {
                messageStatuses.put(userToken.getUserId(), EnumSet.of(MessageStatus.SENT, MessageStatus.INBOX));
            } else {
                messageStatuses.put(userToken.getUserId(), EnumSet.of(MessageStatus.SENT));
                messageStatuses.put(resolvedRecevierId, EnumSet.of(MessageStatus.INBOX));
            }

            if (!messageValidator.containsOnlyAllowedStatuses(messageStatuses,
                    EnumSet.of(MessageStatus.INBOX, MessageStatus.SENT))) {
                return;
            }

            MessageDTO newMessageDTO = createMessageDTO(null, userToken.getUserId(), null, resolvedRecevierId, null,
                    subject, message, LocalDateTime.now(), null, messageStatuses, files);
            clearErrors(errorHandler, "recevierId", "token", "statuses", "expectedStatuses", "email", "subject",
                    "message", "file");
            messageRepository.addMessage(newMessageDTO);
        }
    }

    @Override
    public void updateStatus(UserToken userToken, MessageDTO messageDTO, MessageStatus status,
            OperationType operationType) {
        if (containsDataNull("updateStatus function", new LabeledValue("token", checkUserToken(userToken)),
                new LabeledValue("messageDTO", messageDTO), new LabeledValue("status", status)))
            return;

        if (operationType == null || !EnumSet.of(OperationType.UPDATE, OperationType.REMOVE).contains(operationType)) {
            errorHandler
                    .logError(errorHandler.createErrorBody("operationType", "Provided unsupported type of operation."));
            return;
        }

        Map<String, EnumSet<MessageStatus>> currentMessageStatuses = messageDTO.getStatuses();
        String senderId = resolveUserIdByEmail(messageDTO.getSenderMailAccount());
        String recevierId = resolveUserIdByEmail(messageDTO.getRecevierMailAccount());
        boolean containsDataNull = containsDataNull("updateStatus function", new LabeledValue("senderId", senderId),
                new LabeledValue("recevierId", recevierId), new LabeledValue("messageStatuses", currentMessageStatuses),
                new LabeledValue("messageDTOid", messageDTO.getMessageId()));

        if (!containsDataNull) {
            boolean applyUpdate = false;
            EnumSet<MessageStatus> currentMessageStatusesByUser = EnumSet.copyOf(
                    currentMessageStatuses.getOrDefault(userToken.getUserId(), EnumSet.noneOf(MessageStatus.class)));

            if (operationType == OperationType.REMOVE && currentMessageStatusesByUser.contains(status)) {
                applyUpdate = true;
                currentMessageStatusesByUser.remove(status);
            }

            if (operationType == OperationType.UPDATE && messageValidator
                    .isStatusUpdateAllowed(messageDTO.getStatuses().get(userToken.getUserId()), status)
                    && !currentMessageStatusesByUser.contains(status)) {
                applyUpdate = true;
                currentMessageStatusesByUser.add(status);
            }

            if (applyUpdate) {
                MessageDTO updadMessageDTO = createMessageDTO(messageDTO.getMessageId(), senderId,
                        messageDTO.getSenderMailAccount(), recevierId, messageDTO.getRecevierMailAccount(),
                        messageDTO.getSubject(), messageDTO.getMessage(), messageDTO.getTimestamp(),
                        messageDTO.getAttachedBase64Files(), currentMessageStatuses, messageDTO.getAttachedFiles());
                updadMessageDTO.setStatuses(userToken.getUserId(), currentMessageStatusesByUser);

                clearErrors(errorHandler, "token", "messageDTO", "status", "operationType", "senderId", "recevierId",
                        "messageStatuses", "messageDTOid", "statuses");
                messageRepository.updateMessageStatus(messageDTO, updadMessageDTO);
            }
        }
    }

    @Override
    public void removeMessage(UserToken userToken, MessageDTO messageDTO) {
        if (containsDataNull("removeMessage function", new LabeledValue("messageDTO", messageDTO),
                new LabeledValue("token", checkUserToken(userToken))))
            return;

        if (containsDataNull("removeMessage function", new LabeledValue("messageDTOid", messageDTO.getMessageId())))
            return;

        if (messageDTO.getStatuses().get(userToken.getUserId()).contains(MessageStatus.TRASH)) {
            clearErrors(errorHandler, "messageDTO", "token", "messageDTOid");
            messageRepository.removeMessage(messageDTO);
        }

        updateStatus(userToken, messageDTO, MessageStatus.TRASH, OperationType.UPDATE);
    }

    @Override
    public List<MessageDTO> getMessageDTOs(UserToken userToken, EnumSet<MessageStatus> messageStatusesFromUI) {
        List<MessageDTO> messageDTOs = messageRepository.getAllMessageDtos();
        List<MessageDTO> updatedMessageDTOs = new ArrayList<>();

        boolean containsNull = containsDataNull("getMessageDTOs function",
                new LabeledValue("token", checkUserToken(userToken)), new LabeledValue("DTOs", messageDTOs),
                new LabeledValue("statuses", messageStatusesFromUI));

        if (containsNull)
            return List.of();

        filterMessageDTOsByTokenAndStatus(messageDTOs, userToken, messageStatusesFromUI).forEach(messageDTO -> {
            String senderMailAccount = resolveEmailByUserId(messageDTO.getSenderId());
            String recevierMailAccount = resolveEmailByUserId(messageDTO.getRecevierId());

            MessageDTO updatedMessageDTO = createMessageDTO(messageDTO.getMessageId(), messageDTO.getSenderId(),
                    senderMailAccount, messageDTO.getRecevierId(), recevierMailAccount, messageDTO.getSubject(),
                    messageDTO.getMessage(), messageDTO.getTimestamp(), messageDTO.getAttachedBase64Files(),
                    messageDTO.getStatuses(), messageDTO.getAttachedFiles());
            updatedMessageDTO.sanitize(ViewLevel.PUBLIC);
            updatedMessageDTOs.add(updatedMessageDTO);
        });

        clearErrors(errorHandler, "DTOs", "token", "statuses");
        return updatedMessageDTOs;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

}
