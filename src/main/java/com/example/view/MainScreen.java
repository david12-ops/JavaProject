package com.example.view;

import java.util.EnumSet;

import com.example.components.Layout;
import com.example.components.Table;
import com.example.controller.MessageController;
import com.example.controller.ScreenController;
import com.example.controller.UserController;
import com.example.utils.enums.MessageStatus;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainScreen extends VBox {
    // TODO - obnoveni zprávy
    // TODO - solve update status bug
    // TODO - build tests - message part
    // TODO - test on services too (auth, account)

    public MainScreen(Stage stage, ScreenController screenController, UserController userController,
            MessageController messageController, EnumSet<MessageStatus> messageStatuses) {
        Table table = new Table(stage, screenController, messageController, userController, messageStatuses);
        Layout layout = new Layout(stage, table, screenController, userController, messageController);

        this.getChildren().add(layout);
    }

    public static void show(Stage stage, ScreenController screenController, UserController userController,
            MessageController messageController, EnumSet<MessageStatus> messageStatuses) {
        Scene scene = new Scene(
                new MainScreen(stage, screenController, userController, messageController, messageStatuses));
        stage.setScene(scene);
        stage.show();
    }

}
