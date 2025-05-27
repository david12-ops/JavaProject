package com.example.components;

import java.io.File;
import java.util.List;

import com.example.controller.MessageController;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class FileChooserUI extends VBox {
    private static final List<String> SUPPORTED_EXTENSIONS = List.of("*.doc", "*.docx", "*.xls", "*.xlsx", "*.ppt",
            "*.pptx", "*.pdf", "*.txt", "*.rtf", "*.odt", "*.ods", "*.odp", "*.jpg", "*.jpeg", "*.png", "*.gif",
            "*.bmp", "*.tiff", "*.webp", "*.mp4", "*.mov", "*.avi", "*.wmv", "*.mp3", "*.wav", "*.m4a", "*.zip", "*.7z",
            "*.tar", "*.gz");
    private Button attachButton = new Button("Attach Files");
    private HBox fileBox = new HBox(5);
    private List<File> selectedFiles = null;

    private VBox createFilePreview(File file) {
        ImageView icon = new ImageView();
        icon.setFitWidth(35);
        icon.setFitHeight(35);

        if (file.getName().toLowerCase().matches(".*\\.(jpg|png|gif|jpeg)")) {
            Image image = new Image(file.toURI().toString(), 35, 35, true, true);
            icon.setImage(image);
        } else {
            // TODO - need to have png for privew like world etc...
            icon.setImage(new Image(getClass().getResourceAsStream("/icons/file.png")));
        }

        Label fileNameLabel = new Label(file.getName());

        Button removeButton = new Button("✖");

        VBox filePreview = new VBox(10, removeButton, icon, fileNameLabel);
        filePreview.setPadding(new Insets(5));
        filePreview.setAlignment(Pos.CENTER);
        removeButton.setOnAction(e -> fileBox.getChildren().remove(filePreview));

        return filePreview;
    }

    public FileChooserUI(Label attachedFilesErrorLabel, MessageController messageController) {
        attachButton.getStyleClass().add("appButton");
        attachButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Files to Attach");

            fileChooser.getExtensionFilters()
                    .add(new FileChooser.ExtensionFilter("Supported files", SUPPORTED_EXTENSIONS));

            selectedFiles = fileChooser.showOpenMultipleDialog(null);

            List<File> selected = fileChooser.showOpenMultipleDialog(null);

            if (selected != null && !selected.isEmpty()) {
                if (selected.size() > 5) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("File Limit Exceeded");
                    alert.setHeaderText(null);
                    alert.setContentText("You can attach up to 5 files only.");
                    alert.showAndWait();

                    selectedFiles = selected.subList(0, 5);
                } else {
                    selectedFiles = selected;
                }

                fileBox.getChildren().clear();
                for (File file : selectedFiles) {
                    VBox filePreview = createFilePreview(file);
                    fileBox.getChildren().add(filePreview);
                }
            }
        });

        fileBox.setAlignment(Pos.CENTER);

        ScrollPane scrollPane = new ScrollPane(fileBox);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(false);
        scrollPane.setPrefHeight(200);
        scrollPane.setMaxWidth(400);

        VBox attachFileBox = new VBox(attachButton);
        attachFileBox.setAlignment(Pos.CENTER);

        fileBox.getChildren().addListener((javafx.collections.ListChangeListener<javafx.scene.Node>) change -> {
            attachFileBox.getChildren().clear();

            if (!fileBox.getChildren().isEmpty()) {
                attachedFilesErrorLabel.setText("");
                messageController.clearError("file");
                attachFileBox.getChildren().addAll(scrollPane, attachButton);
                VBox.setMargin(attachButton, new Insets(10, 0, 0, 0));
            } else {
                attachedFilesErrorLabel.setText("");
                messageController.clearError("file");
                attachFileBox.getChildren().add(attachButton);
            }
        });

        this.getChildren().add(attachFileBox);
        this.setAlignment(Pos.CENTER);
    }

    public List<File> getSelectedFiles() {
        return selectedFiles;
    }

    public void clearFileBox() {
        fileBox.getChildren().clear();
    }
}
