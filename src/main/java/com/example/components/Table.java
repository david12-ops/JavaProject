package com.example.components;

import java.time.LocalDateTime;

import com.example.controller.MessageController;
import com.example.model.Message;
import com.example.model.UserToken;
import com.example.utils.enums.MessageStatus;

import javafx.scene.layout.HBox;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;

public class Table extends HBox {
    private Label createLabel(String displayText) {
        Label colLabel = new Label(displayText);
        colLabel.setStyle(
                "-fx-background-color: #D9D89F;-fx-text-fill: black;-fx-font-weight: bold; -fx-padding: 10 15 10 15;");
        return colLabel;
    }

    private void styleTable(TableView<Message> table) {
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
            protected void updateItem(Message item, boolean empty) {
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

    public Table(MessageController messageController, UserToken userToken, MessageStatus messageStatus) {
        TableView<Message> table = new TableView<>();
        styleTable(table);

        TableColumn<Message, String> subjectCol = new TableColumn<>();
        subjectCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSubject()));
        subjectCol.setGraphic(createLabel("Subject"));
        subjectCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Message, String> messageCol = new TableColumn<>();
        messageCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMessage()));
        messageCol.setGraphic(createLabel("Message"));
        messageCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Message, String> timestampCol = new TableColumn<>();
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

        TableColumn<Message, Void> actionsCol = new TableColumn<>();
        actionsCol.setCellFactory(col -> new TableCell<Message, Void>() {
            private final Button removeButton = new Button("Remove");

            {
                removeButton.getStyleClass().add("deleteButton");
                removeButton.setStyle("-fx-padding: 10px 15px; -fx-font-size: 12px;");

                removeButton.setOnAction(e -> {
                    messageController.removeMessage(getTableView().getItems().get(getIndex()));
                    getTableView().getItems().remove(getIndex());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });
        actionsCol.setStyle("-fx-alignment: CENTER;");
        actionsCol.setMaxWidth(125);
        actionsCol.setMinWidth(100);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.getColumns().addAll(subjectCol, messageCol, timestampCol, actionsCol);
        table.setItems(FXCollections.observableArrayList(messageController.getMessages(messageStatus, userToken)));

        this.getChildren().add(table);
        this.setAlignment(Pos.CENTER);
    }
}
