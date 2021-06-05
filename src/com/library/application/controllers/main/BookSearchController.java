package com.library.application.controllers.main;

import com.javafx.custom.components.DBTableColumn;
import com.javafx.custom.components.LibTooltip;
import com.library.application.controllers.IssueBookController;
import com.library.application.controllers.MainController;
import com.library.application.controllers.ReturnBookController;
import com.library.application.controllers.abstractions.AbstractPaginationTableController;
import com.library.application.scene.loaders.elements.IssueBookScene;
import com.library.application.scene.loaders.elements.ReturnBookScene;
import com.library.cache.ControllersCache;
import com.library.data.model.Book;
import com.library.utils.CommonUtils;
import com.library.utils.ControllerUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public final class BookSearchController extends AbstractPaginationTableController<Book> {
    @FXML
    public TableView<Book> tableView;
    @FXML
    private DBTableColumn<Book, Integer> idColumn;
    @FXML
    private DBTableColumn<Book, String> serialColumn;
    @FXML
    private DBTableColumn<Book, String> titleColumn;
    @FXML
    private DBTableColumn<Book, String> authorColumn;
    @FXML
    private DBTableColumn<Book, String> categoryColumn;
    @FXML
    private DBTableColumn<Book, String> languageColumn;
    @FXML
    private DBTableColumn<Book, String> publishHouseColumn;
    @FXML
    private DBTableColumn<Book, Integer> publishYearColumn;
    @FXML
    private DBTableColumn<Book, String> publishCityColumn;
    @FXML
    private DBTableColumn<Book, Integer> pagesColumn;
    @FXML
    private DBTableColumn<Book, Boolean> availableColumn;

    public static BookSearchController getInstance() {
        return ControllersCache.bookSearchController;
    }

    private MainController mainController;

    public void injectMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        if (ControllersCache.bookSearchController == null) {
            ControllersCache.bookSearchController = this;
        }

        setDefaultTableFactory();

        try {
            data = CommonUtils.getBooksData("SELECT * FROM books");
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        lateInit();
        checkTableItems();

        setAvailableRowColor();

        new LibTooltip(availableColumn.getSearchFieldBind(), resourceBundle.getString("controller.bookSearch.existsOrNot"));
    }

    @Override
    protected void addRestMenuItems(ContextMenu contextMenu, Book item) {
        MenuItem issueBookItem;
        if (item.getAvailable()) {
            issueBookItem = new MenuItem(resourceBundle.getString(BookDetailsController.IssueButtonStatements.ISSUE.getStatement()));
        } else {
            issueBookItem = new MenuItem(resourceBundle.getString(BookDetailsController.IssueButtonStatements.RETURN.getStatement()));
        }

        contextMenu.getItems().add(issueBookItem);

        issueBookItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (item.getAvailable()) {
                    try {
                        IssueBookScene issueBookScene = IssueBookScene.load();

                        IssueBookController issueBookController = issueBookScene.getFxmlLoader().getController();
                        issueBookController.setBookData(item);

                        issueBookScene.getStage().show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    ReturnBookScene returnBookScene = null;
                    try {
                        returnBookScene = ReturnBookScene.load();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (returnBookScene != null) {
                        ReturnBookController returnBookController = returnBookScene.getFxmlLoader().getController();
                        try {
                            returnBookController.selectMember(getBookHandler(item));
                        } catch (SQLException sqlException) {
                            sqlException.printStackTrace();
                        }
                        returnBookController.selectBook(item);

                        returnBookScene.getStage().show();
                    }
                }
            }
        });
    }

    @Override
    protected void redirectToDetails(Book book) {
        ControllerUtils.redirectToBookDetails(book);
    }

    @Override
    public void addItem(Book book) {
        super.addItem(book);
        setAvailableRowColor();
    }

    @Override
    public void removeItem(Book book) {
        super.removeItem(book);
        setAvailableRowColor();
    }

    @Override
    public void overwriteItem(Book book) {
        super.overwriteItem(book);
        setAvailableRowColor();
    }

    @FXML
    protected void searchItems() {
        super.searchItems();
        setAvailableRowColor();
    }

    @Override
    protected void setFieldFilterDataClass(String query) throws SQLException {
        ArrayList<Book> arrayList = CommonUtils.getBooksData(query);

        if (availableColumn.getSearchFieldBind().getText() != null && !availableColumn.getSearchFieldBind().getText().isEmpty()) {
            switch (availableColumn.getSearchFieldBind().getText().toLowerCase()) {
                case "да", "true", "yes", "правда", "+" -> arrayList = arrayList.stream().filter(Book::getAvailable).collect(Collectors.toCollection(ArrayList::new));
                case "нет", "false", "no", "ложь", "-" -> arrayList = arrayList.stream().filter(book -> !book.getAvailable()).collect(Collectors.toCollection(ArrayList::new));
            }
        }

        setItems(arrayList);
    }

    public void setAvailableRowColor() {
        availableColumn.setCellFactory(bookBooleanTableColumn -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean aBoolean, boolean b) {
                super.updateItem(aBoolean, b);

                if (b || aBoolean == null) return;

                if (aBoolean) {
                    setStyle(BookDetailsController.availableColor);
                } else {
                    setStyle(BookDetailsController.unavailableColor);
                }
            }
        });
    }

    @Override
    protected void createPage(int page) {
        super.createPage(page);
        setAvailableRowColor();
    }

    @Override
    protected void initializeColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        serialColumn.setCellValueFactory(new PropertyValueFactory<>("serial"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        languageColumn.setCellValueFactory(new PropertyValueFactory<>("language"));
        publishYearColumn.setCellValueFactory(new PropertyValueFactory<>("publishYear"));
        publishHouseColumn.setCellValueFactory(new PropertyValueFactory<>("publishHouse"));
        publishCityColumn.setCellValueFactory(new PropertyValueFactory<>("publishCity"));
        pagesColumn.setCellValueFactory(new PropertyValueFactory<>("pages"));
        availableColumn.setCellValueFactory(new PropertyValueFactory<>("available"));
    }

    @Override
    protected String getDbTableName() {
        return "books";
    }
}
