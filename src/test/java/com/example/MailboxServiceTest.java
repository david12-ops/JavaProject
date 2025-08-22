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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;

@ExtendWith(MockitoExtension.class)
public class MailboxServiceTest {
        private MailboxService mailService;

        private void compareErrors(String expectedMessage, String key, ErrorHandler errorHandler) {
                String errorMessage = errorHandler.getError(key);
                assertEquals(expectedMessage, errorMessage, "Mismatch in error message for key " + key);
        }

        @BeforeEach
        void setup() {
                this.mailService = new MailboxService(EnvironmentType.TEST);
        }

        @Test
        @DisplayName("Should send message")
        public void testSendMessage() {
                mailService.clearTestMessagesData();
                User user = new User("1", "groupA", "alice@example.com",
                                BCrypt.hashpw("hashedPassword1!", BCrypt.gensalt()), null);
                User user2 = new User("2", "groupA", "bob@example.com",
                                BCrypt.hashpw("hashedPassword2!", BCrypt.gensalt()), null);

                UserToken userToken = new UserToken(user.getUserId(), user.getGroupId(), user.getMailAccount());
                UserToken userToken2 = new UserToken(user2.getUserId(), user2.getGroupId(), user2.getMailAccount());

                mailService.sendMessage(userToken, "bob@example.com", "Service sending test", "Testing service sending",
                                null);
                assertEquals(1, mailService.getMessageDTOs(userToken, EnumSet.of(MessageStatus.SENT)).size());

                mailService.sendMessage(userToken2, "alice@example.com", "Service sending test",
                                "Testing service sending", null);
                mailService.sendMessage(userToken2, "nonexisting@example.com", "Service sending test",
                                "Testing service sending", null);
                assertEquals(1, mailService.getMessageDTOs(userToken, EnumSet.of(MessageStatus.SENT)).size());

                mailService.sendMessage(null, null, null, null, null);
                compareErrors("Invalid token argument in sendMessage function.", "token",
                                mailService.getErrorHandler());

                UserToken userTokenWithoutId = new UserToken(null, user.getGroupId(), user.getMailAccount());
                mailService.sendMessage(userTokenWithoutId, null, null, null, null);
                compareErrors("Invalid token argument in sendMessage function.", "token",
                                mailService.getErrorHandler());

                UserToken userTokenWithoutEmail = new UserToken(user.getUserId(), user.getGroupId(), null);
                mailService.sendMessage(userTokenWithoutEmail, null, null, null, null);
                compareErrors("Invalid token argument in sendMessage function.", "token",
                                mailService.getErrorHandler());

                mailService.sendMessage(userToken, null, null, null, null);
                compareErrors("Invalid recevierId argument in sendMessage function.", "recevierId",
                                mailService.getErrorHandler());
        }

