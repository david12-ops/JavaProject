package com.example.utils.interfaces;

import java.io.File;
import java.util.List;

public interface MessageValidator {
    boolean validFiles(List<File> files);

    boolean validMessageData(String whom, String subject, String message);
}
