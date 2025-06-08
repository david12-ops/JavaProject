package com.example.utils.interfaces;

import javafx.scene.control.Label;

public interface GuiErrorHelper {
    Label createErrorLabel();

    void clearErrorLabels(Label... labels);

    void showIfError(String error, Label label);
}
