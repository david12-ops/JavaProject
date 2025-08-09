package com.example;

import org.mockito.junit.jupiter.MockitoExtension;

import com.example.dto.MessageDTO;
import com.example.model.User;
import com.example.model.UserToken;
import com.example.utils.enums.EnvironmentType;
import com.example.utils.enums.MessageStatus;
import com.example.utils.enums.OperationType;
import com.example.utils.interfaces.ErrorHandler;
import com.example.utils.services.MailboxService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
public class MailboxServiceTest {
    private void compareErrors(String expectedMessage, String key, ErrorHandler errorHandler) {
        String errorMessage = errorHandler.getError(key);
        if (errorMessage != null) {
            assertEquals(expectedMessage, errorMessage, "Mismatch in error message for key " + key);
            errorHandler.removeError(key);
        }
    }

    @Test
    @DisplayName("Should send message")
    public void testSendMessage() {
        MailboxService mailboxService = new MailboxService(EnvironmentType.TEST);
        User user = new User("1", "groupA", "alice@example.com", "hashedPassword1!", null);
        User user2 = new User("2", "groupA", "bob@example.com", "hashedPassword2!", null);

        UserToken userToken = new UserToken(user.getUserId(), user.getGroupId(), user.getMailAccount());
        UserToken userToken2 = new UserToken(user2.getUserId(), user2.getGroupId(), user2.getMailAccount());

        mailboxService.sendMessage(userToken, "bob@example.com", "Service sending test", "Testing service sending",
                null);
        mailboxService.sendMessage(userToken2, "alice@example.com", "Service sending test", "Testing service sending",
                null);
        mailboxService.sendMessage(userToken2, "nonexisting@example.com", "Service sending test",
                "Testing service sending", null);

        assertEquals(1, mailboxService.getMessageDTOs(userToken, EnumSet.of(MessageStatus.SENT)).size());
        assertEquals(1, mailboxService.getMessageDTOs(userToken2, EnumSet.of(MessageStatus.SENT)).size());

        mailboxService.sendMessage(null, null, null, null, null);
        compareErrors("Invalid token", "function sendMessage", mailboxService.getErrorHandler());

        UserToken userTokenWithoutId = new UserToken(null, user.getGroupId(), user.getMailAccount());
        mailboxService.sendMessage(userTokenWithoutId, null, null, null, null);
        compareErrors("Invalid token", "function sendMessage", mailboxService.getErrorHandler());

        UserToken userTokenWithoutEmail = new UserToken(user.getUserId(), user.getGroupId(), null);
        mailboxService.sendMessage(userTokenWithoutEmail, null, null, null, null);
        compareErrors("Invalid token", "function sendMessage", mailboxService.getErrorHandler());

        mailboxService.sendMessage(userToken, null, null, null, null);
        compareErrors("Invalid recevierId", "function sendMessage", mailboxService.getErrorHandler());
    }

