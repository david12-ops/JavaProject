package com.example.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.UUID;

import org.apache.tika.Tika;

import javafx.scene.image.Image;

public class FileConvertor {

    private FileConvertor() {
    }

    public static String fileToBase64(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static Image base64ToImage(String base64) {
        if (base64 == null || base64.trim().isEmpty()) {
            return null;
        }

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64);
            return new Image(new ByteArrayInputStream(decodedBytes));
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid Base64 string: " + e.getMessage());
            return null;
        }
    }

    private static String mimeTypeToExtension(String mimeType) {
        if (mimeType == null)
            return null;

        switch (mimeType) {
        case "application/pdf":
            return "pdf";
        case "image/jpeg":
            return "jpg";
        case "image/png":
            return "png";
        case "text/plain":
            return "txt";
        case "application/zip":
            return "zip";
        case "application/msword":
            return "doc";
        case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
            return "docx";
        case "application/vnd.ms-excel":
            return "xls";
        case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
            return "xlsx";
        case "application/vnd.ms-powerpoint":
            return "ppt";
        case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
            return "pptx";
        default:
            return null;
        }
    }

    private static String detectMimeType(byte[] data) {
        Tika tika = new Tika();
        return tika.detect(data);
    }

    public static File base64ToFile(String base64, File outputDirectory) throws IOException {
        byte[] decodedBytes = Base64.getDecoder().decode(base64);

        String mimeType = detectMimeType(decodedBytes);

        String extension = mimeTypeToExtension(mimeType);
        if (extension == null) {
            extension = "bin";
        }

        String filename = "attached_file_" + UUID.randomUUID() + "." + extension;
        File outputFile = new File(outputDirectory, filename);
        Files.write(outputFile.toPath(), decodedBytes);

        return outputFile;
    }
}
