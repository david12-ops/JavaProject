package com.example.utils.services;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.dto.MessageDTO;
import com.example.dto.UserDTO;
import com.example.model.UserToken;
import com.example.model.repository.MessageRepository;
import com.example.utils.RepositoryFactory;
import com.example.utils.ValidationContext;
import com.example.utils.enums.ValidationMode;
import com.example.utils.enums.ViewLevel;
import com.example.utils.enums.MessageStatus;
import com.example.utils.interfaces.ErrorHandler;
import com.example.utils.interfaces.MailService;
import com.example.utils.interfaces.MessageValidator;

public class MailboxService implements MailService {
    private final ValidationContext validationContext = new ValidationContext(ValidationMode.MESSAGE);
    private final MessageRepository messageRepository = RepositoryFactory.getMessageRepository();
    private final List<UserDTO> userDTOs = RepositoryFactory.getUserRepository().getAllUserDtos();

    private ErrorHandler errorHandler;
    private MessageValidator messageValidator;

    private record LabeledValue(String label, Object value) {
    }

    // Support Methods
    private MessageDTO createMessageDTO(String messageId, String senderId, String senderMailAccount, String recevierId,
            String recevierMailAccount, String subject, String message, LocalDateTime timestamp,
            List<String> attachedBase64Files, Map<String, EnumSet<MessageStatus>> statuses, List<File> attachedFiles) {
        return new MessageDTO(messageId, senderId, senderMailAccount, recevierId, recevierMailAccount, subject, message,
                timestamp, attachedBase64Files, statuses, attachedFiles);
    }