    @Test
    @DisplayName("Should update status of message")
    public void testUpdateStatus() {
        MailboxService mailboxService = new MailboxService(EnvironmentType.TEST);
        User user = new User("1", "groupA", "alice@example.com", "hashedPassword1!", null);
        User user2 = new User("2", "groupA", "bob@example.com", "hashedPassword2!", null);

        UserToken userToken = new UserToken(user.getUserId(), user.getGroupId(), user.getMailAccount());
        UserToken userToken2 = new UserToken(user2.getUserId(), user2.getGroupId(), user2.getMailAccount());

        mailboxService.sendMessage(userToken, "bob@example.com", "Service sending test", "Testing service sending",
                null);
        mailboxService.sendMessage(userToken2, "alice@example.com", "Service sending test", "Testing service sending",
                null);

        mailboxService.updateStatus(null, null, null, null);
        compareErrors("Invalid token", "function updateStatus", mailboxService.getErrorHandler());

        UserToken userTokenWithoutId = new UserToken(null, user.getGroupId(), user.getMailAccount());
        mailboxService.sendMessage(userTokenWithoutId, null, null, null, null);
        compareErrors("Invalid token", "function updateStatus", mailboxService.getErrorHandler());

        UserToken userTokenWithoutEmail = new UserToken(user.getUserId(), user.getGroupId(), null);
        mailboxService.sendMessage(userTokenWithoutEmail, null, null, null, null);
        compareErrors("Invalid token", "function updateStatus", mailboxService.getErrorHandler());

        mailboxService.updateStatus(userToken,
                mailboxService.getMessageDTOs(userToken, EnumSet.of(MessageStatus.INBOX)).get(0), MessageStatus.STARRED,
                null);
        compareErrors("Invalid operationType", "function updateStatus", mailboxService.getErrorHandler());

        mailboxService.updateStatus(userToken,
                mailboxService.getMessageDTOs(userToken, EnumSet.of(MessageStatus.INBOX)).get(0), MessageStatus.STARRED,
                OperationType.CREATE);
        compareErrors("Invalid operationType", "function updateStatus", mailboxService.getErrorHandler());

        mailboxService.updateStatus(userToken, null, null, null);
        compareErrors("Invalid messageDTO", "function updateStatus", mailboxService.getErrorHandler());

        mailboxService.updateStatus(userToken,
                mailboxService.getMessageDTOs(userToken, EnumSet.of(MessageStatus.INBOX)).get(0), null, null);
        compareErrors("Invalid status", "function updateStatus", mailboxService.getErrorHandler());

        MessageDTO messageDTOWithoutId = mailboxService.getMessageDTOs(userToken, EnumSet.of(MessageStatus.SENT))
                .get(0);
        messageDTOWithoutId.setMessageId(null);
        mailboxService.updateStatus(userToken, messageDTOWithoutId, MessageStatus.STARRED, OperationType.UPDATE);
        compareErrors("Invalid messageDTOid", "function updateStatus", mailboxService.getErrorHandler());

        MessageDTO messageDTO = mailboxService.getMessageDTOs(userToken, EnumSet.of(MessageStatus.INBOX)).get(0);

        mailboxService.updateStatus(userToken, messageDTO, MessageStatus.SENT, OperationType.UPDATE);
        compareErrors("Unsupported update with new status.", "status", mailboxService.getErrorHandler());

        mailboxService.updateStatus(userToken, messageDTO, MessageStatus.STARRED, OperationType.UPDATE);
        assertTrue(messageDTO.getStatuses().get(userToken.getUserId()).contains(MessageStatus.STARRED));

        mailboxService.updateStatus(userToken, messageDTO, MessageStatus.STARRED, OperationType.REMOVE);
        assertFalse(messageDTO.getStatuses().get(userToken.getUserId()).contains(MessageStatus.STARRED));

        mailboxService.updateStatus(userToken, messageDTO, MessageStatus.TRASH, OperationType.UPDATE);
        assertTrue(messageDTO.getStatuses().get(userToken.getUserId()).contains(MessageStatus.TRASH));
        mailboxService.updateStatus(userToken, messageDTO, MessageStatus.TRASH, OperationType.REMOVE);
        assertFalse(messageDTO.getStatuses().get(userToken.getUserId()).contains(MessageStatus.TRASH));
    }

