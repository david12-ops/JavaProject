package com.example.components;

import java.time.LocalDateTime;
import java.util.EnumSet;

import com.example.controller.MessageController;
import com.example.controller.ScreenController;
import com.example.controller.UserController;
import com.example.dto.MessageDTO;
import com.example.model.UserToken;
import com.example.utils.enums.MessageStatus;
import com.example.view.DetailMessageScreen;
import com.example.view.MainScreen;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;

public class Table extends VBox {
    private void updateStatusAction(Stage stage, UserController userController, ScreenController screenController,
            MessageController messageController, MessageDTO messageDTO, MessageStatus newMessageStatus) {
        messageController.updateStatus(userController.getLoggedUser(), messageDTO, newMessageStatus);
        screenController.updateScreen("main", new MainScreen(stage, screenController, userController, messageController,
                EnumSet.of(newMessageStatus)));
        screenController.activate("main", stage);
    }

    private void removeMessageStatus(Stage stage, UserController userController, ScreenController screenController,
            MessageController messageController, MessageDTO messageDTO, MessageStatus messageStatusToRemove) {
        messageController.removeMessageStatus(userController.getLoggedUser(), messageDTO, messageStatusToRemove);
        screenController.updateScreen("main", new MainScreen(stage, screenController, userController, messageController,
                EnumSet.of(messageStatusToRemove)));
        screenController.activate("main", stage);
    }

    private boolean isMessageStatusOneFromUI(EnumSet<MessageStatus> messageStatusesFromUI) {
        return messageStatusesFromUI.size() == 1;
    }

    private HBox createButtonBox(Stage stage, MessageController messageController, UserController userController,
            ScreenController screenController, MessageDTO messageDTO, EnumSet<MessageStatus> messageStatusesFromUI) {
        UserToken userToken = userController.getLoggedUser();
        boolean isTokenFilled = userToken != null && userToken.getUserId() != null;

        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("appButton");
        backButton.setOnAction(e -> {
            screenController.activate("main", stage);
        });

        Button addToFavoritesButton = new Button("Add to favorites");
        addToFavoritesButton.getStyleClass().add("updateButton");
        addToFavoritesButton.setOnAction(e -> {
            updateStatusAction(stage, userController, screenController, messageController, messageDTO,
                    MessageStatus.STARRED);
        });

        Button removeFromFavoritesButton = new Button("Remove from favorites");
        removeFromFavoritesButton.getStyleClass().add("deleteButton");
        removeFromFavoritesButton.setOnAction(e -> {
            removeMessageStatus(stage, userController, screenController, messageController, messageDTO,
                    MessageStatus.STARRED);
        });

        Button renewMessageButton = new Button("Renew message");
        renewMessageButton.getStyleClass().add("updateButton");
        renewMessageButton.setOnAction(e -> {
            removeMessageStatus(stage, userController, screenController, messageController, messageDTO,
                    MessageStatus.TRASH);
        });

        buttonBox.getChildren().add(backButton);

        if (!isTokenFilled) {
            return buttonBox;
        }

        EnumSet<MessageStatus> userStatuses = messageDTO.getStatuses().getOrDefault(userToken.getUserId(),
                EnumSet.noneOf(MessageStatus.class));

        boolean isStarred = userStatuses.contains(MessageStatus.STARRED);
        boolean isInTrash = userStatuses.contains(MessageStatus.TRASH);

        if (isMessageStatusOneFromUI(messageStatusesFromUI) && (messageStatusesFromUI.contains(MessageStatus.INBOX)
                || messageStatusesFromUI.contains(MessageStatus.SENT))) {
            buttonBox.getChildren().add(isStarred ? removeFromFavoritesButton : addToFavoritesButton);
        } else if (isMessageStatusOneFromUI(messageStatusesFromUI)
                && messageStatusesFromUI.contains(MessageStatus.STARRED)) {
            buttonBox.getChildren().add(removeFromFavoritesButton);
        } else if (isMessageStatusOneFromUI(messageStatusesFromUI)
                && messageStatusesFromUI.contains(MessageStatus.TRASH)) {
            buttonBox.getChildren().addAll(isStarred ? removeFromFavoritesButton : addToFavoritesButton,
                    renewMessageButton);
        } else if (!isMessageStatusOneFromUI(messageStatusesFromUI)) {
            if (isInTrash) {
                buttonBox.getChildren().addAll(isStarred ? removeFromFavoritesButton : addToFavoritesButton,
                        renewMessageButton);
            } else {
                buttonBox.getChildren().add(isStarred ? removeFromFavoritesButton : addToFavoritesButton);
            }
        }

        return buttonBox;
    }

