package com.example.view;

import com.example.components.FileChooserUI;
import com.example.controller.MessageController;
import com.example.dto.MessageDTO;
import com.example.utils.enums.FileChooserUIState;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DetailMessageScreen extends VBox {
    private FileChooserUI fileChooserUI;

    public DetailMessageScreen(Stage stage, MessageController messageController, MessageDTO messageDTO,
            HBox buttonBox) {
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

        HBox senderBox = new HBox(10, senderLabel, senderField);
        senderBox.setAlignment(Pos.CENTER);

        HBox subjectBox = new HBox(10, subjectLabel, subjectField);
        subjectBox.setAlignment(Pos.CENTER);

        HBox messageBox = new HBox(10, messageLabel, messageAreaField);
        messageBox.setAlignment(Pos.CENTER);

        HBox attachmentsBox = new HBox(10, attachmentsLabel, fileChooserUI);
        attachmentsBox.setAlignment(Pos.CENTER);

        VBox form = buttonBox != null ? new VBox(25, senderBox, subjectBox, messageBox, attachmentsBox, buttonBox)
                : new VBox(25, senderBox, subjectBox, messageBox, attachmentsBox);
        form.setAlignment(Pos.CENTER);

        form.setAlignment(Pos.CENTER);

        this.getChildren().add(form);
        this.setAlignment(Pos.CENTER);
    }

    public static void show(Stage stage, MessageController messageController, MessageDTO messageDTO, HBox buttonBox) {
        Scene scene = new Scene(new DetailMessageScreen(stage, messageController, messageDTO, buttonBox));
        stage.setScene(scene);
        stage.show();
    }
}
