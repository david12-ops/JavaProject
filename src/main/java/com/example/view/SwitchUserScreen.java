package com.example.view;

import java.util.List;

import com.example.components.CustomGridPane;
import com.example.components.Layout;
import com.example.controller.MessageController;
import com.example.controller.ScreenController;
import com.example.controller.UserController;
import com.example.dto.UserDTO;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SwitchUserScreen extends VBox {
    public SwitchUserScreen(Stage stage, ScreenController screenController, UserController userController,
            MessageController messageController) {
        List<UserDTO> userDTOs = userController.getAllUserAccounts();
        Layout layout = null;
        VBox content = null;

        Label textInfoLabel = new Label("No another accounts found");
        textInfoLabel.setStyle("-fx-text-fill: orangered; -fx-font-size: 30px;");
        textInfoLabel.setAlignment(Pos.CENTER);
        textInfoLabel.setMaxWidth(Double.MAX_VALUE);

        Label textTitle = new Label("Your accounts");
        textTitle.setStyle("-fx-text-fill: rgb(244, 160, 4); -fx-font-size: 30px;");
        textTitle.setAlignment(Pos.CENTER);
        textTitle.setMaxWidth(Double.MAX_VALUE);

        if (userDTOs != null && userDTOs.size() > 0) {
            CustomGridPane gridPane = new CustomGridPane(screenController, userController, messageController, stage);
            content = new VBox(textTitle, gridPane);

            layout = new Layout(stage, content, screenController, userController, messageController);
        } else {
            HBox textInfHBox = new HBox(textInfoLabel);
            textInfHBox.setAlignment(Pos.CENTER);
            content = new VBox(20, textTitle, textInfHBox);

            layout = new Layout(stage, content, screenController, userController, messageController);
        }

        this.getChildren().add(layout);
    }

    public static void show(Stage stage, ScreenController screenController, UserController userController,
            MessageController messageController) {
        Scene scene = new Scene(new SwitchUserScreen(stage, screenController, userController, messageController));
        stage.setScene(scene);
        stage.show();
    }

}
