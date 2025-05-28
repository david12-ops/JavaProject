package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.model.Message;
import com.example.utils.ErrorToolManager;
import com.example.utils.enums.MessageStatus;
import com.example.utils.services.ValidationService;
import com.example.utils.services.ValidationService.MessageModelValidations;

@ExtendWith(MockitoExtension.class)
public class MessageValtidationsTest {
    private ErrorToolManager errorToolManager;
    private ValidationService validationService;
    private MessageModelValidations validator;

    private void compareErrors(String expectedMessage, String key, ErrorToolManager errorToolManager) {
        String errorMessage = errorToolManager.getError(key);
        if (errorMessage != null) {
            assertEquals(expectedMessage, errorMessage, "Mismatch in error message for key " + key);
            errorToolManager.removeError(key);
        }

    }

    private Map<String, Set<MessageStatus>> getMesssageStatus(String senderId, String recevierEmail) {
        Map<String, Set<MessageStatus>> messageStatus = new HashMap<>();
        messageStatus.put(senderId, Set.of(MessageStatus.SENT));
        messageStatus.put(recevierEmail, Set.of(MessageStatus.INBOX));

        return messageStatus;
    }

    @BeforeEach
    void setup() {
        this.errorToolManager = new ErrorToolManager(new HashMap<>());
        this.validationService = new ValidationService();
        this.validator = validationService.new MessageModelValidations(errorToolManager);
    }

