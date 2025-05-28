package com.example.components;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private static final Map<String, String> EXTENSION_ICON_MAP = Map.ofEntries(
            Map.entry("doc", "/icons/icons8-microsoft-word-file.png"),
            Map.entry("docx", "/icons/icons8-microsoft-word-file.png"), Map.entry("xls", "/icons/icons8-xls-file.png"),
            Map.entry("xlsx", "/icons/icons8-xls-file.png"), Map.entry("ppt", "/icons/icons8-ppt-file.png"),
            Map.entry("pptx", "/icons/icons8-ppt-file.png"), Map.entry("txt", "/icons/icons8-txt-file.png"),
            Map.entry("rtf", "/icons/icons8-rtf-file.png"), Map.entry("bmp", "/icons/icons8-bmp-file.png"),
            Map.entry("tiff", "/icons/icons8-tiff-file.png"), Map.entry("webp", "/icons/icons8-webp-file.png"),
            Map.entry("mp4", "/icons/icons8-video-file.png"), Map.entry("mov", "/icons/icons8-video-file.png"),
            Map.entry("avi", "/icons/icons8-video-file.png"), Map.entry("wmv", "/icons/icons8-video-file.png"),
            Map.entry("mp3", "/icons/icons8-mp3-file.png"), Map.entry("wav", "/icons/icons8-wav-file.png"),
            Map.entry("m4a", "/icons/icons8-m4a-file.png"), Map.entry("zip", "/icons/icons8-zip-file.png"),
            Map.entry("7z", "/icons/icons8-7z-file.png"), Map.entry("tar", "/icons/icons8-tar-file.png"));

    private static final List<String> SUPPORTED_EXTENSIONS = List.of("*.doc", "*.docx", "*.xls", "*.xlsx", "*.ppt",
            "*.pptx", "*.pdf", "*.txt", "*.rtf", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp", "*.tiff", "*.webp",
            "*.mp4", "*.mov", "*.avi", "*.wmv", "*.mp3", "*.wav", "*.m4a", "*.zip", "*.7z", "*.tar");

    private Button attachButton = new Button("Attach Files");
    private HBox fileBox = new HBox(5);
    private List<File> selectedFiles = null;

    private void getNoImageFileIcon(ImageView icon, File file) {
        String name = file.getName().toLowerCase();
        int dotIndex = name.lastIndexOf(".");

        if (dotIndex != -1 && dotIndex < name.length() - 1) {
            String ext = name.substring(dotIndex + 1);
            String iconPath = EXTENSION_ICON_MAP.get(ext);

            if (iconPath != null) {
                icon.setImage(new Image(getClass().getResourceAsStream(iconPath)));
            } else {
                icon.setImage(new Image(getClass().getResourceAsStream("/icons/icons8-default-file.png")));
            }
        } else {
            icon.setImage(new Image(getClass().getResourceAsStream("/icons/icons8-default-file.png")));
        }
    }

    private VBox createFilePreview(File file) {
        ImageView icon = new ImageView();
        icon.setFitWidth(50);
        icon.setFitHeight(50);

        if (file.getName().toLowerCase().matches(".*\\.(jpg|png|gif|jpeg)")) {
            Image image = new Image(file.toURI().toString(), 50, 50, true, true);
            icon.setImage(image);
        } else {
            getNoImageFileIcon(icon, file);
        }

        Label fileNameLabel = new Label(file.getName());

        Button removeButton = new Button("✖");
        removeButton.getStyleClass().add("deleteButton");
        removeButton.setStyle("-fx-font-size: 10px; -fx-padding: 2px 4px;");

        VBox filePreview = new VBox(10, removeButton, icon, fileNameLabel);
        filePreview.setPadding(new Insets(5));
        filePreview.setAlignment(Pos.CENTER);
        removeButton.setOnAction(e -> {
            selectedFiles = new ArrayList(selectedFiles);
            selectedFiles.remove(file);
            fileBox.getChildren().remove(filePreview);
        });

        return filePreview;
    }

    public FileChooserUI(Label attachedFilesErrorLabel, MessageController messageController) {
        attachButton.getStyleClass().add("appButton");
        attachButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Files to Attach");

            fileChooser.getExtensionFilters()
                    .add(new FileChooser.ExtensionFilter("Supported files", SUPPORTED_EXTENSIONS));

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
        scrollPane.getStyleClass().add("files-field");

        VBox attachFileBox = new VBox(attachButton);
        attachFileBox.setAlignment(Pos.CENTER);

        fileBox.getChildren().addListener((javafx.collections.ListChangeListener<javafx.scene.Node>) change -> {
            attachFileBox.getChildren().clear();

            if (!fileBox.getChildren().isEmpty()) {
                attachFileBox.getChildren().addAll(scrollPane, attachButton);
                VBox.setMargin(attachButton, new Insets(10, 0, 0, 0));
                attachedFilesErrorLabel.setText("");
                messageController.clearError("file");
            } else {
                attachFileBox.getChildren().add(attachButton);
                attachedFilesErrorLabel.setText("");
                messageController.clearError("file");
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