        @Test
        @DisplayName("Should update status of message")
        public void testUpdateStatus() {
                mailService.clearTestMessagesData();
                User user = new User("1", "groupA", "alice@example.com",
                                BCrypt.hashpw("hashedPassword1!", BCrypt.gensalt()), null);
                User user2 = new User("2", "groupA", "bob@example.com",
                                BCrypt.hashpw("hashedPassword2!", BCrypt.gensalt()), null);

                UserToken userToken = new UserToken(user.getUserId(), user.getGroupId(), user.getMailAccount());
                UserToken userToken2 = new UserToken(user2.getUserId(), user2.getGroupId(), user2.getMailAccount());

                mailService.sendMessage(userToken, "bob@example.com", "Service sending test", "Testing service sending",
                                null);
                mailService.sendMessage(userToken2, "alice@example.com", "Service sending test",
                                "Testing service sending", null);

                mailService.updateStatus(null, null, null, null);
                compareErrors("Invalid token argument in updateStatus function.", "token",
                                mailService.getErrorHandler());

                UserToken userTokenWithoutId = new UserToken(null, user.getGroupId(), user.getMailAccount());
                mailService.updateStatus(userTokenWithoutId, null, null, null);
                compareErrors("Invalid token argument in updateStatus function.", "token",
                                mailService.getErrorHandler());

                UserToken userTokenWithoutEmail = new UserToken(user.getUserId(), user.getGroupId(), null);
                mailService.updateStatus(userTokenWithoutEmail, null, null, null);
                compareErrors("Invalid token argument in updateStatus function.", "token",
                                mailService.getErrorHandler());

                mailService.updateStatus(userToken,
                                mailService.getMessageDTOs(userToken, EnumSet.of(MessageStatus.INBOX)).get(0),
                                MessageStatus.STARRED, null);
                compareErrors("Provided unsupported type of operation.", "operationType",
                                mailService.getErrorHandler());

                mailService.updateStatus(userToken,
                                mailService.getMessageDTOs(userToken, EnumSet.of(MessageStatus.INBOX)).get(0),
                                MessageStatus.STARRED, OperationType.CREATE);
                compareErrors("Provided unsupported type of operation.", "operationType",
                                mailService.getErrorHandler());

                mailService.updateStatus(userToken, null, null, null);
                compareErrors("Invalid messageDTO argument in updateStatus function.", "messageDTO",
                                mailService.getErrorHandler());

                mailService.updateStatus(userToken,
                                mailService.getMessageDTOs(userToken, EnumSet.of(MessageStatus.INBOX)).get(0), null,
                                null);
                compareErrors("Invalid status argument in updateStatus function.", "status",
                                mailService.getErrorHandler());

                MessageDTO messageDTOWithoutId = mailService.getMessageDTOs(userToken, EnumSet.of(MessageStatus.SENT))
                                .get(0);
                messageDTOWithoutId.setMessageId(null);
                mailService.updateStatus(userToken, messageDTOWithoutId, MessageStatus.STARRED, OperationType.UPDATE);
                compareErrors("Invalid messageDTOid argument in updateStatus function.", "messageDTOid",
                                mailService.getErrorHandler());

                MessageDTO messageDTO = mailService.getMessageDTOs(userToken, EnumSet.of(MessageStatus.INBOX)).get(0);

                mailService.updateStatus(userToken, messageDTO, MessageStatus.SENT, OperationType.UPDATE);
                compareErrors("Unsupported update with new status.", "status", mailService.getErrorHandler());

                mailService.updateStatus(userToken, messageDTO, MessageStatus.STARRED, OperationType.UPDATE);
                assertTrue(messageDTO.getStatuses().get(userToken.getUserId()).contains(MessageStatus.STARRED));

                mailService.updateStatus(userToken, messageDTO, MessageStatus.STARRED, OperationType.REMOVE);
                assertFalse(messageDTO.getStatuses().get(userToken.getUserId()).contains(MessageStatus.STARRED));

                mailService.updateStatus(userToken, messageDTO, MessageStatus.TRASH, OperationType.UPDATE);
                assertTrue(messageDTO.getStatuses().get(userToken.getUserId()).contains(MessageStatus.TRASH));
                mailService.updateStatus(userToken, messageDTO, MessageStatus.TRASH, OperationType.REMOVE);
                assertFalse(messageDTO.getStatuses().get(userToken.getUserId()).contains(MessageStatus.TRASH));
        }

