package com.library.application.controllers.settings;

import com.javafx.custom.components.DBTableColumn;
import com.library.application.controllers.abstractions.AbstractPaginationTableController;
import com.library.data.model.Change;
import com.library.utils.CommonUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public final class ChangesController extends AbstractPaginationTableController<Change> {
    @FXML
    private DBTableColumn<Change, Integer> idColumn;
    @FXML
    private DBTableColumn<Change, Integer> executorIDColumn;
    @FXML
    private DBTableColumn<Change, String> executorColumn;
    @FXML
    private DBTableColumn<Change, String> typeColumn;
    @FXML
    private DBTableColumn<Change, String> targetColumn;
    @FXML
    private DBTableColumn<Change, String> detailsColumn;
    @FXML
    private DBTableColumn<Change, String> datetimeColumn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        try {
            data = CommonUtils.getChangesData("SELECT * FROM admin_actions");
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        lateInit();
        checkTableItems();
    }

    @Override
    protected void addRestMenuItems(ContextMenu contextMenu, Change item) {
    }

    @Override
    protected void redirectToDetails(Change change) {
    }

    @Override
    protected void setFieldFilterDataClass(String query) throws SQLException {
        ArrayList<Change> arrayList = CommonUtils.getChangesData(query);

        setItems(arrayList);
    }

    @Override
    protected void initializeColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        executorIDColumn.setCellValueFactory(new PropertyValueFactory<>("executorID"));
        executorColumn.setCellValueFactory(changeStringCellDataFeatures -> new SimpleStringProperty(changeStringCellDataFeatures.getValue().getFullName() + " (" + changeStringCellDataFeatures.getValue().getUsername() + ")"));
        typeColumn.setCellValueFactory(changeStringCellDataFeatures -> new SimpleStringProperty(resourceBundle.getString(changeStringCellDataFeatures.getValue().getType().getText())));
        targetColumn.setCellValueFactory(changeStringCellDataFeatures -> new SimpleStringProperty(resourceBundle.getString(changeStringCellDataFeatures.getValue().getTarget().getText())));
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));
        datetimeColumn.setCellValueFactory(new PropertyValueFactory<>("datetime"));
    }

    @Override
    protected String getDbTableName() {
        return "admin_actions";
    }
}
