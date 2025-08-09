package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
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
import com.example.utils.ErrorManager;
import com.example.utils.enums.MessageStatus;
import com.example.utils.interfaces.ErrorHandler;
import com.example.utils.services.ValidationService;
import com.example.utils.services.ValidationService.MessageValidations;

@ExtendWith(MockitoExtension.class)
public class MessageValtidationsTest {
    private ErrorHandler errorHandler;
    private ValidationService validationService;
    private MessageValidations validator;

    private void compareErrors(String expectedMessage, String key, ErrorHandler errorHandler) {
        String errorMessage = errorHandler.getError(key);
        if (errorMessage != null) {
            assertEquals(expectedMessage, errorMessage, "Mismatch in error message for key " + key);
            errorHandler.removeError(key);
        }
    }

    private Map<String, EnumSet<MessageStatus>> getMesssageStatus(String senderId, String recevierId) {
        Map<String, EnumSet<MessageStatus>> messageStatus = new HashMap<>();
        messageStatus.put(senderId, EnumSet.of(MessageStatus.SENT));
        messageStatus.put(recevierId, EnumSet.of(MessageStatus.INBOX));

        return messageStatus;
    }

    @BeforeEach
    void setup() {
        this.errorHandler = new ErrorManager(new HashMap<>());
        this.validationService = new ValidationService();
        this.validator = validationService.new MessageValidations(errorHandler);
    }

    @Test
    @DisplayName("Should detect invalid message format (whom email, subject, message) and accept valid ones")
    void testMesssageData() {
        List<MessageDTO> messageDTOs = List.of(
                new MessageDTO(null, "ABCD", null, null, null, null, null, LocalDateTime.now(), null, null, null),
                new MessageDTO(null, "ABCD1", null, "BCD5", "strat@gmail.com",
                        "Exploring the Impact of Artificial Intelligence on Modern Education Systems", null,
                        LocalDateTime.now(), null, getMesssageStatus("ABCD1", "BCD5"), null),
                new MessageDTO(null, "ABCD2", null, "BCD4", "splet@gmail.com",
                        "Exploring the Impact of Artificial Intelligence ",
                        "Artificial Intelligence (AI) is no longer a concept confined to science fiction or academic research. "
                                + "It has evolved into a transformative force that is actively reshaping the fabric of global industries. "
                                + "From healthcare to manufacturing, finance to entertainment, and education to logistics, AI's influence "
                                + "continues to expand, revolutionizing processes, improving efficiencies, and redefining the boundaries "
                                + "of what is possible. This essay explores the multifaceted impact of AI across several key industries, "
                                + "shedding light on both the opportunities and challenges that accompany this technological revolution.\n\n"
                                + "In the healthcare sector, AI has introduced groundbreaking innovations in diagnostics, patient care, "
                                + "and medical research. Machine learning algorithms can analyze vast datasets of medical records, imaging "
                                + "scans, and genetic information to identify patterns that might elude even the most experienced professionals. "
                                + "AI-powered diagnostic tools can detect diseases such as cancer, diabetes, and cardiovascular conditions at "
                                + "early stages, improving treatment outcomes and saving lives. Virtual health assistants and AI-driven chatbots "
                                + "have also enhanced patient engagement, offering timely medical advice and support while reducing the burden "
                                + "on healthcare providers. Furthermore, AI is playing a crucial role in drug discovery and development, "
                                + "accelerating the process of identifying potential compounds and predicting their efficacy.\n\n"
                                + "...", // trimmed for brevity
                        LocalDateTime.now(), null, getMesssageStatus("ABCD2", "BCD4"), null),
                new MessageDTO(null, "ABCD3", null, "BCD3", "rotr@gamil.com",
                        "Exploring the Impact of Artificial Intelligence ",
                        "Artificial Intelligence (AI) is rapidly transforming industries by automating processes, enhancing decision-making, "
                                + "and uncovering insights from massive data sets. From healthcare and education to finance and manufacturing, "
                                + "AI continues to revolutionize how we live and work. Its responsible integration promises innovation, "
                                + "efficiency, and new opportunities across every sector of society.",
                        LocalDateTime.now(), null, getMesssageStatus("ABCD3", "BCD3"), null));

        for (int i = 0; i < messageDTOs.size(); i++) {
            MessageDTO messageDTO = messageDTOs.get(i);
            boolean isValid = validator.validMessageData(messageDTO.getRecevierMailAccount(), messageDTO.getSubject(),
                    messageDTO.getMessage());
            if (i == 0) {
                assertFalse(isValid);
                compareErrors("Please enter a valid email address (e.g., user@example.com).", "email", errorHandler);
            }
            if (i == 1) {
                assertFalse(isValid);
                compareErrors("Subject is too long.", "subject", errorHandler);
            }
            if (i == 2) {
                assertFalse(isValid);
                compareErrors("Message is too long.", "message", errorHandler);
            }
            if (i == 3) {
                assertTrue(isValid);
            }
        }
    }

