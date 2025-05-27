package com.example.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import javafx.scene.image.Image;

public class FileConvertor {

    private FileConvertor() {
    }

    public static String imageToBase64(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static Image Base64ToImage(String base64) {

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
}
