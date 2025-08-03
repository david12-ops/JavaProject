package com.example;

import java.util.EnumSet;

import com.example.controller.MessageController;
import com.example.controller.ScreenController;
import com.example.controller.UserController;
import com.example.utils.enums.MessageStatus;
import com.example.view.AddAnotherAccountScreen;
import com.example.view.DetailMessageScreen;
import com.example.view.ForgotCredentialsScreen;
import com.example.view.LoginScreen;
import com.example.view.MainScreen;
import com.example.view.NewMessageScreen;
import com.example.view.RegisterScreen;
import com.example.view.SwitchUserScreen;
import com.example.view.UpdateAvatar;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
        // Icons use from - https://icons8.com
        private UserController userController = new UserController();
        private MessageController messageController = new MessageController();
        private ScreenController screenController;

        @Override
        public void start(Stage primaryStage) {
                screenController = new ScreenController(null);

                LoginScreen loginScreen = new LoginScreen(primaryStage, screenController, userController,
                                messageController);
                RegisterScreen registerScreen = new RegisterScreen(primaryStage, screenController, userController);
                ForgotCredentialsScreen resetScreen = new ForgotCredentialsScreen(primaryStage, screenController,
                                userController);
                AddAnotherAccountScreen anotherAccountScreen = new AddAnotherAccountScreen(primaryStage,
                                screenController, userController, messageController);
                SwitchUserScreen switchUserScreen = new SwitchUserScreen(primaryStage, screenController, userController,
                                messageController);
                MainScreen mainScreen = new MainScreen(primaryStage, screenController, userController,
                                messageController, EnumSet.of(MessageStatus.INBOX));
                UpdateAvatar updateAvatarScreen = new UpdateAvatar(primaryStage, screenController, userController,
                                messageController);
                NewMessageScreen newMessageScreen = new NewMessageScreen(primaryStage, screenController, userController,
                                messageController);
                DetailMessageScreen detailMessageScreen = new DetailMessageScreen(primaryStage, userController,
                                screenController, messageController, null);

                Scene scene = new Scene(loginScreen, 400, 300);
                screenController.setScene(scene);

                screenController.addScreen("login", loginScreen);
                screenController.addScreen("register", registerScreen);
                screenController.addScreen("reset", resetScreen);
                screenController.addScreen("addAnotherAccount", anotherAccountScreen);
                screenController.addScreen("main", mainScreen);
                screenController.addScreen("updateAvatarImage", updateAvatarScreen);
                screenController.addScreen("switchUser", switchUserScreen);
                screenController.addScreen("newMessage", newMessageScreen);
                screenController.addScreen("detailMessage", detailMessageScreen);

                scene.getStylesheets().add(getClass().getResource("/styles/form.css").toExternalForm());
                scene.getStylesheets().add(getClass().getResource("/styles/appBar.css").toExternalForm());
                scene.getStylesheets().add(getClass().getResource("/styles/buttons.css").toExternalForm());

                screenController.activate("login", primaryStage);

                primaryStage.setScene(scene);
                primaryStage.show();
        }

        public static void main(String[] args) {
                launch(args);
        }
}
