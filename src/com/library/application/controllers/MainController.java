package com.library.application.controllers;

import com.library.application.controllers.abstractions.AbstractController;
import com.library.application.controllers.main.BookSearchController;
import com.library.application.controllers.main.MemberSearchController;
import com.library.application.scene.loaders.elements.SettingsScene;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public final class MainController extends AbstractController {
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab bookDetailsTab;
    @FXML
    private Tab memberDetailsTab;

    @FXML
    private MainController mainController;
    @FXML
    private BookSearchController bookSearchController;
    @FXML
    private MemberSearchController memberSearchController;

    private static MainController mainControllerI;

    public static MainController getInstance() {
        return mainControllerI;
    }

    public TabPane getTabPane() {
        return tabPane;
    }

    public Tab getBookDetailsTab() {
        return bookDetailsTab;
    }

    public Tab getMemberDetailsTab() {
        return memberDetailsTab;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mainControllerI = this;

        bookSearchController.injectMainController(this);
        memberSearchController.injectMainController(this);
    }

    @FXML
    private void handleLogoutButton() {
        logout();
    }

    @FXML
    private void handleSettingsButton() throws IOException {
        SettingsScene settingsScene = SettingsScene.load();
        settingsScene.getStage().show();
    }
}
