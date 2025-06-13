package com.example.view;

import com.example.components.Layout;
import com.example.components.Table;
import com.example.controller.MessageController;
import com.example.controller.ScreenController;
import com.example.controller.UserController;
import com.example.model.UserToken;
import com.example.utils.enums.MessageStatus;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainScreen extends VBox {
    // TODO - comunication between message nad user controller to provide data to
    // message
    // TODO - Better use of StateService
    // TODO - build tests - message part
    // TODO - test on services too (auth, account)

    public MainScreen(Stage stage, ScreenController screenController, UserController userController,
            MessageController messageController) {

        UserToken userToken = userController.getLoggedUser();

        Table table = new Table(messageController, userToken, MessageStatus.INBOX);

        Layout layout = new Layout(stage, table, screenController, userController, messageController);

        this.getChildren().add(layout);
    }

    public static void show(Stage stage, ScreenController screenController, UserController userController,
            MessageController messageController) {
        Scene scene = new Scene(new MainScreen(stage, screenController, userController, messageController));
        stage.setScene(scene);
        stage.show();
    }

}
