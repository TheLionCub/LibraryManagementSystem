package com.library.application.controllers.abstractions;

import com.library.utils.ControllerUtils;
import com.library.utils.PairKey;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public abstract class AbstractDetailsController<T> extends AbstractController {
    @FXML
    protected ChoiceBox<PairKey<String, String>> filterChoiceBox;
    @FXML
    protected TextField filterTextField;
    @FXML
    public HBox filterHBox;
    @FXML
    protected Button editButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button undoButton;
    @FXML
    private Label readState;
    @FXML
    protected Label alertLabel;

    private boolean editMode = false;
    private String dataMemory = null;

    protected T objectMemory = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<PairKey<String, String>> observableList = FXCollections.observableArrayList();
        observableList.add(SearchFilter.EMPTY.getValue(), new PairKey<>("", ""));
        observableList.add(SearchFilter.ID.getValue(), new PairKey<>("ID", "id"));
        filterChoiceBox.setItems(observableList);

        filterChoiceBox.getSelectionModel().select(SearchFilter.EMPTY.getValue());

        filterTextField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                try {
                    filterButtonAction();
                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                }
                filterTextField.positionCaret(filterTextField.getText().length());
            }
        });

        anchorPane.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.S) {
                    if (editMode) {
                        saveButtonAction();
                    }
                }
                if (keyEvent.getCode() == KeyCode.ESCAPE) {
                    if (editMode) {
                        editButtonAction();
                        disableEditMode();
                        setFieldsData(objectMemory);
                    }
                }
            }
        });

        ControllerUtils.getAllInputControls(anchorPane).forEach(inputControl -> {
            inputControl.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                    if (editMode) {
                        if (dataMemory == null) return;
                        editButtonsToggle(dataMemory.equals(getInputDataContent()));
                    }
                }
            });
        });

        setAllTextInputControlLimit(128);
    }

    public void setFilterID(String id) {
        filterTextField.setText(id);
    }

    @FXML
    protected void clearButtonAction() {
        ControllerUtils.clearAllTextFields(anchorPane);
        disableAllButtons();
        alertLabel.setText(null);
    }

    @FXML
    private void undoButtonAction() {
        if (dataMemory != null) {
            setFieldsData(objectMemory);
        }
        alertLabel.setText(null);
    }

    @FXML
    private void onFilterTextChange() {
        disableAllButtons();
    }

    @FXML
    private void editButtonAction() {
        if (editMode) {
            setReadonlyMode();
        } else {
            setEditMode();
        }

        editMode = !editMode;

        editModeAvailableFieldsToggle(editMode);

        if (editMode) {
            dataMemory = getInputDataContent();
        }
    }

    public void disableEditMode() {
        editMode = false;
        editModeAvailableFieldsToggle(false);
        editButtonsToggle(true);
        setReadonlyMode();
    }

    private void editButtonsToggle(boolean toggle) {
        saveButton.setDisable(toggle);
        undoButton.setDisable(toggle);
        editButton.setDisable(!toggle);
    }

    protected String getInputDataContent() {
        ArrayList<TextInputControl> textInputControls = ControllerUtils.getAllInputControls(anchorPane);

        StringBuilder content = new StringBuilder();

        for (TextInputControl textInputControl : textInputControls) {
            if (textInputControl.getText() != null) {
                content.append(textInputControl.getText());
            }
        }

        return content.toString();
    }

    protected void editModeAvailableFieldsToggle(boolean toggle) {
        filterHBox.setDisable(toggle);
        filterTextField.setEditable(!toggle);
    }

    @FXML
    protected abstract void saveButtonAction();

    @FXML
    protected abstract void filterButtonAction() throws SQLException;

    @FXML
    protected abstract void disableAllButtons();

    protected abstract void clearAllFields();

    private void setEditMode() {
        readState.setText(resourceBundle.getString("scene.bookDetails.editMode"));
        editButton.setText(resourceBundle.getString("scene.bookDetails.readModeLabel"));
    }

    private void setReadonlyMode() {
        readState.setText(resourceBundle.getString("scene.bookDetails.readMode"));
        editButton.setText(resourceBundle.getString("scene.bookDetails.editModeLabel"));
    }

    public void setFieldsData(T t) {
        objectMemory = t;

        editButton.setDisable(false);
        alertLabel.setVisible(false);
    }

    public boolean isEditMode() {
        return editMode;
    }

    protected void setNewMemoryObject(T t) {
        setFieldsData(t);
        dataMemory = getInputDataContent();
        editButtonsToggle(true);
    }

    public String getDataMemory() {
        return dataMemory;
    }

    public T getObjectMemory() {
        return objectMemory;
    }

    public void setFilterType(SearchFilter searchFilter) {
        filterChoiceBox.getSelectionModel().select(searchFilter.value);
    }

    public enum SearchFilter {
        EMPTY(0),
        ID(1);

        private final int value;

        SearchFilter(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