        @Test
        @DisplayName("Should remove message")
        public void testRemoveMessage() {
                mailService.clearTestMessagesData();
                User user = new User("1", "groupA", "alice@example.com",
                                BCrypt.hashpw("hashedPassword1!", BCrypt.gensalt()), null);
                User user2 = new User("2", "groupA", "bob@example.com",
                                BCrypt.hashpw("hashedPassword2!", BCrypt.gensalt()), null);

                UserToken userToken = new UserToken(user.getUserId(), user.getGroupId(), user.getMailAccount());
                UserToken userToken2 = new UserToken(user2.getUserId(), user2.getGroupId(), user2.getMailAccount());

                mailService.sendMessage(userToken, "bob@example.com", "Service sending test", "Testing service sending",
                                null);
                mailService.sendMessage(userToken2, "alice@example.com", "Service sending test",
                                "Testing service sending", null);
                mailService.sendMessage(userToken2, "alice@example.com", "Service sending test twice",
                                "Testing service sending twice", null);

                MessageDTO messageDTOtoTrashStatus = mailService
                                .getMessageDTOs(userToken2, EnumSet.of(MessageStatus.SENT)).get(0);
                mailService.removeMessage(userToken2, messageDTOtoTrashStatus);
                assertTrue(messageDTOtoTrashStatus.getStatuses().get(userToken2.getUserId())
                                .contains(MessageStatus.TRASH));

                mailService.removeMessage(userToken2, messageDTOtoTrashStatus);
                assertFalse(mailService.getMessageDTOs(userToken2, EnumSet.of(MessageStatus.SENT)).stream()
                                .anyMatch(messageDTO -> messageDTO.getMessageId()
                                                .equals(messageDTOtoTrashStatus.getMessageId())));

                mailService.removeMessage(null,
                                mailService.getMessageDTOs(userToken2, EnumSet.of(MessageStatus.SENT)).get(0));
                compareErrors("Invalid token argument in removeMessage function.", "token",
                                mailService.getErrorHandler());

                UserToken userTokenWithoutId = new UserToken(null, user.getGroupId(), user.getMailAccount());
                mailService.removeMessage(userTokenWithoutId,
                                mailService.getMessageDTOs(userToken2, EnumSet.of(MessageStatus.SENT)).get(0));
                compareErrors("Invalid token argument in removeMessage function.", "token",
                                mailService.getErrorHandler());

                UserToken userTokenWithoutEmail = new UserToken(user.getUserId(), user.getGroupId(), null);
                mailService.removeMessage(userTokenWithoutEmail,
                                mailService.getMessageDTOs(userToken2, EnumSet.of(MessageStatus.SENT)).get(0));
                compareErrors("Invalid token argument in removeMessage function.", "token",
                                mailService.getErrorHandler());

                mailService.removeMessage(userToken2, null);
                compareErrors("Invalid messageDTO argument in removeMessage function.", "messageDTO",
                                mailService.getErrorHandler());

                MessageDTO messageDTOWithoutId = mailService.getMessageDTOs(userToken2, EnumSet.of(MessageStatus.SENT))
                                .get(0);
                messageDTOWithoutId.setMessageId(null);
                mailService.removeMessage(userToken2, messageDTOWithoutId);
                compareErrors("Invalid messageDTOid argument in removeMessage function.", "messageDTOid",
                                mailService.getErrorHandler());

                assertEquals(1, mailService.getMessageDTOs(userToken2, EnumSet.of(MessageStatus.SENT)).size());

                mailService.sendMessage(userToken2, "test2@gmail.com", "Service sending second test",
                                "Testing service sending twice", null);

                for (MessageDTO messageDTO : mailService.getMessageDTOs(userToken2, EnumSet.of(MessageStatus.SENT))) {
                        mailService.removeMessage(userToken2, messageDTO);
                }

                assertEquals(0, mailService.getMessageDTOs(userToken2, EnumSet.of(MessageStatus.SENT)).size());
                assertEquals(2, mailService.getMessageDTOs(userToken, EnumSet.of(MessageStatus.INBOX)).size());

                for (MessageDTO messageDTO : mailService.getMessageDTOs(userToken2, EnumSet.of(MessageStatus.TRASH))) {
                        assertTrue(messageDTO.getStatuses().containsKey(userToken2.getUserId()));
                        mailService.removeMessage(userToken2, messageDTO);
                }

                for (MessageDTO messageDTO : mailService.getMessageDTOs(userToken, EnumSet.of(MessageStatus.INBOX))) {
                        assertFalse(messageDTO.getStatuses().containsKey(userToken2.getUserId()));
                        assertTrue(messageDTO.getStatuses().containsKey(userToken.getUserId()));
                        assertTrue(messageDTO.getStatuses().get(userToken.getUserId()).contains(MessageStatus.INBOX));
                }
        }

        @Test
        @DisplayName("Should get message")
        public void testGetMessageDTOs() {
                mailService.clearTestMessagesData();
                User user = new User("1", "groupA", "alice@example.com",
                                BCrypt.hashpw("hashedPassword1!", BCrypt.gensalt()), null);
                User user2 = new User("2", "groupA", "bob@example.com",
                                BCrypt.hashpw("hashedPassword2!", BCrypt.gensalt()), null);

                UserToken userToken = new UserToken(user.getUserId(), user.getGroupId(), user.getMailAccount());
                UserToken userToken2 = new UserToken(user2.getUserId(), user2.getGroupId(), user2.getMailAccount());

                mailService.sendMessage(userToken, "bob@example.com", "Service sending test", "Testing service sending",
                                null);
                mailService.sendMessage(userToken2, "alice@example.com", "Service sending test",
                                "Testing service sending", null);

                mailService.getMessageDTOs(null, EnumSet.of(MessageStatus.INBOX));
                compareErrors("Invalid token argument in getMessageDTOs function.", "token",
                                mailService.getErrorHandler());

                UserToken userTokenWithoutId = new UserToken(null, user.getGroupId(), user.getMailAccount());
                mailService.getMessageDTOs(userTokenWithoutId, null);
                compareErrors("Invalid token argument in getMessageDTOs function.", "token",
                                mailService.getErrorHandler());

                UserToken userTokenWithoutEmail = new UserToken(user.getUserId(), user.getGroupId(), null);
                mailService.getMessageDTOs(userTokenWithoutEmail, null);
                compareErrors("Invalid token argument in getMessageDTOs function.", "token",
                                mailService.getErrorHandler());

                mailService.getMessageDTOs(userToken, null);
                compareErrors("Invalid statuses argument in getMessageDTOs function.", "statuses",
                                mailService.getErrorHandler());

                assertFalse(mailService.getMessageDTOs(userToken, EnumSet.of(MessageStatus.INBOX)).isEmpty());
                assertFalse(mailService.getMessageDTOs(userToken2, EnumSet.of(MessageStatus.INBOX)).isEmpty());

                assertTrue(mailService.getMessageDTOs(userToken, EnumSet.of(MessageStatus.STARRED)).isEmpty());
                assertTrue(mailService.getMessageDTOs(userToken2, EnumSet.of(MessageStatus.TRASH)).isEmpty());
        }
}