    private Label createLabel(String displayText) {
        Label colLabel = new Label(displayText);
        colLabel.setStyle(
                "-fx-background-color: #D9D89F;-fx-text-fill: black;-fx-font-weight: bold; -fx-padding: 10 15 10 15;");
        return colLabel;
    }

    private void styleTable(TableView<MessageDTO> table) {
        table.setStyle(
                "-fx-background-color: white; -fx-background-radius: 15; -fx-border-radius: 15; -fx-border-color: #D9D89F; -fx-border-width: 1; -fx-padding: 10");

        table.setPrefWidth(1100);
        table.setPrefHeight(600);

        table.widthProperty().addListener((obs, oldVal, newVal) -> {
            table.lookupAll(".column-header").forEach(header -> {
                header.setStyle("-fx-background-color: #D9D89F;");
            });
        });

        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(MessageDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    String baseColor = (getIndex() % 2 == 0) ? "#f4f4f4" : "#e9e9e9";
                    if (isSelected()) {
                        baseColor = (getIndex() % 2 == 0) ? "#c0c0c0" : "#b0b0b0";
                    }
                    setStyle("-fx-background-color: " + baseColor + ";");
                }
            };
        });
    }

    private HBox detailButtonBox;

    public Table(Stage stage, ScreenController screenController, MessageController messageController,
            UserController userController, EnumSet<MessageStatus> messageStatusesFromUI) {
        TableView<MessageDTO> table = new TableView<>();
        UserToken userToken = userController.getLoggedUser();
        styleTable(table);

        Label statusLabel = new Label(
                messageStatusesFromUI.size() == 1 ? messageStatusesFromUI.iterator().next().toString() : "All");
        statusLabel.setStyle("-fx-text-fill: #D9D89F;\r\n" + //
                "    -fx-font-size: 30px;\r\n" + //
                "    -fx-padding: 5px 0 10px 0; -fx-font-weight: bold;");

        TableColumn<MessageDTO, String> sender = new TableColumn<>();
        sender.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSenderMailAccount()));
        sender.setGraphic(createLabel("Sender"));
        sender.setStyle("-fx-alignment: CENTER;");

        TableColumn<MessageDTO, String> subjectCol = new TableColumn<>();
        subjectCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSubject()));
        subjectCol.setGraphic(createLabel("Subject"));
        subjectCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<MessageDTO, String> messageCol = new TableColumn<>();
        messageCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMessage()));
        messageCol.setGraphic(createLabel("Message"));
        messageCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<MessageDTO, String> timestampCol = new TableColumn<>();
        timestampCol.setCellValueFactory(data -> {
            LocalDateTime ts = data.getValue().getTimestamp();
            String formatted = ts != null
                    ? ts.getDayOfMonth() + "." + ts.getMonthValue() + "." + ts.getYear() + " " + ts.getHour() + ":"
                            + ts.getMinute()
                    : "";
            return new SimpleStringProperty(formatted);
        });
        timestampCol.setGraphic(createLabel("Sent"));
        timestampCol.setStyle("-fx-alignment: CENTER;");
        timestampCol.setMaxWidth(100);
        timestampCol.setMinWidth(75);

        TableColumn<MessageDTO, Void> actionsCol = new TableColumn<>();
        actionsCol.setCellFactory(col -> new TableCell<MessageDTO, Void>() {
            private final Button removeButton = new Button("Remove");
            private final Button detailButton = new Button("Detail");

            {
                removeButton.getStyleClass().add("deleteButton");
                detailButton.getStyleClass().add("appButton");
                removeButton.setStyle("-fx-padding: 10px 15px; -fx-font-size: 12px;");
                detailButton.setStyle("-fx-padding: 10px 15px; -fx-font-size: 12px;");

                removeButton.setOnAction(e -> {
                    messageController.removeMessage(userToken, getTableView().getItems().get(getIndex()));
                    getTableView().getItems().remove(getIndex());
                });

                detailButton.setOnAction(e -> {
                    detailButtonBox = createButtonBox(stage, messageController, userController, screenController,
                            getTableView().getItems().get(getIndex()), messageStatusesFromUI);
                    screenController.updateScreen("detailMessage", new DetailMessageScreen(stage, messageController,
                            getTableView().getItems().get(getIndex()), detailButtonBox));
                    screenController.activate("detailMessage", stage);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox boxButton = new HBox(10, removeButton, detailButton);
                    boxButton.setAlignment(Pos.CENTER);
                    setGraphic(boxButton);
                }
            }
        });
        actionsCol.setMaxWidth(175);
        actionsCol.setMinWidth(100);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.getColumns().addAll(sender, subjectCol, messageCol, timestampCol, actionsCol);
        table.setItems(
                FXCollections.observableArrayList(messageController.getMessages(messageStatusesFromUI, userToken)));

        this.getChildren().addAll(statusLabel, table);
        this.setAlignment(Pos.CENTER);
    }
}
