package com.example.utils.services;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.mindrot.jbcrypt.BCrypt;

import com.example.dto.UserDTO;
import com.example.utils.interfaces.ErrorHandler;
import com.example.utils.enums.FormType;
import com.example.utils.enums.MessageStatus;
import com.example.utils.enums.OperationType;
import com.example.utils.interfaces.MessageValidator;
import com.example.utils.interfaces.UserValidator;

public class ValidationService {
    private static final Pattern EMAIL_REGEX = Pattern
            .compile("^(?=.{1,254}$)(?=.{1,64}@)(?!.*\\.\\.)(?!.*\\.$)[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final long MAX_FILE_SIZE = 25L * 1024 * 1024;
    private static final Pattern PASSWORD_REGEX = Pattern
            .compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&.])[A-Za-z\\d@$!%*?&.]{8,}$");
    private static final String SUPPORTED_IMAGE_FILES = "(?i).*\\.(png|jpg|jpeg|gif)$";
    private static final String SUPPORTED_FILES = "(?i).*\\.(docx?|xlsx?|pptx?|pdf|txt|rtf|jpg|jpeg|png|gif|bmp|tiff|webp|mp4|mov|avi|wmv|mp3|wav|m4a|zip|7z|tar)$";

    public class UserValidations implements UserValidator {
        private ErrorHandler errorHandler;

        public UserValidations(ErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
        }

        @Override
        public boolean validEmail(String email) {
            if (email == null || !EMAIL_REGEX.matcher(email).matches()) {
                errorHandler.logError(errorHandler.createErrorBody("email",
                        "Please enter a valid email address (e.g., user@example.com)."));
                return false;
            }
            return true;
        }

        @Override
        public boolean validPassword(String currentPassword, String password, String email, FormType form) {
            if (password == null || !PASSWORD_REGEX.matcher(password).matches()) {
                if (form == FormType.ADDACCOUNT || form == FormType.REGISTER) {
                    errorHandler.logError(errorHandler.createErrorBody("password",
                            "Password must include uppercase, lowercase, number, and special character, and be at least 8 characters."));
                    return false;
                }

                if (form == FormType.FORGOTCREDENTIALS) {
                    errorHandler.logError(errorHandler.createErrorBody("newPassword",
                            "New password must include uppercase, lowercase, number, and special character, and be at least 8 characters."));
                    return false;
                }
            }

            if (email != null) {
                int atIndex = email.indexOf('@');
                String emailPart = atIndex == -1 ? null : email.substring(0, atIndex).toLowerCase().trim();
                String lowerPassword = password.toLowerCase();
                String[] parts = emailPart.split("[._-]");

                if (parts.length == 0 || parts[0].isEmpty()) {
                    parts = new String[] { emailPart };
                }

                for (String part : parts) {
                    if ((form == FormType.ADDACCOUNT || form == FormType.REGISTER) && lowerPassword.contains(part)
                            && emailPart.length() >= 4) {
                        errorHandler.logError(
                                errorHandler.createErrorBody("password", "Password is too similar to your email."));
                        return false;
                    }

                    if (form == FormType.FORGOTCREDENTIALS && lowerPassword.contains(part) && emailPart.length() >= 4) {
                        errorHandler.logError(errorHandler.createErrorBody("newPassword",
                                "New password is too similar to your email."));
                        return false;
                    }
                }
            }

            // TODO - for similarity use Levenshtein distance (edit distance), Substring
            // overlap, Common prefix/suffix comparison (optional)

            if (currentPassword != null) {
                if ((form == FormType.ADDACCOUNT || form == FormType.REGISTER)
                        && BCrypt.checkpw(password, currentPassword)) {
                    errorHandler.logError(errorHandler.createErrorBody("password",
                            "Password must be different from the current password."));
                    return false;
                }

                if (form == FormType.FORGOTCREDENTIALS && BCrypt.checkpw(password, currentPassword)) {
                    errorHandler.logError(errorHandler.createErrorBody("newPassword",
                            "New password must be different from the current password."));
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean nonDuplicateUserWithEmail(OperationType operation, String currentUserEmail, String newEmail,
                List<UserDTO> userDTOs) {
            if (userDTOs != null && !userDTOs.isEmpty()) {
                List<UserDTO> fliteredUserDTOs = operation == OperationType.UPDATE && currentUserEmail != null
                        ? userDTOs.stream().filter(userDTO -> !userDTO.getMailAccount().equals(currentUserEmail))
                                .collect(Collectors.toList())
                        : userDTOs;

                for (UserDTO userDTO : fliteredUserDTOs) {
                    if (userDTO.getMailAccount().equals(newEmail)) {
                        errorHandler.logError(errorHandler.createErrorBody("email", "Provided email is already used."));
                        return false;
                    }
                }

                return true;
            }
            return true;
        }

        @Override
        public boolean confirmedPassword(String password, String confirmationPassword, FormType form) {
            if (!password.equals(confirmationPassword)) {
                if (form == FormType.ADDACCOUNT || form == FormType.REGISTER) {
                    errorHandler.logError(errorHandler.createErrorBody("confirmPassword",
                            "Password does not match the confirmation."));
                }

                if (form == FormType.FORGOTCREDENTIALS) {
                    errorHandler.logError(errorHandler.createErrorBody("confirmNewPassword",
                            "New password does not match the confirmation."));
                }
                return false;
            }
            return true;
        }

        @Override
        public boolean validProfileImage(File profileImage) {
            if (profileImage != null) {
                String name = profileImage.getName().toLowerCase();
                if (!name.matches(SUPPORTED_IMAGE_FILES)) {
                    errorHandler
                            .logError(errorHandler.createErrorBody("file", "Unsupported file type for profile image."));
                    return false;
                }
                return true;
            }
            return true;
        }
    }

    public class MessageValidations implements MessageValidator {
        private ErrorHandler errorHandler;
        private Map<Integer, String> messagePartForNullFiles = new HashMap<>();
        private Map<MessageStatus, EnumSet<MessageStatus>> allowedByStatus;

        private void defineMessageStatusTransition() {
            Map<MessageStatus, EnumSet<MessageStatus>> map = new HashMap<>();

            map.put(MessageStatus.INBOX, EnumSet.of(MessageStatus.TRASH, MessageStatus.STARRED));
            map.put(MessageStatus.SENT, EnumSet.of(MessageStatus.TRASH, MessageStatus.STARRED));
            map.put(MessageStatus.STARRED, EnumSet.of(MessageStatus.TRASH));

            map.put(MessageStatus.DRAFTS, EnumSet.noneOf(MessageStatus.class));
            map.put(MessageStatus.TRASH, EnumSet.noneOf(MessageStatus.class));

            allowedByStatus = Collections.unmodifiableMap(map);
        }

        public MessageValidations(ErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
            defineMessageStatusTransition();
        }

        @Override
        public boolean validFiles(List<File> files) {
            messagePartForNullFiles.put(1, "first");
            messagePartForNullFiles.put(2, "second");
            messagePartForNullFiles.put(3, "third");
            messagePartForNullFiles.put(4, "fourth");
            messagePartForNullFiles.put(5, "fifth");

            if (files != null && files.size() > 5) {
                errorHandler.logError(
                        errorHandler.createErrorBody("file", "Too much attached files in one message (max. 5)."));
                return false;
            }

            if (files != null) {
                for (File file : files) {
                    if (file == null) {
                        errorHandler.logError(errorHandler.createErrorBody("file",
                                "We couldn't process your " + messagePartForNullFiles.get(files.indexOf(file) + 1)
                                        + " file. Make sure it's uploaded and in a supported format."));
                        return false;
                    }

                    String fileName = file.getName().toLowerCase();

                    if (file.length() > MAX_FILE_SIZE) {
                        errorHandler.logError(errorHandler.createErrorBody("file", "The file \"" + file.getName()
                                + "\" is too big — only files smaller than 25 MB can be sent."));
                        return false;
                    }

                    if (!fileName.matches(SUPPORTED_FILES)) {
                        errorHandler.logError(
                                errorHandler.createErrorBody("file", "Unsupported file type: " + file.getName() + "."));
                        return false;
                    }
                }
                return true;
            }
            return true;
        }

        @Override
        public boolean validMessageData(String whom, String subject, String message) {
            if (whom == null || !EMAIL_REGEX.matcher(whom).matches()) {
                errorHandler.logError(errorHandler.createErrorBody("email",
                        "Please enter a valid email address (e.g., user@example.com)."));
                return false;
            }

            if (isTextTooLong(subject, 50)) {
                errorHandler.logError(errorHandler.createErrorBody("subject", "Subject is too long."));
                return false;
            }

            if (isTextTooLong(message, 700)) {
                errorHandler.logError(errorHandler.createErrorBody("message", "Message is too long."));
                return false;
            }
            return true;
        }

        private boolean isTextTooLong(String text, int limit) {
            if (text == null)
                return false;

            return Arrays.stream(text.trim().split("(?<=\\S)\\s+(?=\\S)")).mapToInt(w -> w.trim().length())
                    .sum() > limit;
        }

        @Override
        public boolean containsOnlyAllowedStatuses(Map<String, EnumSet<MessageStatus>> messageStatuses,
                EnumSet<MessageStatus> expectedMessageStatus) {
            for (EnumSet<MessageStatus> statuses : messageStatuses.values()) {
                for (MessageStatus status : statuses) {
                    if (!expectedMessageStatus.contains(status)) {
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public boolean isStatusUpdateAllowed(EnumSet<MessageStatus> messageStatuses, MessageStatus newStatus) {
            for (MessageStatus messageStatus : messageStatuses) {
                if (!allowedByStatus.get(messageStatus).contains(newStatus)) {
                    return false;
                }
            }
            return true;
        }
    }
}