    @Test
    @DisplayName("Should allow only attach limit count of supported files")
    void testAttachedFiles() {
        boolean withEmptyFileArr = validator.validFiles(null);
        assertTrue(withEmptyFileArr);

        boolean withBiggerFileArr = validator.validFiles(
                List.of(new File("/icons/icons8-default-file.png"), new File("/icons/icons8-default-file.png"),
                        new File("/icons/icons8-default-file.png"), new File("/icons/icons8-default-file.png"),
                        new File("/icons/icons8-default-file.png"), new File("/icons/icons8-default-file.png")));
        assertFalse(withBiggerFileArr);
        compareErrors("Too much attached files in one message (max. 5).", "file", errorHandler);

        List<File> files = new ArrayList<>();
        files.add(new File("/icons/icons8-default-file.png"));
        files.add(null);

        boolean withUnsupportedFileArr = validator.validFiles(files);
        assertFalse(withUnsupportedFileArr);
        compareErrors("We couldn't process your second file. Make sure it's uploaded and in a supported format.",
                "file", errorHandler);

    }

    @Test
    @DisplayName("Should check if contains only allowed statuses")
    public void testOnlyAllowedStatuses() {
        MessageDTO messageDTO = new MessageDTO(null, "ABCD1", null, "BCD2", "strat@gmail.com",
                "Exploring the Impact of Artificial Intelligence on Modern Education Systems", null,
                LocalDateTime.now(), null, getMesssageStatus("ABCD1", "BCD2"), null);

        assertTrue(validator.containsOnlyAllowedStatuses(messageDTO.getStatuses(),
                EnumSet.of(MessageStatus.INBOX, MessageStatus.SENT)));
    }

    @Test
    @DisplayName("Should check if update with provided status is allowed")
    public void testStatusUpdateAllowed() {
        MessageDTO messageDTO = new MessageDTO(null, "ABCD1", null, "BCD5", "strat@gmail.com",
                "Exploring the Impact of Artificial Intelligence on Modern Education Systems", null,
                LocalDateTime.now(), null, getMesssageStatus("ABCD1", "BCD5"), null);

        assertFalse(validator.isStatusUpdateAllowed(null, null));
        compareErrors("Current message statuses required.", "statuses", errorHandler);
        compareErrors("New status required.", "status", errorHandler);

        assertFalse(validator.isStatusUpdateAllowed(messageDTO.getStatuses().get("ABCD1"), null));
        compareErrors("New status required.", "status", errorHandler);

        assertFalse(validator.isStatusUpdateAllowed(null, MessageStatus.INBOX));
        compareErrors("Current message statuses required.", "statuses", errorHandler);

        assertFalse(validator.isStatusUpdateAllowed(messageDTO.getStatuses().get("ABCD1"), MessageStatus.INBOX));
        assertFalse(validator.isStatusUpdateAllowed(messageDTO.getStatuses().get("BCD5"), MessageStatus.SENT));

        assertTrue(validator.isStatusUpdateAllowed(messageDTO.getStatuses().get("ABCD1"), MessageStatus.STARRED));
        assertTrue(validator.isStatusUpdateAllowed(messageDTO.getStatuses().get("BCD5"), MessageStatus.STARRED));

        assertTrue(validator.isStatusUpdateAllowed(messageDTO.getStatuses().get("ABCD1"), MessageStatus.TRASH));
        assertTrue(validator.isStatusUpdateAllowed(messageDTO.getStatuses().get("BCD5"), MessageStatus.TRASH));
    }
}
