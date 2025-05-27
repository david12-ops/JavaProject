package com.example.view;

import com.example.components.Layout;
import com.example.controller.MessageController;
import com.example.controller.ScreenController;
import com.example.controller.UserController;
import com.example.model.Message;
import com.example.model.UserToken;
import com.example.utils.enums.MessageStatus;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;

public class MainScreen extends VBox {
    // TODO - styling - message part
    // TODO - bug in avatar in switchUser - switched but avatar dont updated - fixed
    // (optional better way)
    // TODO - build tests - message part

    public MainScreen(Stage stage, ScreenController screenController, UserController userController,
            MessageController messageController) {

        UserToken userToken = userController.getLoggedUser();

        List<Message> receviedMessages = messageController.getMessages(MessageStatus.INBOX, userToken);

        // listView.setCellFactory(lv -> new ListCell<>() {
        // @Override
        // protected void updateItem(Message msg, boolean empty) {
        // super.updateItem(msg, empty);
        // if (empty || msg == null) {
        // setText(null);
        // } else {
        // setText(msg.getSender() + " - " + msg.getSubject());
        // }
        // }
        // });

        ObservableList<Message> items = FXCollections.observableList(receviedMessages);
        ListView<String> listView = new ListView(items);

        Button button = new Button("Update profile");
        button.setOnAction(event -> {
            screenController.activate("updateAvatarImage", stage);
        });

        VBox mainBox = new VBox(button);

        Layout layout = new Layout(stage, mainBox, screenController, userController, messageController);

        this.getChildren().add(layout);

    }

    public static void show(Stage stage, ScreenController screenController, UserController userController,
            MessageController messageController) {
        Scene scene = new Scene(new MainScreen(stage, screenController, userController, messageController));
        stage.setScene(scene);
        stage.show();
    }

}
