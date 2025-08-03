package com.example.components;

import java.util.EnumSet;

import com.example.controller.MessageController;
import com.example.controller.ScreenController;
import com.example.controller.UserController;
import com.example.utils.enums.MessageStatus;
import com.example.view.MainScreen;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class SideBar extends VBox {
        private void showContent(Stage stage, ScreenController screenController, UserController userController,
                        MessageController messageController, EnumSet<MessageStatus> messageStatuses) {
                screenController.updateScreen("main", new MainScreen(stage, screenController, userController,
                                messageController, messageStatuses));
                screenController.activate("main", stage);
        }

        public SideBar(Stage stage, ScreenController screenController, UserController userController,
                        MessageController messageController) {
                setPadding(new Insets(20));
                setSpacing(10);
                setPrefWidth(200);
                setMinWidth(200);
                setMinHeight(800);

                CornerRadii radiiSideBar = new CornerRadii(0, 10, 0, 0, false);
                CornerRadii radiiButtonBox = new CornerRadii(10);

                this.setBorder(new Border(new BorderStroke(Color.web("rgb(243, 245, 224)"), BorderStrokeStyle.SOLID,
                                radiiSideBar, new BorderWidths(2))));

                this.setBackground(
                                new Background(new BackgroundFill(Color.web("#D9D89F"), radiiSideBar, Insets.EMPTY)));
                setAlignment(Pos.TOP_CENTER);

                Button addButton = new Button("New message");
                addButton.setOnAction(e -> {
                        screenController.activate("newMessage", stage);
                });

                Button inboxButton = new Button("Inbox");
                inboxButton.setOnAction(e -> {
                        showContent(stage, screenController, userController, messageController,
                                        EnumSet.of(MessageStatus.INBOX));
                });

                Button starredButton = new Button("Starred");
                starredButton.setOnAction(e -> {
                        showContent(stage, screenController, userController, messageController,
                                        EnumSet.of(MessageStatus.STARRED));
                });

                Button sentButton = new Button("Sent");
                sentButton.setOnAction(e -> {
                        showContent(stage, screenController, userController, messageController,
                                        EnumSet.of(MessageStatus.SENT));
                });

                Button draftsButton = new Button("Drafts");
                draftsButton.setOnAction(e -> {
                        showContent(stage, screenController, userController, messageController,
                                        EnumSet.of(MessageStatus.DRAFTS));
                });

                Button moreButton = new Button("More");
                Button allMailButton = new Button("All Mail");
                allMailButton.setOnAction(e -> {
                        showContent(stage, screenController, userController, messageController,
                                        EnumSet.of(MessageStatus.INBOX, MessageStatus.SENT, MessageStatus.STARRED,
                                                        MessageStatus.DRAFTS, MessageStatus.TRASH));
                });

                Button trashButton = new Button("Trash");
                trashButton.setOnAction(e -> {
                        showContent(stage, screenController, userController, messageController,
                                        EnumSet.of(MessageStatus.TRASH));
                });

                addButton.getStyleClass().add("addButton");
                inboxButton.getStyleClass().add("appButton");
                starredButton.getStyleClass().add("appButton");
                sentButton.getStyleClass().add("appButton");
                draftsButton.getStyleClass().add("appButton");
                moreButton.getStyleClass().add("appButton");
                allMailButton.getStyleClass().add("appButton");
                trashButton.getStyleClass().add("deleteButton");

                VBox extraButtonsBox = new VBox(10, allMailButton, trashButton);
                extraButtonsBox.setAlignment(Pos.CENTER);
                extraButtonsBox.setVisible(false);
                extraButtonsBox.setManaged(false);
                extraButtonsBox.setBorder(new Border(new BorderStroke(Color.web("rgb(243, 245, 224)"),
                                BorderStrokeStyle.SOLID, radiiButtonBox, new BorderWidths(2))));

                extraButtonsBox.setBackground(
                                new Background(new BackgroundFill(Color.WHITESMOKE, radiiButtonBox, Insets.EMPTY)));
                extraButtonsBox.setPadding(new Insets(10));

                moreButton.setOnAction(e -> {
                        boolean isVisible = extraButtonsBox.isVisible();
                        extraButtonsBox.setVisible(!isVisible);
                        extraButtonsBox.setManaged(!isVisible);
                        moreButton.setText(isVisible ? "More" : "Less");
                });

                getChildren().addAll(addButton, inboxButton, starredButton, sentButton, draftsButton, moreButton,
                                extraButtonsBox);
        }
}