    @Test
    @DisplayName("Should detect invalid message format (whom email, subject, message) and accept valid ones")
    void testMesssageData() {

        List<Message> messsages = List.of(new Message(null, "ABCD", null, null, null, LocalDateTime.now(), null, null),
                new Message(null, "ABCD1", "strat@gmail.com",
                        "Exploring the Impact of Artificial Intelligence on Modern Education Systems", null,
                        LocalDateTime.now(), null, getMesssageStatus("ABCD1", "strat@gmail.com")),
                new Message(null, "ABCD2", "splet@gmail.com", "Exploring the Impact of Artificial Intelligence ",
                        "Artificial Intelligence (AI) is no longer a concept confined to science fiction or academic research. It has evolved into a transformative force that is actively reshaping the fabric of global industries. From healthcare to manufacturing, finance to entertainment, and education to logistics, AI's influence continues to expand, revolutionizing processes, improving efficiencies, and redefining the boundaries of what is possible. This essay explores the multifaceted impact of AI across several key industries, shedding light on both the opportunities and challenges that accompany this technological revolution.\n"
                                + //
                                "\n" + //
                                "In the healthcare sector, AI has introduced groundbreaking innovations in diagnostics, patient care, and medical research. Machine learning algorithms can analyze vast datasets of medical records, imaging scans, and genetic information to identify patterns that might elude even the most experienced professionals. AI-powered diagnostic tools can detect diseases such as cancer, diabetes, and cardiovascular conditions at early stages, improving treatment outcomes and saving lives. Virtual health assistants and AI-driven chatbots have also enhanced patient engagement, offering timely medical advice and support while reducing the burden on healthcare providers. Furthermore, AI is playing a crucial role in drug discovery and development, accelerating the process of identifying potential compounds and predicting their efficacy.\n"
                                + //
                                "\n" + //
                                "In the realm of finance, AI has revolutionized the way institutions operate and interact with customers. Predictive analytics and algorithmic trading have enabled firms to make faster, data-driven decisions, minimizing risk and maximizing returns. Fraud detection systems powered by AI can identify suspicious transactions in real time, protecting consumers and institutions from financial crime. Chatbots and virtual assistants have improved customer service by providing instant responses to inquiries and facilitating routine transactions. Additionally, robo-advisors are making investment services more accessible to a broader audience by offering personalized portfolio recommendations based on individual financial goals and risk tolerance.\n"
                                + //
                                "\n" + //
                                "The manufacturing industry has embraced AI to streamline operations, enhance productivity, and reduce costs. Smart factories equipped with AI-driven automation systems can monitor equipment performance, predict maintenance needs, and optimize production schedules. Collaborative robots, or cobots, work alongside human workers, performing repetitive or hazardous tasks with precision and efficiency. AI is also being used to design products more effectively, simulate manufacturing processes, and manage supply chains with greater agility. These advancements contribute to a more sustainable and competitive manufacturing ecosystem.\n"
                                + //
                                "\n" + //
                                "In the field of logistics and transportation, AI is enabling more efficient and reliable movement of goods and people. Self-driving vehicles, while still in the development stage, promise to revolutionize personal and commercial transportation by reducing accidents and improving fuel efficiency. AI-powered route optimization tools help logistics companies plan deliveries more effectively, minimize fuel consumption, and enhance customer satisfaction. In warehouses, AI systems manage inventory, track shipments, and coordinate the movement of goods with remarkable accuracy, reducing human error and increasing throughput.\n"
                                + //
                                "\n" + //
                                "Education is another domain where AI is making a significant impact. Personalized learning platforms use AI algorithms to adapt educational content to the individual needs of students, enhancing engagement and improving learning outcomes. Intelligent tutoring systems provide real-time feedback and guidance, allowing learners to progress at their own pace. AI is also being used to automate administrative tasks such as grading and scheduling, freeing educators to focus on teaching and mentoring. Moreover, AI-powered analytics help educators identify students at risk of falling behind and implement timely interventions to support their success.\n"
                                + //
                                "\n" + //
                                "Entertainment and media industries have been profoundly influenced by AI as well. Recommendation engines on streaming platforms like Netflix and Spotify use AI to analyze user behavior and preferences, curating personalized content that keeps audiences engaged. AI-generated music, art, and even writing are pushing the boundaries of creativity, challenging traditional notions of authorship and originality. In filmmaking and video game development, AI tools assist in scriptwriting, animation, and voice synthesis, enabling creators to bring their visions to life more efficiently and effectively.\n"
                                + //
                                "\n" + //
                                "Despite the numerous benefits of AI, its widespread adoption raises important ethical, social, and economic considerations. Issues such as data privacy, algorithmic bias, and job displacement require careful attention and proactive policy measures. Ensuring that AI technologies are developed and deployed responsibly is crucial to maximizing their positive impact while minimizing harm. Transparent governance, inclusive dialogue, and interdisciplinary collaboration will be essential in navigating these complex challenges.\n"
                                + //
                                "\n" + //
                                "In conclusion, Artificial Intelligence is not just a tool for automation or efficiency; it is a catalyst for transformation across every major sector of the global economy. Its potential to drive innovation, solve complex problems, and improve human lives is immense. However, realizing this potential will require a balanced approach that embraces technological advancement while safeguarding ethical values and human dignity. The journey toward an AI-powered future is already underway, and how we choose to shape that future will determine the kind of world we create for generations to come.",
                        LocalDateTime.now(), null, getMesssageStatus("ABCD2", "splet@gmail.com")),
                new Message(null, "ABCD3", "rotr@gamil.com", "Exploring the Impact of Artificial Intelligence ",
                        "Artificial Intelligence (AI) is rapidly transforming industries by automating processes, enhancing decision-making, and uncovering insights from massive data sets. From healthcare and education to finance and manufacturing, AI continues to revolutionize how we live and work. Its responsible integration promises innovation, efficiency, and new opportunities across every sector of society.",
                        LocalDateTime.now(), null, getMesssageStatus("ABCD3", "rotr@gamil.com")));

        for (int i = 0; i < messsages.size(); i++) {
            Message mess = messsages.get(i);
            boolean valid = validator.validMessageData(mess.getReceiver(), mess.getSubject(), mess.getMessage());
            if (i == 0) {
                assertFalse(valid);
                compareErrors("Please enter a valid email address (e.g., user@example.com)", "email", errorToolManager);
            }
            if (i == 1) {
                assertFalse(valid);
                compareErrors("Subject is too long", "subject", errorToolManager);
            }
            if (i == 2) {
                assertFalse(valid);
                compareErrors("Message is too long", "message", errorToolManager);
            }
            if (i == 3) {
                assertTrue(valid);
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
        compareErrors("Too much attached files in one message (max. 5)", "file", errorToolManager);

        List<File> files = new ArrayList<>();
        files.add(new File("/icons/icons8-default-file.png"));
        files.add(null);

        boolean withUnsupportedFileArr = validator.validFiles(files);
        assertFalse(withUnsupportedFileArr);
        compareErrors("We couldn't process your second file. Make sure it's uploaded and in a supported format", "file",
                errorToolManager);

    }

}
