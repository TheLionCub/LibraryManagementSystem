package com.library.application.scene.loaders.elements;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public final class LoadingLock {
    private final AnchorPane anchorPane;
    private final StackPane stackPane;

    private static final double height = 50;
    private static final double width = 50;

    private boolean isEnabled = false;

    public LoadingLock(AnchorPane anchorPane) {
        this.anchorPane = anchorPane;

        ImageView imageView = new ImageView();
        imageView.setImage(new Image(getClass().getResourceAsStream("/resources/images/loading.gif")));
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);

        double x_center = anchorPane.getWidth() / 2 - width / 2;
        double y_center = anchorPane.getHeight() / 2 - height / 2;

        stackPane = new StackPane();

        stackPane.setLayoutX(x_center);
        stackPane.setLayoutY(y_center);

        AnchorPane.setTopAnchor(stackPane, x_center);
        AnchorPane.setBottomAnchor(stackPane, x_center);
        AnchorPane.setLeftAnchor(stackPane, y_center);
        AnchorPane.setRightAnchor(stackPane, y_center);

        stackPane.getChildren().add(imageView);
    }

    public void enable() {
        if (!isEnabled) {
            anchorPane.getScene().getRoot().setDisable(true);
            anchorPane.getChildren().add(stackPane);

            isEnabled = true;
        }
    }

    public void disable() {
        if (isEnabled) {
            anchorPane.getScene().getRoot().setDisable(false);
            anchorPane.getChildren().remove(stackPane);

            isEnabled = false;
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }
}
