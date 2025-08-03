package com.example.view;

import java.util.EnumSet;

import com.example.components.FileChooserUI;
import com.example.controller.MessageController;
import com.example.controller.ScreenController;
import com.example.controller.UserController;
import com.example.dto.MessageDTO;
import com.example.model.UserToken;
import com.example.utils.enums.FileChooserUIState;
import com.example.utils.enums.MessageStatus;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DetailMessageScreen extends VBox {
    private void updateStatusAction(Stage stage, UserController userController, ScreenController screenController,
            MessageController messageController, MessageDTO messageDTO, MessageStatus newMessageStatus,
            UserToken userToken) {
        messageController.updateStatus(userToken, messageDTO, newMessageStatus);
        screenController.updateScreen("main", new MainScreen(stage, screenController, userController, messageController,
                EnumSet.of(newMessageStatus)));
        screenController.activate("main", stage);
    }

    private FileChooserUI fileChooserUI;

    public DetailMessageScreen(Stage stage, UserController userController, ScreenController screenController,
            MessageController messageController, MessageDTO messageDTO) {
        UserToken userToken = userController.getLoggedUser();
        fileChooserUI = new FileChooserUI(new Label(), messageController, FileChooserUIState.READONLY);
        fileChooserUI.setFilesToDisplay(messageDTO != null ? messageDTO.getAttachedBase64Files() : null);

        Label senderLabel = new Label("Sender:");
        senderLabel.setAlignment(Pos.CENTER);

        Label subjectLabel = new Label("Subject:");
        subjectLabel.setAlignment(Pos.CENTER);

        Label messageLabel = new Label("Message:");
        messageLabel.setAlignment(Pos.CENTER);

        Label attachmentsLabel = new Label("Attachments:");
        attachmentsLabel.setAlignment(Pos.CENTER);

        TextField senderField = new TextField(messageDTO != null ? messageDTO.getRecevierMailAccount() : "");
        senderField.setEditable(false);
        senderField.getStyleClass().add("text-field");

        TextField subjectField = new TextField(messageDTO != null ? messageDTO.getSubject() : "");
        subjectField.setEditable(false);
        subjectField.getStyleClass().add("text-field");

        TextArea messageAreaField = new TextArea(messageDTO != null ? messageDTO.getMessage() : "");
        messageAreaField.setEditable(false);
        messageAreaField.getStyleClass().add("text-area-field");
        messageAreaField.setPrefRowCount(15);
        messageAreaField.setWrapText(true);

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("appButton");
        backButton.setOnAction(e -> {
            screenController.activate("main", stage);
        });

        Button starredButton = new Button("Add to favorites");
        starredButton.getStyleClass().add("updateButton");
        starredButton.setOnAction(e -> {
            updateStatusAction(stage, userController, screenController, messageController, messageDTO,
                    MessageStatus.STARRED, userToken);
        });

        HBox buttonBox = new HBox(20, backButton, starredButton);
        buttonBox.setAlignment(Pos.CENTER);

        HBox senderBox = new HBox(10, senderLabel, senderField);
        senderBox.setAlignment(Pos.CENTER);

        HBox subjectBox = new HBox(10, subjectLabel, subjectField);
        subjectBox.setAlignment(Pos.CENTER);

        HBox messageBox = new HBox(10, messageLabel, messageAreaField);
        messageBox.setAlignment(Pos.CENTER);

        HBox attachmentsBox = new HBox(10, attachmentsLabel, fileChooserUI);
        attachmentsBox.setAlignment(Pos.CENTER);

        VBox form = new VBox(25, senderBox, subjectBox, messageBox, attachmentsBox, buttonBox);
        form.setAlignment(Pos.CENTER);

        form.setAlignment(Pos.CENTER);

        this.getChildren().add(form);
        this.setAlignment(Pos.CENTER);
    }

    public static void show(Stage stage, UserController userController, ScreenController screenController,
            MessageController messageController, MessageDTO messageDTO) {
        Scene scene = new Scene(
                new DetailMessageScreen(stage, userController, screenController, messageController, messageDTO));
        stage.setScene(scene);
        stage.show();
    }
}
