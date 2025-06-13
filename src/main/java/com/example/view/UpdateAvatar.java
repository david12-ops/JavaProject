package com.example.view;

import java.io.File;

import com.example.components.ImageDropZone;
import com.example.controller.MessageController;
import com.example.controller.ScreenController;
import com.example.controller.UserController;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UpdateAvatar extends VBox {
    private ImageDropZone dropZone = new ImageDropZone();

    public UpdateAvatar(Stage stage, ScreenController screenController, UserController userController,
            MessageController messageController) {
        Label dropDownLabel = new Label("Drop new image profile");
        dropDownLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label imageErrorLabel = new Label();
        imageErrorLabel.getStyleClass().add("error-label");

        dropZone.setOnImageDropped(image -> {
            try {
                dropZone.setVisibility(true);
                imageErrorLabel.setText("");
                userController.clearError("file");
                File file = new File(new java.net.URI(image.getUrl()));
                userController.updateImageProfile(file);

                if (userController.getError("file") == null) {
                    screenController.updateScreen("main",
                            new MainScreen(stage, screenController, userController, messageController));
                    screenController.updateScreen("switchUser",
                            new SwitchUserScreen(stage, screenController, userController, messageController));
                    screenController.activate("main", stage);
                } else {
                    imageErrorLabel.setText(userController.getError("file"));
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Button defaultButton = new Button("Default profile");
        defaultButton.getStyleClass().add("updateButton");
        defaultButton.setOnAction(event -> {
            userController.updateImageProfile(null);
            dropZone.clear();
            imageErrorLabel.setText("");
            screenController.updateScreen("main",
                    new MainScreen(stage, screenController, userController, messageController));
            screenController.updateScreen("switchUser",
                    new SwitchUserScreen(stage, screenController, userController, messageController));
            screenController.activate("main", stage);
        });

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("appButton");
        backButton.setOnAction(event -> {
            screenController.activate("main", stage);
        });

        dropZone.setPrefSize(200, 200);
        dropZone.setMaxWidth(200);

        HBox buttonBox = new HBox(20, backButton, defaultButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox imageBox = new VBox(10, dropDownLabel, imageErrorLabel, dropZone, buttonBox);
        imageBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(imageBox, Priority.ALWAYS);

        this.getChildren().add(imageBox);
        this.setAlignment(Pos.CENTER);
        this.setSpacing(10);
    }

    public static void show(Stage stage, ScreenController screenController, UserController userController,
            MessageController messageController) {
        Scene scene = new Scene(new UpdateAvatar(stage, screenController, userController, messageController));
        stage.setScene(scene);
        stage.show();
    }
}
