package com.example.components;

import com.example.controller.MessageController;
import com.example.controller.ScreenController;
import com.example.controller.UserController;
import com.example.view.ForgotCredentialsScreen;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Layout extends BorderPane {
    public Layout(Stage stage, Node content, ScreenController screenController, UserController userController,
            MessageController messageController) {

        VBox contentArea = new VBox(content);
        contentArea.setPadding(new Insets(15));
        contentArea.setFillWidth(false);
        contentArea.setAlignment(Pos.TOP_CENTER);

        AppBar appBar = new AppBar(stage, "Send It!", userController, screenController);
        appBar.setAvatarImage(userController.getImageProfile());
        appBar.getLogoutButton().setOnAction(event -> {
            userController.logOut();
            screenController.updateScreen("reset",
                    new ForgotCredentialsScreen(stage, screenController, userController));
            screenController.activate("login", stage);
        });

        SideBar sideBar = new SideBar(stage, screenController, userController, messageController);
        sideBar.setVisible(false);
        sideBar.setManaged(false);

        appBar.getBurgerButton().setOnAction(e -> {
            boolean show = !sideBar.isVisible();
            sideBar.setVisible(show);
            sideBar.setManaged(show);
        });

        this.setTop(appBar);
        this.setLeft(sideBar);
        this.setCenter(contentArea);
    }
}
