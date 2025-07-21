package com.example.view;

import com.example.controller.MessageController;
import com.example.controller.ScreenController;
import com.example.controller.UserController;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoginScreen extends VBox {

    private Label createErrorLabel() {
        Label label = new Label();
        label.setWrapText(true);
        label.setMaxWidth(250);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setAlignment(Pos.CENTER);
        label.getStyleClass().add("error-label");
        return label;
    }

    private void updateScreens(ScreenController screenController, UserController userController,
            MessageController messageController, Stage stage) {
        screenController.updateScreen("main",
                new MainScreen(stage, screenController, userController, messageController));
        screenController.updateScreen("reset", new ForgotCredentialsScreen(stage, screenController, userController));
        screenController.updateScreen("switchUser",
                new SwitchUserScreen(stage, screenController, userController, messageController));
    }

    private void loginButtonAction(TextField emailField, PasswordField passwordField, Label emailErrorLabel,
            Label passwordErrorLabel, UserController userController, ScreenController screenController,
            MessageController messageController, Label labelError, Stage stage) {

        boolean isValid = true;

        if (emailField.getText().isBlank()) {
            emailErrorLabel.setText("Email is required");
            isValid = false;
        }

        if (passwordField.getText().isBlank()) {
            passwordErrorLabel.setText("Password is required");
            isValid = false;
        }

        if (isValid) {
            userController.login(emailField.getText(), passwordField.getText());
            if (userController.getLoggedUser() != null) {
                emailField.clear();
                passwordField.clear();
                updateScreens(screenController, userController, messageController, stage);
                screenController.activate("main", stage);
            } else {
                labelError.setText(
                        "User not found or registration failed due to an unexpected error. Please try again or contact support");
            }
        }
    }

    private void onchangeInitialize(TextField emailField, PasswordField passwordField, Label emailErrorLabel,
            Label passwordErrorLabel, Label labelError) {
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isBlank()) {
                emailErrorLabel.setText("");
                labelError.setText("");
            }
        });

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isBlank()) {
                passwordErrorLabel.setText("");
                labelError.setText("");
            }
        });
    }

    public LoginScreen(Stage stage, ScreenController screenController, UserController userController,
            MessageController messageController) {
        Label labelError = createErrorLabel();

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        emailField.getStyleClass().add("text-field");

        Label emailErrorLabel = createErrorLabel();

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.getStyleClass().add("password-field");

        Label passwordErrorLabel = createErrorLabel();

        onchangeInitialize(emailField, passwordField, emailErrorLabel, passwordErrorLabel, labelError);

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("appButton");
        loginButton.setOnAction(event -> {
            loginButtonAction(emailField, passwordField, emailErrorLabel, passwordErrorLabel, userController,
                    screenController, messageController, labelError, stage);
        });

        Hyperlink registerLink = new Hyperlink("Don't have an account? Register");
        registerLink.setOnAction(event -> {
            emailField.clear();
            passwordField.clear();
            emailErrorLabel.setText("");
            passwordErrorLabel.setText("");
            labelError.setText("");
            screenController.activate("register", stage);
        });

        Hyperlink resetLink = new Hyperlink("Reset password");
        resetLink.setOnAction(event -> {
            emailField.clear();
            passwordField.clear();
            emailErrorLabel.setText("");
            passwordErrorLabel.setText("");
            labelError.setText("");
            screenController.activate("reset", stage);
        });

        VBox form = new VBox(5, labelError, emailLabel, emailField, emailErrorLabel, passwordLabel, passwordField,
                passwordErrorLabel, loginButton, registerLink, resetLink);
        form.setAlignment(Pos.CENTER);

        this.getChildren().add(form);
        this.setAlignment(Pos.CENTER);
    }

    public static void show(Stage stage, ScreenController screenController, UserController userController,
            MessageController messageController) {
        Scene scene = new Scene(new LoginScreen(stage, screenController, userController, messageController));
        stage.setScene(scene);
        stage.show();
    }
}
