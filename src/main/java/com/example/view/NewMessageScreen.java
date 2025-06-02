package com.example.view;

import java.io.File;
import java.util.List;

import com.example.components.FileChooserUI;
import com.example.controller.MessageController;
import com.example.controller.ScreenController;
import com.example.controller.UserController;
import com.example.utils.interfaces.GuiHelperFunctions;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class NewMessageScreen extends VBox implements GuiHelperFunctions {

    @Override
    public Label createErrorLabel() {
        Label label = new Label();
        label.setWrapText(true);
        label.setMaxWidth(250);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setAlignment(Pos.CENTER);
        label.getStyleClass().add("error-label");
        return label;
    }

    @Override
    public void clearErrorLabels(Label... labels) {
        for (Label label : labels) {
            label.setText("");
        }
    }

    @Override
    public void showIfError(String error, Label label) {
        label.setText(error != null ? error : "");
    }

    private void clearFields(TextField whomField, TextField subjectField, TextArea messageAreaField,
            Label whomErrorLabel, Label subjectErrorLabel, Label messageTextAreaErrorLabel,
            MessageController messageController) {
        if (whomErrorLabel != null && whomField != null) {
            whomField.clear();
            whomErrorLabel.setText("");
            messageController.clearError("email");
        }

        if (subjectErrorLabel != null && subjectField != null) {
            subjectField.clear();
            subjectErrorLabel.setText("");
            messageController.clearError("subject");
        }

        if (messageTextAreaErrorLabel != null && messageAreaField != null) {
            messageAreaField.clear();
            messageTextAreaErrorLabel.setText("");
            messageController.clearError("message");
        }
    }

    private void sendButtonAction(UserController userController, ScreenController screenController,
            MessageController messageController, TextField whomField, TextField subjectField, TextArea messageAreaField,
            Label whomErrorLabel, Label subjectErrorLabel, Label messageTextAreaErrorLabel, Label attachFileErrorLabel,
            List<File> selectedFiles, Stage stage) {

        boolean valid = true;
        clearErrorLabels(whomErrorLabel, subjectErrorLabel, messageTextAreaErrorLabel);

        if (whomField.getText().isBlank()) {
            whomErrorLabel.setText("Email is required");
            valid = false;
        }

        if (valid) {
            messageController.addMessage(userController.getLoggedUser(), whomField.getText(), subjectField.getText(),
                    messageAreaField.getText(), selectedFiles);

            showIfError(messageController.getError("email"), whomErrorLabel);
            showIfError(messageController.getError("subject"), subjectErrorLabel);
            showIfError(messageController.getError("message"), messageTextAreaErrorLabel);
            showIfError(messageController.getError("file"), attachFileErrorLabel);

            if (messageController.getError("email") != null || messageController.getError("subject") != null
                    || messageController.getError("message") != null || messageController.getError("file") != null) {
                valid = false;
            }
        }

        if (valid) {
            clearFields(whomField, subjectField, messageAreaField, whomErrorLabel, subjectErrorLabel,
                    messageTextAreaErrorLabel, messageController);
            fileChooserUI.clearFileBox();
            screenController.updateScreen("main",
                    new MainScreen(stage, screenController, userController, messageController));
            screenController.activate("main", stage);
        }

    }

    private void onchangeInitialize(TextField whomField, TextField subjectField, TextArea messageAreaField,
            Label whomErrorLabel, Label subjectErrorLabel, Label messageTextAreaErrorLabel,
            MessageController messageController) {
        whomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isBlank()) {
                whomErrorLabel.setText("");
                messageController.clearError("email");
            }
        });

        subjectField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isBlank()) {
                subjectErrorLabel.setText("");
                messageController.clearError("subject");
            }
        });

        messageAreaField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isBlank()) {
                messageTextAreaErrorLabel.setText("");
                messageController.clearError("message");
            }
        });
    }

    private FileChooserUI fileChooserUI;

    public NewMessageScreen(Stage stage, ScreenController screenController, UserController userController,
            MessageController messageController) {

        Label attachFileErrorLabel = createErrorLabel();
        fileChooserUI = new FileChooserUI(attachFileErrorLabel, messageController);

        TextField whomField = new TextField();
        whomField.setPromptText("Whom:");
        whomField.getStyleClass().add("text-field");

        Label whomErrorLabel = createErrorLabel();

        TextField subjectField = new TextField();
        subjectField.setPromptText("Subject:");
        subjectField.getStyleClass().add("text-field");

        Label subjectErrorLabel = createErrorLabel();

        TextArea messageAreaField = new TextArea();
        messageAreaField.setPromptText("Message:");
        messageAreaField.getStyleClass().add("text-area-field");
        messageAreaField.setPrefRowCount(15);
        messageAreaField.setWrapText(true);

        Label messageTextAreaErrorLabel = createErrorLabel();

        onchangeInitialize(whomField, subjectField, messageAreaField, whomErrorLabel, subjectErrorLabel,
                messageTextAreaErrorLabel, messageController);

        Button sendButton = new Button("Send");
        sendButton.getStyleClass().add("addButton");
        sendButton.setOnAction(e -> {
            sendButtonAction(userController, screenController, messageController, whomField, subjectField,
                    messageAreaField, whomErrorLabel, subjectErrorLabel, messageTextAreaErrorLabel,
                    attachFileErrorLabel, fileChooserUI.getSelectedFiles(), stage);
        });

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("appButton");
        backButton.setOnAction(e -> {
            clearFields(whomField, subjectField, messageAreaField, whomErrorLabel, subjectErrorLabel,
                    messageTextAreaErrorLabel, messageController);
            fileChooserUI.clearFileBox();
            screenController.activate("main", stage);
        });

        HBox buttonBox = new HBox(20, backButton, sendButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox form = new VBox(5, whomField, whomErrorLabel, subjectField, subjectErrorLabel, messageAreaField,
                messageTextAreaErrorLabel, fileChooserUI, attachFileErrorLabel, buttonBox);
        form.setAlignment(Pos.CENTER);

        this.getChildren().add(form);
        this.setAlignment(Pos.CENTER);
    }

    public static void show(Stage stage, ScreenController screenController, UserController userController,
            MessageController messageController) {
        Scene scene = new Scene(new NewMessageScreen(stage, screenController, userController, messageController));
        stage.setScene(scene);
        stage.show();
    }
}
