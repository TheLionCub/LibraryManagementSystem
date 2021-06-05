package com.library.application.controllers.abstractions;

import com.javafx.custom.components.DBTableColumn;
import com.javafx.custom.components.LibTooltip;
import com.library.data.RowItem;
import com.library.managers.windows.CHMManager;
import com.library.utils.ControllerUtils;
import com.library.utils.DatabaseUtils;
import com.library.utils.PairKey;
import com.library.utils.StringUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;

import javax.persistence.Column;
import javax.swing.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public abstract class AbstractPaginationTableController<T extends RowItem> extends AbstractTableController<T> {
    private static final int maxRows = 1000;

    private final Class<T> type;

    @FXML
    protected ChoiceBox<PairKey<String, Integer>> displayRows;
    @FXML
    protected Pagination pagination;
    @FXML
    protected TextField pageField;
    @FXML
    protected TextField filterField;
    @FXML
    protected CheckBox strictSearch;
    @FXML
    protected CheckBox searchEverywhere;
    @FXML
    protected Button selectPageButton;
    @FXML
    private HBox tableFieldsHBox;

    protected ArrayList<T> data;

    protected int rows = DisplayItemsKey.FIFTY.value;

    @SuppressWarnings("unchecked")
    public AbstractPaginationTableController() {
        Type type = getClass().getGenericSuperclass();

        while (!(type instanceof ParameterizedType) || ((ParameterizedType) type).getRawType() != AbstractPaginationTableController.class) {
            if (type instanceof ParameterizedType) {
                type = ((Class<?>) ((ParameterizedType) type).getRawType()).getGenericSuperclass();
            } else {
                type = ((Class<?>) type).getGenericSuperclass();
            }
        }

        this.type = (Class<T>) ((ParameterizedType) type).getActualTypeArguments()[0];
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        displayRows.setItems(getDisplayRowsFilters());

        displayRows.getSelectionModel().select(0);

        new LibTooltip(strictSearch, resourceBundle.getString("tooltip.clickToOpenFAQ"));
        new LibTooltip(searchEverywhere, resourceBundle.getString("tooltip.clickToOpenFAQ"));

        showHelpListener(strictSearch, "QuerySearch");
        showHelpListener(searchEverywhere, "SearchEverywhere");

        searchEverywhere.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                updateSearchState();
            }
        });

        displayRows.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (displayRows.getSelectionModel().getSelectedItem() != null) {
                    int key = displayRows.getSelectionModel().getSelectedItem().getKey();
                    if (key == DisplayItemsKey.CUSTOM.value) {
                        displayRows.getSelectionModel().select(null);

                        displayRows.hide();

                        String value = JOptionPane.showInputDialog(null, MessageFormat.format(resourceBundle.getString("alert.displayRows"), maxRows));
                        if (value == null || value.isEmpty()) {
                            return;
                        }

                        try {
                            rows = Integer.parseInt(value);
                            if (rows <= 0) {
                                JOptionPane.showMessageDialog(null, resourceBundle.getString("alert.lessOrNull"), null, JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            if (rows > maxRows) {
                                JOptionPane.showMessageDialog(null, MessageFormat.format(resourceBundle.getString("alert.maxRowsPerPage"), maxRows), null, JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        } catch (NumberFormatException numberFormatException) {
                            JOptionPane.showMessageDialog(null, resourceBundle.getString("alert.notANumber"), null, JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        displayRows.getSelectionModel().select(new PairKey<>(String.valueOf(rows), rows));
                    } else {
                        rows = displayRows.getSelectionModel().getSelectedItem().getKey();
                    }

                    ObservableList<T> observableList = FXCollections.observableList(data);

                    if (rows > observableList.size()) {
                        rows = observableList.size();
                    }

                    pagination.setPageCount(getPages());
                    createPage(0);
                }
            }
        });

        pageField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                selectPage();
            }
        });

        addHeaderFilterFields();

        getDbTableColumns(tableView.getColumns()).forEach(dbTableColumn -> {
            dbTableColumn.getSearchFieldBind().setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent keyEvent) {
                    if (keyEvent.getCode() == KeyCode.ENTER) {
                        searchItems();
                    }
                }
            });

            dbTableColumn.getSearchFieldBind().textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                    if (getDbEmptyColumns(getDbTableColumns(tableView.getColumns())).size() == tableView.getColumns().size()) {
                        clearSearch();
                    }
                }
            });
        });

        filterField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    searchItems();
                }
            }
        });

        updateSearchState();
        setTextInputControlLimit(filterField, 128);
    }

    protected abstract void addRestMenuItems(ContextMenu contextMenu, T item);

    protected abstract void redirectToDetails(T t);

    protected abstract String getDbTableName();

    protected void setDefaultTableFactory() {
        tableView.setRowFactory(new Callback<TableView<T>, TableRow<T>>() {
            @Override
            public TableRow<T> call(TableView<T> tableView) {
                TableRow<T> tableRow = new TableRow<>();

                ContextMenu contextMenu = new ContextMenu();
                MenuItem editItem = new MenuItem(resourceBundle.getString("button.edit"));

                tableRow.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2 && !tableRow.isEmpty()) {
                            redirectToDetails(tableRow.getItem());
                        }
                        if (mouseEvent.getButton() == MouseButton.SECONDARY && tableRow.getItem() != null) {
                            contextMenu.getItems().clear();
                            contextMenu.getItems().add(editItem);
                            addRestMenuItems(contextMenu, tableRow.getItem());
                            tableRow.contextMenuProperty().bind(Bindings
                                    .when(tableRow.emptyProperty())
                                    .then((ContextMenu) null)
                                    .otherwise(contextMenu));
                        }
                    }
                });

                editItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        redirectToDetails(tableRow.getItem());
                    }
                });

                return tableRow;
            }
        });
    }

    private void showHelpListener(Control control, String path) {
        control.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                    CHMManager.loadWithURL(path);
                }
            }
        });
    }

    private void updateSearchState() {
        getDbTableColumns(tableView.getColumns()).forEach(dbTableColumn -> {
            dbTableColumn.getSearchFieldBind().setDisable(searchEverywhere.isSelected());
        });

        filterField.setDisable(!searchEverywhere.isSelected());
    }

    protected void lateInit() {
        setDefaultPagination();

        pagination.currentPageIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                createPage(pagination.getCurrentPageIndex());
            }
        });
    }

    public <G extends RowItem> G getRowItem(RowItem t, TableView<G> tableView) {
        return tableView
                .getItems()
                .stream()
                .filter(item -> item.getId().equals(t.getId()))
                .findFirst()
                .orElse(null);
    }

    public <G extends RowItem> void addItem(G item, TableView<G> tableView) {
        tableView.getItems().add(item);

        if (tableView.getItems().size() > rows) {
            pagination.setCurrentPageIndex(pagination.getPageCount() - 1);
        }
    }

    public <G extends RowItem> void removeItem(G item, TableView<G> tableView) {
        G rowItem = getRowItem(item, tableView);

        tableView.getItems().remove(rowItem);

        if (tableView.getItems().size() == 0) {
            if (pagination.getPageCount() > pagination.getCurrentPageIndex() + 1) {
                pagination.setCurrentPageIndex(pagination.getCurrentPageIndex() + 1);
            } else {
                pagination.setCurrentPageIndex(0);
            }
        }
    }

    public <G extends RowItem> void overwriteItem(G item, TableView<G> tableView) {
        G rowItem = getRowItem(item, tableView);

        tableView.getItems().remove(rowItem);
        tableView.getItems().add(item);
    }

    public void addItem(T t) {
        addItem(t, tableView);
        data.add(t);

        checkTableItems();
    }

    public void removeItem(T t) {
        T rowItem = getRowItem(t, tableView);
        removeItem(t, tableView);
        data.remove(rowItem);

        checkTableItems();
    }

    public void overwriteItem(T t) {
        T rowItem = getRowItem(t, tableView);

        overwriteItem(t, tableView);

        data.remove(rowItem);
        data.add(t);

        checkTableItems();
    }

    public void setItems(ArrayList<T> arrayList) {
        tableView.getItems().clear();

        for (T item : arrayList) {
            addItem(item, tableView);
        }
    }

    protected void checkTableItems() {
        boolean isEmpty = data.size() == 0;

        pagination.setPageCount(getPages());
        if (isEmpty) {
            if (pagination.getPageCount() > pagination.getCurrentPageIndex() + 1) {
                pagination.setCurrentPageIndex(pagination.getCurrentPageIndex() + 1);
            }
        }

        pagination.setDisable(isEmpty);
        selectPageButton.setDisable(isEmpty);
        displayRows.setDisable(isEmpty);
        pageField.setDisable(isEmpty);
    }

    @FXML
    protected void searchItems() {
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM " + getDbTableName());

        ObservableList<TableColumn<T, ?>> tableColumns = tableView.getColumns();
        ArrayList<DBTableColumn<?, ?>> dbTableColumns = getDbTableColumns(tableColumns);

        if (searchEverywhere.isSelected()) {
            if (filterField.getText() == null || filterField.getText().isEmpty()) {
                return;
            }

            query.append(" WHERE ");

            String[] keys = dbTableColumns.stream().map(DBTableColumn::getDbPropertyName).toArray(String[]::new);
            String searchText = StringUtils.trimIfNotNull(filterField.getText());

            query.append(DatabaseUtils.getAnyColumnQueryPart(searchText, keys, strictSearch.isSelected()));
        } else {
            List<DBTableColumn<?, ?>> notEmptyTableColumns = getDbNotEmptyTableColumns(dbTableColumns);
            if (!notEmptyTableColumns.isEmpty()) {
                query.append(" WHERE ");
            }

            int index = 0;
            for (DBTableColumn<?, ?> dbTableColumn : notEmptyTableColumns) {
                if (dbTableColumn.getSearchFieldBind().getText() != null && !dbTableColumn.getSearchFieldBind().getText().isEmpty()) {
                    if (strictSearch.isSelected()) {
                        query.append(dbTableColumn.getDbPropertyName() + "='" + dbTableColumn.getSearchFieldBind().getText() + "'");
                    } else {
                        query.append(dbTableColumn.getDbPropertyName() + " LIKE '%" + dbTableColumn.getSearchFieldBind().getText() + "%'");
                    }
                }
                if (index + 1 < notEmptyTableColumns.size()) {
                    query.append(" AND ");
                }
                index++;
            }
        }

        try {
            setFieldFilterDataClass(query.toString());
        } catch (SQLException ignored) {
        }
    }

    protected abstract void setFieldFilterDataClass(String query) throws SQLException;

    private List<DBTableColumn<?, ?>> getDbNotEmptyTableColumns(ArrayList<DBTableColumn<?, ?>> dbTableColumns) {
        return dbTableColumns.stream()
                .filter(dbTableColumn -> dbTableColumn.getSearchFieldBind().getText() != null
                        && !dbTableColumn.getSearchFieldBind().getText().isEmpty()
                        && dbTableColumn.getDbPropertyName() != null)
                .collect(Collectors.toList());
    }

    private List<DBTableColumn<?, ?>> getDbEmptyColumns(ArrayList<DBTableColumn<?, ?>> dbTableColumns) {
        return dbTableColumns.stream()
                .filter(dbTableColumn -> dbTableColumn.getSearchFieldBind().getText() == null
                        || dbTableColumn.getSearchFieldBind().getText().isEmpty())
                .collect(Collectors.toList());
    }

    private ArrayList<DBTableColumn<?, ?>> getDbTableColumns(ObservableList<TableColumn<T, ?>> tableColumns) {
        ArrayList<DBTableColumn<?, ?>> arrayList = new ArrayList<>();

        for (TableColumn<T, ?> tableColumn : tableColumns) {
            DBTableColumn<?, ?> dbTableColumn = (DBTableColumn<?, ?>) tableColumn;
            if (dbTableColumn.getSearchFieldBind() != null) {
                setTextInputControlLimit(dbTableColumn.getSearchFieldBind(), 32);
                arrayList.add(dbTableColumn);
            }
        }

        return arrayList;
    }

    @FXML
    private void clearSearch() {
        createPage(0);

        filterField.clear();
        getDbTableColumns(tableView.getColumns()).forEach(dbTableColumn -> {
            if (dbTableColumn.getSearchFieldBind() != null) {
                dbTableColumn.getSearchFieldBind().clear();
            }
        });
    }

    private void setDatabaseProperties(TableColumn<T, ?> tableColumn) {
        if (tableColumn.getCellValueFactory() != null) {
            String propertyName = null;
            if (tableColumn.getCellValueFactory() instanceof PropertyValueFactory) {
                propertyName = ((PropertyValueFactory) tableColumn.getCellValueFactory()).getProperty();
            } else {
                if (tableColumn.getCellValueFactory() instanceof SimpleStringProperty) {
                    propertyName = ((SimpleStringProperty) tableColumn.getCellValueFactory()).getName();
                }
            }
            if (propertyName != null) {
                try {
                    Column columnName = type
                            .getDeclaredField(propertyName)
                            .getAnnotation(Column.class);
                    if (columnName != null) {
                        ((DBTableColumn<?, ?>) tableColumn).setDbPropertyName(columnName.name());
                    }
                } catch (NoSuchFieldException ignored) {
                }
            }
        }
    }

    protected int getPages() {
        return (int) Math.ceil(data.size() * 1.0 / rows);
    }

    protected void setDefaultPagination() {
        pagination.setCurrentPageIndex(0);
        createPage(0);
        pagination.setPageCount(getPages());
    }

    protected void createPage(int page) {
        int fromIndex = page * rows;
        int toIndex = Math.min(fromIndex + rows, data.size());
        if (data.size() > 0) {
            tableView.setItems(FXCollections.observableArrayList(data.subList(fromIndex, toIndex)));
        }
    }

    @FXML
    private void setDefaultTableColumnsWidth() {
        ControllerUtils.setDefaultTableColumnsWidth(tableView);
    }

    @FXML
    private void selectPage() {
        if (pageField.getText() == null || pageField.getText().isEmpty()) {
            return;
        }

        int page;
        try {
            page = Integer.parseInt(pageField.getText());
            if (page <= 0) {
                return;
            }
        } catch (NumberFormatException numberFormatException) {
            pageField.clear();
            return;
        }

        int pageCount = pagination.getPageCount();
        if (page > pageCount) {
            return;
        }

        pagination.setCurrentPageIndex(page - 1);
    }

    private void addHeaderFilterFields() {
        ObservableList<TableColumn<T, ?>> tableColumns = tableView.getColumns();
        int index = 0;
        for (TableColumn<T, ?> tableColumn : tableColumns) {
            createTextField(tableColumn, index);
            index++;
        }
    }

    private void createTextField(TableColumn<T, ?> tableColumn, int index) {
        TextField textField = new TextField();

        textField.setPromptText(tableColumn.getText());

        HBox.setHgrow(textField, Priority.ALWAYS);
        textField.setPrefHeight(20);

        setDatabaseProperties(tableColumn);
        ((DBTableColumn<?, ?>) tableColumn).setSearchFieldBind(textField);

        tableColumn.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                textField.setPrefWidth(t1.doubleValue());
            }
        });

        tableFieldsHBox.getChildren().add(index, textField);
    }

    private ObservableList<PairKey<String, Integer>> getDisplayRowsFilters() {
        ObservableList<PairKey<String, Integer>> observableList = FXCollections.observableArrayList();

        observableList.add(new PairKey<>(String.valueOf(DisplayItemsKey.FIFTY.value), DisplayItemsKey.FIFTY.value));
        observableList.add(new PairKey<>(String.valueOf(DisplayItemsKey.HUNDRED.value), DisplayItemsKey.HUNDRED.value));
        observableList.add(new PairKey<>(String.valueOf(DisplayItemsKey.F_HUNDRED.value), DisplayItemsKey.F_HUNDRED.value));
        observableList.add(new PairKey<>(resourceBundle.getString("text.other"), DisplayItemsKey.CUSTOM.value));

        return observableList;
    }

    public enum DisplayItemsKey {
        CUSTOM(-1),
        FIFTY(50),
        HUNDRED(100),
        F_HUNDRED(500);

        private final int value;

        DisplayItemsKey(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}