    private boolean containsDataNull(String errorKey, LabeledValue... labeledValues) {
        for (LabeledValue labeledValue : labeledValues) {
            if (labeledValue.value == null) {
                errorHandler.logError(errorHandler.createErrorBody(errorKey, "Invalid " + labeledValue.label()));
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

    private UserToken checkUserToken(UserToken userToken) {
        if (userToken == null || userToken.getMailAccount() == null || userToken.getUserId() == null) {
            return null;
        }

        return userToken;
    }

    private List<MessageDTO> filterMessageByUserId(UserToken userToken, List<MessageDTO> messageDTOs) {
        return messageDTOs.stream().filter(messageDTO -> messageDTO.getRecevierId().equals(userToken.getUserId())
                || messageDTO.getSenderId().equals(userToken.getUserId())).toList();
    }

    private Map<MessageStatus, List<MessageDTO>> aggregateMessageDTOs(UserToken userToken, MessageStatus messageStatus,
            List<MessageDTO> messageDTOs) {
        List<MessageDTO> matchedMessageDTOsByStatus = new ArrayList<>();
        Map aggregatedByStatusList = new HashMap();

        List<MessageDTO> filterMessageDTOs = filterMessageByUserId(userToken, messageDTOs);

        if (filterMessageDTOs.isEmpty()) {
            aggregatedByStatusList.put(messageStatus, List.of());
            return aggregatedByStatusList;
        }

        for (MessageDTO messageDTO : filterMessageDTOs) {
            Map<String, EnumSet<MessageStatus>> statusesByUser = messageDTO.getStatuses();
            EnumSet<MessageStatus> userStatuses = statusesByUser.get(userToken.getUserId());

            if (userStatuses.contains(messageStatus)) {
                matchedMessageDTOsByStatus.add(messageDTO);
            }
        }

        aggregatedByStatusList.put(messageStatus, matchedMessageDTOsByStatus);
        return aggregatedByStatusList;
    }

    private List<MessageDTO> filterMessageDTOsByTokenAndStatus(List<MessageDTO> messageDTOs, UserToken userToken,
            EnumSet<MessageStatus> messageStatusesFromUI) {
        List<MessageDTO> filteredList = new ArrayList<>();

        for (MessageStatus messageStatus : messageStatusesFromUI) {
            List<MessageDTO> matchedMessageDTOs = aggregateMessageDTOs(userToken, messageStatus, messageDTOs)
                    .get(messageStatus);
            filteredList.addAll(matchedMessageDTOs);
        }

        return filteredList;
    }

    public MailboxService() {
        errorHandler = validationContext.getMessageValidationBundle().getErrorManager();
        messageValidator = validationContext.getMessageValidationBundle().getValidator();
    }

    @Override
    public void sendMessage(UserToken userToken, String recevierEmail, String subject, String message,
            List<File> files) {
        if (containsDataNull("sendMessage", new LabeledValue("token", checkUserToken(userToken))))
            return;

        Map<String, EnumSet<MessageStatus>> messageStatuses = new HashMap<>();
        boolean isValid = messageValidator.validMessageData(recevierEmail, subject, message)
                && messageValidator.validFiles(files);
        String resolvedRecevierId = resolveUserIdByEmail(recevierEmail);

        if (isValid && !containsDataNull("sendMessage", new LabeledValue("recevierId", resolvedRecevierId))) {
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
                errorHandler.logError(
                        errorHandler.createErrorBody("addMessage", "Message contains unsupported status values"));
                return;
            }

            MessageDTO newMessageDTO = createMessageDTO(null, userToken.getUserId(), null, resolvedRecevierId, null,
                    subject, message, LocalDateTime.now(), null, messageStatuses, files);
            messageRepository.addMessage(userToken, newMessageDTO);
        }
    }

    @Override
    public void updateStatus(UserToken userToken, MessageDTO messageDTO, MessageStatus newStatus) {
        if (containsDataNull("updateStatus", new LabeledValue("status", newStatus),
                new LabeledValue("messageDTO", messageDTO), new LabeledValue("token", checkUserToken(userToken))))
            return;

        Map<String, EnumSet<MessageStatus>> plainMapStatuses = new HashMap<>();
        EnumSet<MessageStatus> currentMessageStatuses = EnumSet.copyOf(
                messageDTO.getStatuses().getOrDefault(userToken.getUserId(), EnumSet.noneOf(MessageStatus.class)));
        String senderId = resolveUserIdByEmail(messageDTO.getSenderMailAccount());
        String recevierId = resolveUserIdByEmail(messageDTO.getRecevierMailAccount());
        boolean contaisIds = senderId != null && recevierId != null;

        if (contaisIds && messageValidator.isStatusUpdateAllowed(messageDTO.getStatuses().get(userToken.getUserId()),
                newStatus)) {
            currentMessageStatuses.add(newStatus);
            MessageDTO updadMessageDTO = createMessageDTO(messageDTO.getMessageId(), senderId,
                    messageDTO.getSenderMailAccount(), recevierId, messageDTO.getRecevierMailAccount(),
                    messageDTO.getSubject(), messageDTO.getMessage(), messageDTO.getTimestamp(),
                    messageDTO.getAttachedBase64Files(), plainMapStatuses, messageDTO.getAttachedFiles());
            updadMessageDTO.setStatuses(userToken.getUserId(), currentMessageStatuses);

            messageRepository.updateMessageStatus(messageDTO, updadMessageDTO);
        }
    }

    @Override
    public void removeMessage(UserToken userToken, MessageDTO messageDTO) {
        if (containsDataNull("updateStatus", new LabeledValue("messageDTO", messageDTO),
                new LabeledValue("token", checkUserToken(userToken))))
            return;

        if (messageDTO.getStatuses().get(userToken.getUserId()).contains(MessageStatus.TRASH))
            messageRepository.removeMessage(messageDTO);

        updateStatus(userToken, messageDTO, MessageStatus.TRASH);
    }

    @Override
    public List<MessageDTO> getMessageDTOs(UserToken userToken, EnumSet<MessageStatus> messageStatusesFromUI) {
        List<MessageDTO> messageDTOs = messageRepository.getAllMessageDtos();
        List<MessageDTO> updatedMessageDTOs = new ArrayList<>();

        boolean containsNull = containsDataNull("getMessageDTOs", new LabeledValue("token", checkUserToken(userToken)),
                new LabeledValue("DTOs", messageDTOs));

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

        return updatedMessageDTOs;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
}
