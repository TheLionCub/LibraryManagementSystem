package com.library.application.scene.loaders.components.alertLabel;

import com.javafx.custom.components.LibTooltip;
import com.library.LibraryBaseAbstract;
import com.library.application.scene.loaders.components.alertLabel.enums.AlertLabelType;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class AlertLabel extends LibraryBaseAbstract {
    private static final String successColor = "247D2C";
    private static final String warningColor = "CC9900";
    private static final String errorColor = "FF0000";

    private final Label label;
    private final String text;
    private final AlertLabelType alertLabelType;
    private long deleteAfter = 0;

    public AlertLabel(Label label, String text, AlertLabelType alertLabelType) {
        this.label = label;
        this.text = text;
        this.alertLabelType = alertLabelType;

        initialize();
    }

    public AlertLabel(Label label, String text, AlertLabelType alertLabelType, long deleteAfter) {
        this.label = label;
        this.text = text;
        this.alertLabelType = alertLabelType;
        this.deleteAfter = deleteAfter;

        initialize();
    }

    public AlertLabel initialize() {
        label.setText(text);

        switch (alertLabelType) {
            case SUCCESS -> label.setTextFill(Paint.valueOf(successColor));
            case WARNING -> label.setTextFill(Paint.valueOf(warningColor));
            case ERROR -> label.setTextFill(Paint.valueOf(errorColor));
        }

        if (!label.isVisible()) {
            label.setVisible(true);
        }

        label.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                    label.setText(null);
                    label.setVisible(false);
                }
            }
        });

        new LibTooltip(label, resourceBundle.getString("tooltip.clickToHide"), 0.5);

        if (deleteAfter > 0) {
            CompletableFuture.delayedExecutor(deleteAfter, TimeUnit.SECONDS).execute(() -> {
                if (label.getText() != null && label.getText().equals(text)) {
                    Platform.runLater(() -> {
                        label.setText(null);
                        label.setVisible(false);
                    });
                }
            });
        }

        return this;
    }
}