    @Test
    @DisplayName("Should remove message")
    public void testRemoveMessage() {
        MailboxService mailboxService = new MailboxService(EnvironmentType.TEST);
        User user = new User("1", "groupA", "alice@example.com", "hashedPassword1!", null);
        User user2 = new User("2", "groupA", "bob@example.com", "hashedPassword2!", null);

        UserToken userToken = new UserToken(user.getUserId(), user.getGroupId(), user.getMailAccount());
        UserToken userToken2 = new UserToken(user2.getUserId(), user2.getGroupId(), user2.getMailAccount());

        mailboxService.sendMessage(userToken, "bob@example.com", "Service sending test", "Testing service sending",
                null);
        mailboxService.sendMessage(userToken2, "alice@example.com", "Service sending test", "Testing service sending",
                null);
        mailboxService.sendMessage(userToken2, "alice@example.com", "Service sending test twice",
                "Testing service sending twice", null);

        MessageDTO messageDTOtoTrashStatus = mailboxService.getMessageDTOs(userToken2, EnumSet.of(MessageStatus.SENT))
                .get(0);
        mailboxService.removeMessage(userToken2, messageDTOtoTrashStatus);
        assertTrue(messageDTOtoTrashStatus.getStatuses().get(userToken2.getUserId()).contains(MessageStatus.TRASH));

        mailboxService.removeMessage(userToken2, messageDTOtoTrashStatus);
        assertFalse(mailboxService.getMessageDTOs(userToken2, EnumSet.of(MessageStatus.SENT)).stream()
                .anyMatch(messageDTO -> messageDTO.getMessageId().equals(messageDTOtoTrashStatus.getMessageId())));

        mailboxService.removeMessage(null,
                mailboxService.getMessageDTOs(userToken2, EnumSet.of(MessageStatus.SENT)).get(0));
        compareErrors("Invalid token", "function removeMessage", mailboxService.getErrorHandler());

        UserToken userTokenWithoutId = new UserToken(null, user.getGroupId(), user.getMailAccount());
        mailboxService.sendMessage(userTokenWithoutId, null, null, null, null);
        compareErrors("Invalid token", "function removeMessage", mailboxService.getErrorHandler());

        UserToken userTokenWithoutEmail = new UserToken(user.getUserId(), user.getGroupId(), null);
        mailboxService.sendMessage(userTokenWithoutEmail, null, null, null, null);
        compareErrors("Invalid token", "function removeMessage", mailboxService.getErrorHandler());

        mailboxService.removeMessage(userToken2, null);
        compareErrors("Invalid messageDTO", "function removeMessage", mailboxService.getErrorHandler());

        MessageDTO messageDTOWithoutId = mailboxService.getMessageDTOs(userToken2, EnumSet.of(MessageStatus.SENT))
                .get(0);
        messageDTOWithoutId.setMessageId(null);
        mailboxService.removeMessage(userToken2, messageDTOWithoutId);
        compareErrors("Invalid messageDTOid", "function removeMessage", mailboxService.getErrorHandler());

        assertEquals(1, mailboxService.getMessageDTOs(userToken2, EnumSet.of(MessageStatus.SENT)).size());

        mailboxService.sendMessage(userToken2, "test2@gmail.com", "Service sending second test",
                "Testing service sending twice", null);

        for (MessageDTO messageDTO : mailboxService.getMessageDTOs(userToken2, EnumSet.of(MessageStatus.SENT))) {
            mailboxService.removeMessage(userToken2, messageDTO);
        }

        assertEquals(0, mailboxService.getMessageDTOs(userToken2, EnumSet.of(MessageStatus.SENT)).size());
    }

    @Test
    @DisplayName("Should get message")
    public void testGetMessageDTOs() {
        MailboxService mailboxService = new MailboxService(EnvironmentType.TEST);
        User user = new User("1", "groupA", "alice@example.com", "hashedPassword1!", null);
        User user2 = new User("2", "groupA", "bob@example.com", "hashedPassword2!", null);

        UserToken userToken = new UserToken(user.getUserId(), user.getGroupId(), user.getMailAccount());
        UserToken userToken2 = new UserToken(user2.getUserId(), user2.getGroupId(), user2.getMailAccount());

        mailboxService.sendMessage(userToken, "bob@example.com", "Service sending test", "Testing service sending",
                null);
        mailboxService.sendMessage(userToken2, "alice@example.com", "Service sending test", "Testing service sending",
                null);

        mailboxService.getMessageDTOs(null, EnumSet.of(MessageStatus.INBOX));
        compareErrors("Invalid token", "function getMessageDTOs", mailboxService.getErrorHandler());

        UserToken userTokenWithoutId = new UserToken(null, user.getGroupId(), user.getMailAccount());
        mailboxService.getMessageDTOs(userTokenWithoutId, null);
        compareErrors("Invalid token", "function getMessageDTOs", mailboxService.getErrorHandler());

        UserToken userTokenWithoutEmail = new UserToken(user.getUserId(), user.getGroupId(), null);
        mailboxService.getMessageDTOs(userTokenWithoutEmail, null);
        compareErrors("Invalid token", "function getMessageDTOs", mailboxService.getErrorHandler());

        mailboxService.getMessageDTOs(userToken, null);
        compareErrors("Invalid statuses", "function getMessageDTOs", mailboxService.getErrorHandler());

        assertFalse(mailboxService.getMessageDTOs(userToken, EnumSet.of(MessageStatus.INBOX)).isEmpty());
        assertFalse(mailboxService.getMessageDTOs(userToken2, EnumSet.of(MessageStatus.INBOX)).isEmpty());

        assertTrue(mailboxService.getMessageDTOs(userToken, EnumSet.of(MessageStatus.STARRED)).isEmpty());
        assertTrue(mailboxService.getMessageDTOs(userToken2, EnumSet.of(MessageStatus.TRASH)).isEmpty());
    }
}
