package com.library.application.controllers.abstractions;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

public abstract class AbstractTableController<T> extends AbstractController {
    @FXML
    protected TableView<T> tableView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeColumns();
    }

    protected abstract void initializeColumns();
}
