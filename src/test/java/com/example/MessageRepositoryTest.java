package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.dto.MessageDTO;
import com.example.model.Message;
import com.example.model.repository.MessageRepository;
import com.example.utils.enums.EnvironmentType;
import com.example.utils.enums.MessageStatus;

@ExtendWith(MockitoExtension.class)
public class MessageRepositoryTest {
    private MessageRepository messageRepository;

    @BeforeEach
    void setup() {
        this.messageRepository = new MessageRepository(EnvironmentType.TEST);
        prepareData();
    }

    private List<Message> createMessages() {
        Map<String, EnumSet<MessageStatus>> statuses1 = new HashMap<>();
        statuses1.put("receiver1", EnumSet.of(MessageStatus.SENT, MessageStatus.SENT));

        Map<String, EnumSet<MessageStatus>> statuses2 = new HashMap<>();
        statuses2.put("receiver2", EnumSet.of(MessageStatus.SENT));

        Map<String, EnumSet<MessageStatus>> statuses3 = new HashMap<>();
        statuses3.put("receiver3", EnumSet.of(MessageStatus.SENT, MessageStatus.STARRED));

        Map<String, EnumSet<MessageStatus>> statuses4 = new HashMap<>();
        statuses4.put("receiver4", EnumSet.of(MessageStatus.SENT, MessageStatus.STARRED, MessageStatus.INBOX));

        Map<String, EnumSet<MessageStatus>> statuses5 = new HashMap<>();
        statuses5.put("receiver5", EnumSet.of(MessageStatus.SENT));

        Message m1 = new Message("msg-001", "user-101", "receiver1", "Hello!", "Just wanted to say hi.",
                LocalDateTime.now().minusDays(1), List.of("file1_base64_string"), statuses1);

        Message m2 = new Message("msg-002", "user-102", "receiver2", "Reminder",
                "Don't forget about our meeting tomorrow.", LocalDateTime.now().minusHours(5), List.of(), statuses2);

        Message m3 = new Message("msg-003", "user-103", "receiver3", "Follow-up",
                "Did you have a chance to review the document?", LocalDateTime.now().minusMinutes(30),
                List.of("doc_base64_string", "image_base64_string"), statuses3);

        Message m4 = new Message("msg-004", "user-104", "receiver4", "Project Update", "The new version is now live.",
                LocalDateTime.now().minusWeeks(1), List.of(), statuses4);

        Message m5 = new Message("msg-005", "user-105", "receiver5", "Welcome", "Thanks for joining our platform!",
                LocalDateTime.now(), List.of("welcome_pdf_base64_string"), statuses5);

        List<Message> messages = new ArrayList<>();
        messages.add(m1);
        messages.add(m2);
        messages.add(m3);
        messages.add(m4);
        messages.add(m5);

        return messages;
    }

    private void prepareData() {
        messageRepository.setTestData(createMessages());
        List<MessageDTO> messageDTOs = new ArrayList<>(messageRepository.getAllMessageDtos());

        List<Message> data = new ArrayList<>();

        messageDTOs.forEach(messageDTO -> data.add(new Message(messageDTO.getMessageId(), messageDTO.getSenderId(),
                messageDTO.getRecevierId(), messageDTO.getSubject(), messageDTO.getMessage(), messageDTO.getTimestamp(),
                messageDTO.getAttachedBase64Files(), messageDTO.getStatuses())));

        messageRepository.setTestData(data);
    }

    private MessageDTO getMessageDTObyID(String messageId, List<MessageDTO> messageDTOs) {
        for (MessageDTO messageDTO : messageDTOs) {
            if (messageDTO.getMessageId().equals(messageId)) {
                return messageDTO;
            }
        }
        return null;
    }

    @Test
    @DisplayName("Should remove all messages")
    void testRemoveMessage() {
        for (MessageDTO messageDTO : messageRepository.getAllMessageDtos()) {
            messageRepository.removeMessage(messageDTO);
        }

        assertEquals(0, messageRepository.getAllMessageDtos().size());
    }

    @Test
    @DisplayName("Should add message")
    void testAddMessage() {
        LocalDateTime localDateTime = LocalDateTime.now().minusHours(5);
        messageRepository.addMessage(
                new MessageDTO(null, "user-106", "user-106@gmail.com", "receiver6", "receiver6@gmail.com", "Reminder",
                        "Don't forget about our meeting tomorrow.", localDateTime, List.of(), null, List.of()));

        assertTrue(messageRepository.getAllMessageDtos().stream()
                .anyMatch(messageDTO -> messageDTO.getSubject().equals("Reminder")
                        && messageDTO.getMessage().equals("Don't forget about our meeting tomorrow.")
                        && messageDTO.getTimestamp().equals(localDateTime)));

        messageRepository.addMessage(new MessageDTO(null, "user-107", "user-107@gmail.com", "receiver7",
                "receiver7@gmail.com", "Reminder", "Don't forget about our meeting today.",
                LocalDateTime.now().minusHours(5), List.of(), null, List.of()));

        assertTrue(messageRepository.getAllMessageDtos().size() == 7);
    }

    @Test
    @DisplayName("Should update message status")
    void testUpdateMessageStatus() {
        MessageDTO foundMessageDTO = getMessageDTObyID("msg-002", messageRepository.getAllMessageDtos());
        Map<String, EnumSet<MessageStatus>> foundMessageDTOStatuses = foundMessageDTO.getStatuses();
        Map<String, EnumSet<MessageStatus>> updatedMessageDTOStatuses = foundMessageDTO.getStatuses();
        assertNotNull(foundMessageDTO);

        foundMessageDTOStatuses.get("receiver2").add(MessageStatus.STARRED);
        updatedMessageDTOStatuses.put("receiver2", foundMessageDTOStatuses.get("receiver2"));

        MessageDTO updatedMessageDTO = new MessageDTO(foundMessageDTO.getMessageId(), foundMessageDTO.getSenderId(),
                foundMessageDTO.getSenderMailAccount(), foundMessageDTO.getRecevierId(),
                foundMessageDTO.getRecevierMailAccount(), foundMessageDTO.getSubject(), foundMessageDTO.getMessage(),
                foundMessageDTO.getTimestamp(), foundMessageDTO.getAttachedBase64Files(), updatedMessageDTOStatuses,
                foundMessageDTO.getAttachedFiles());

        messageRepository.updateMessageStatus(foundMessageDTO, updatedMessageDTO);
        assertTrue(updatedMessageDTO.getStatuses().get("receiver2").contains(MessageStatus.STARRED));

        updatedMessageDTOStatuses.get("receiver2").remove(MessageStatus.STARRED);
        updatedMessageDTO.setStatuses("receiver2", updatedMessageDTOStatuses.get("receiver2"));

        assertFalse(updatedMessageDTO.getStatuses().get("receiver2").contains(MessageStatus.STARRED));
    }
}
