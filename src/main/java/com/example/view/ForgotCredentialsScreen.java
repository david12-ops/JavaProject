package com.example.view;

import com.example.controller.ScreenController;
import com.example.controller.UserController;
import com.example.model.UserToken;
import com.example.utils.interfaces.GuiErrorHelper;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class ForgotCredentialsScreen extends VBox implements GuiErrorHelper {
    @Override
    public Label createErrorLabel() {
        Label label = new Label();
        label.setWrapText(true);
        label.setMaxWidth(250);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setAlignment(Pos.CENTER);
        label.getStyleClass().add("error-label");
        return label;
    }

    @Override
    public void showIfError(String error, Label label) {
        label.setText(error != null ? error : "");
    }

    @Override
    public void clearErrorLabels(Label... labels) {
        for (Label label : labels) {
            label.setText("");
        }
    }

    private void clearFields(PasswordField newPasswordField, PasswordField confirmNewPasswordField,
            PasswordField passwordField, TextField emailField, UserController userController, Label emailErrorLabel,
            Label passwordErrorLabel, Label newPasswordErrorLabel, Label confirmNewPasswordErrorLabel,
            Label labelError) {

        if (passwordErrorLabel != null && passwordField != null) {
            passwordField.clear();
            passwordErrorLabel.setText("");
            userController.clearAuthError("password");
        }

        if (newPasswordErrorLabel != null && newPasswordField != null) {
            newPasswordField.clear();
            newPasswordErrorLabel.setText("");
            userController.clearAuthError("newPassword");
        }

        if (confirmNewPasswordErrorLabel != null && confirmNewPasswordField != null) {
            confirmNewPasswordField.clear();
            confirmNewPasswordErrorLabel.setText("");
            userController.clearAuthError("confirmNewPassword");
        }

        if (emailErrorLabel != null && emailField != null) {
            emailField.clear();
            emailErrorLabel.setText("");
            userController.clearAuthError("email");
        }

        labelError.setText("");
    }

    private VBox createVbox(UserToken userToken, Label labelError, Label emailLabel, TextField emailField,
            Label emailErrorLabel, Label passwordLabel, PasswordField passwordField, Label passwordErrorLabel,
            Label newPasswordLabel, PasswordField newPasswordField, Label newPasswordErrorLabel,
            Label confirmPasswordLabel, PasswordField confirmNewPasswordField, Label confirmNewPasswordErrorLabel,
            HBox buttonBox) {
        return userToken != null
                ? new VBox(5, labelError, newPasswordLabel, newPasswordField, newPasswordErrorLabel,
                        confirmPasswordLabel, confirmNewPasswordField, confirmNewPasswordErrorLabel, buttonBox)
                : new VBox(5, labelError, emailLabel, emailField, emailErrorLabel, passwordLabel, passwordField,
                        passwordErrorLabel, newPasswordLabel, newPasswordField, newPasswordErrorLabel,
                        confirmPasswordLabel, confirmNewPasswordField, confirmNewPasswordErrorLabel, buttonBox);
    }

    private void resetButtonAction(Stage stage, TextField emailField, PasswordField passwordField,
            PasswordField newPasswordField, PasswordField confirmNewPasswordField, Label emailErrorLabel,
            Label passwordErrorLabel, Label newPasswordErrorLabel, Label confirmNewPasswordErrorLabel,
            UserController userController, ScreenController screenController, Label labelError, UserToken userToken) {
        boolean isValid = true;
        labelError.setText("");
        clearErrorLabels(emailErrorLabel, passwordErrorLabel, newPasswordErrorLabel, confirmNewPasswordErrorLabel);

        if (userToken == null) {
            if (emailField.getText().isBlank()) {
                emailErrorLabel.setText("Email is required");
                isValid = false;
            }

            if (passwordField.getText().isBlank()) {
                passwordErrorLabel.setText("Current password is required");
                isValid = false;
            }
        }

        if (newPasswordField.getText().isBlank()) {
            newPasswordErrorLabel.setText("New password is required");
            isValid = false;
        }

        if (confirmNewPasswordField.getText().isBlank()) {
            confirmNewPasswordErrorLabel.setText("Confirmation new password is required");
            isValid = false;
        }

        boolean updated = false;

        if (userToken != null) {
            if (isValid) {
                updated = userController.updateLoggedInAccount(newPasswordField.getText(),
                        confirmNewPasswordField.getText());

                showIfError(userController.getAuthError("newPassword"), newPasswordErrorLabel);
                showIfError(userController.getAuthError("confirmNewPassword"), confirmNewPasswordErrorLabel);

                if (userController.getAuthError("newPassword") != null
                        || userController.getAuthError("confirmNewPassword") != null) {
                    isValid = false;
                }
            }

            if (isValid && updated) {
                clearFields(newPasswordField, confirmNewPasswordField, null, null, userController, null, null,
                        newPasswordErrorLabel, confirmNewPasswordErrorLabel, labelError);
                userController.logOut();
                screenController.activate("login", stage);
            } else if (isValid) {
                labelError.setText(
                        "Password update failed due to an unexpected error or bad credentials. Please try again or contact support");
            }
        }

        if (userToken == null) {
            if (isValid) {
                updated = userController.updateNotLoggedAccount(emailField.getText(), passwordField.getText(),
                        newPasswordField.getText(), confirmNewPasswordField.getText());

                showIfError(userController.getAuthError("newPassword"), newPasswordErrorLabel);
                showIfError(userController.getAuthError("confirmNewPassword"), confirmNewPasswordErrorLabel);

                if (userController.getAuthError("newPassword") != null
                        || userController.getAuthError("confirmNewPassword") != null) {
                    isValid = false;
                }
            }

            if (isValid && updated) {
                clearFields(newPasswordField, confirmNewPasswordField, passwordField, emailField, userController,
                        emailErrorLabel, passwordErrorLabel, newPasswordErrorLabel, confirmNewPasswordErrorLabel,
                        labelError);
                screenController.activate("login", stage);
            } else if (isValid) {
                labelError.setText(
                        "Password update failed due to an unexpected error or bad credentials. Please try again or contact support");
            }
        }
    }

    private void onchangeInitialize(TextField emailField, PasswordField passwordField, PasswordField newPasswordField,
            PasswordField confirmNewPasswordField, Label emailErrorLabel, Label passwordErrorLabel,
            Label newPasswordErrorLabel, Label confirmNewPasswordErrorLabel, Label labelError,
            UserController userController) {
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isBlank()) {
                labelError.setText("");
                emailErrorLabel.setText("");
            }
        });

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isBlank()) {
                labelError.setText("");
                passwordErrorLabel.setText("");
            }
        });

        newPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isBlank()) {
                newPasswordErrorLabel.setText("");
                labelError.setText("");
                userController.clearAuthError("newPassword");
            }
        });

        confirmNewPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isBlank()) {
                confirmNewPasswordErrorLabel.setText("");
                labelError.setText("");
                userController.clearAuthError("confirmNewPassword");
            }
        });
    }

    public ForgotCredentialsScreen(Stage stage, ScreenController screenController, UserController userController) {
        UserToken userToken = userController.getLoggedUser();

        Label labelError = createErrorLabel();

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        emailField.getStyleClass().add("text-field");

        Label emailErrorLabel = createErrorLabel();

        Label passwordLabel = new Label("Current password:");
        PasswordField passwordField = new PasswordField();
        passwordField.getStyleClass().add("password-field");

        Label passwordErrorLabel = createErrorLabel();

        Label newPasswordLabel = new Label("New password:");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.getStyleClass().add("password-field");

        Label newPasswordErrorLabel = createErrorLabel();

        Label confirmPasswordLabel = new Label("Confirm new password:");
        PasswordField confirmNewPasswordField = new PasswordField();
        confirmNewPasswordField.getStyleClass().add("password-field");

        Label confirmNewPasswordErrorLabel = createErrorLabel();

        onchangeInitialize(emailField, passwordField, newPasswordField, confirmNewPasswordField, emailErrorLabel,
                passwordErrorLabel, newPasswordErrorLabel, confirmNewPasswordErrorLabel, labelError, userController);

        Button resetButton = new Button("Reset");
        resetButton.getStyleClass().add("updateButton");
        resetButton.setOnAction(event -> {
            resetButtonAction(stage, emailField, passwordField, newPasswordField, confirmNewPasswordField,
                    emailErrorLabel, passwordErrorLabel, newPasswordErrorLabel, confirmNewPasswordErrorLabel,
                    userController, screenController, labelError, userToken);
        });

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("appButton");
        backButton.setOnAction(event -> {
            if (userToken != null) {
                clearFields(newPasswordField, confirmNewPasswordField, null, null, userController, null, null,
                        newPasswordErrorLabel, confirmNewPasswordErrorLabel, labelError);
                screenController.activate("main", stage);
            } else {
                clearFields(newPasswordField, confirmNewPasswordField, passwordField, emailField, userController,
                        emailErrorLabel, passwordErrorLabel, newPasswordErrorLabel, confirmNewPasswordErrorLabel,
                        labelError);
                screenController.activate("login", stage);
            }
        });

        HBox buttonBox = new HBox(20, backButton, resetButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox form = createVbox(userToken, labelError, emailLabel, emailField, emailErrorLabel, passwordLabel,
                passwordField, passwordErrorLabel, newPasswordLabel, newPasswordField, newPasswordErrorLabel,
                confirmPasswordLabel, confirmNewPasswordField, confirmNewPasswordErrorLabel, buttonBox);

        form.setAlignment(Pos.CENTER);

        this.getChildren().add(form);
        this.setAlignment(Pos.CENTER);
    }

    public static void show(Stage stage, ScreenController screenController, UserController userController) {
        Scene scene = new Scene(new ForgotCredentialsScreen(stage, screenController, userController));
        stage.setScene(scene);
        stage.show();
    }

}
