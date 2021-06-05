package com.library.application.controllers;

import com.library.application.controllers.abstractions.AbstractIssueReturnBookController;
import com.library.application.controllers.main.BookDetailsController;
import com.library.application.controllers.main.BookSearchController;
import com.library.application.scene.loaders.components.alertLabel.AlertLabel;
import com.library.application.scene.loaders.components.alertLabel.enums.AlertLabelType;
import com.library.data.model.Book;
import com.library.data.model.Member;
import com.library.worker.admin.AdminActionsLogger;
import com.library.worker.admin.enums.LogTarget;
import com.library.worker.admin.enums.LogType;
import com.library.utils.CommonUtils;
import com.library.utils.DateTimeUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

public final class ReturnBookController extends AbstractIssueReturnBookController {
    @FXML
    private TableColumn<Book, String> rentDeadlineColumn;
    @FXML
    private TextField selectedBookField;

    private Book selectedBook;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        bookTableView.setRowFactory(new Callback<TableView<Book>, TableRow<Book>>() {
            @Override
            public TableRow<Book> call(TableView<Book> bookTableView) {
                TableRow<Book> tableRow = new TableRow<>();

                ContextMenu contextMenu = new ContextMenu();
                MenuItem selectItem = new MenuItem(resourceBundle.getString("button.select"));

                contextMenu.getItems().add(selectItem);

                selectItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        selectBook(bookTableView.getSelectionModel().getSelectedItem());
                    }
                });

                tableRow.contextMenuProperty().setValue(contextMenu);
                return tableRow;
            }
        });

        datePicker.setDayCellFactory(datePicker1 -> new DateCell() {
            @Override
            public void updateItem(LocalDate localDate, boolean b) {
                super.updateItem(localDate, b);

                setDisable(true);
            }
        });
    }

    public void selectMember(Member member) {
        super.selectMember(member);
        if (member != null) {
            try {
                selectBook(null);
                getMemberHoldBooks();
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        }
    }

    private void getMemberHoldBooks() throws SQLException {
        ArrayList<Book> bookArrayList = CommonUtils.getBooksData("SELECT * FROM books WHERE member_id='" + selectedMember.getId() + "'");
        bookTableView.setItems(FXCollections.observableList(bookArrayList));
    }

    @FXML
    private void selectBookFromTable() {
        selectBook(bookTableView.getSelectionModel().getSelectedItem());
    }

    public void selectBook(Book book) {
        if (book != null) {
            selectedBook = book;
            selectedBookField.setText(book.getSerial() + " [" + book.getTitle() + "]");
            datePicker.setValue(DateTimeUtils.timestampToLocalDate(book.getRentDeadline()));
            notes.setText(book.getRentNotes());
        } else {
            selectedBook = null;
            selectedBookField.clear();
            datePicker.setValue(null);
            notes.clear();
        }
    }

    @FXML
    private void returnBook() throws SQLException {
        if (selectedMember == null) {
            new AlertLabel(alertLabel, resourceBundle.getString("controller.returnBook.noSelectedMember"), AlertLabelType.ERROR);
            return;
        }

        if (selectedBook == null) {
            new AlertLabel(alertLabel, resourceBundle.getString("controller.returnBook.noSelectedBook"), AlertLabelType.ERROR);
            return;
        }

        ResultSet resultSet = databaseHandler.executeQuery("SELECT * FROM books WHERE id='" + selectedBook.getId() + "' AND member_id='" + selectedMember.getId() + "'");
        if (!resultSet.next()) {
            new AlertLabel(alertLabel, resourceBundle.getString("controller.returnBook.memberHasNoBook"), AlertLabelType.ERROR);
            return;
        }

        databaseHandler.executeUpdate("UPDATE books SET member_id=NULL, rent_deadline=NULL, rent_notes=NULL WHERE id='" + selectedBook.getId() + "'");

        Book overwrittenBook = new Book(selectedBook.getId(), selectedBook.getSerial(), selectedBook.getTitle(), selectedBook.getAuthor(), selectedBook.getDescription(),
                selectedBook.getCategory(), selectedBook.getLanguage(), selectedBook.getPublishYear(), selectedBook.getPublishHouse(), selectedBook.getPublishCity(),
                selectedBook.getPages(), null, null, null, selectedBook.getCondition(),
                selectedBook.getNotes(), selectedBook.getCreated(), selectedBook.getLastUpdated());

        if (BookDetailsController.getInstance().getObjectMemory() != null) {
            if (BookDetailsController.getInstance().getObjectMemory().getId().equals(overwrittenBook.getId())) {
                BookDetailsController.getInstance().setFieldsData(overwrittenBook);
            }
        }

        BookSearchController.getInstance().overwriteItem(overwrittenBook);

        new AlertLabel(alertLabel, MessageFormat.format(resourceBundle.getString("controller.returnBook.bookReturned"), selectedBook.getTitle(), selectedBook.getSerial()), AlertLabelType.SUCCESS);

        AdminActionsLogger.newLog(LogType.RETURN, LogTarget.BOOK, MessageFormat.format(resourceBundle.getString("log.returnBook"), selectedBook.getId(), selectedMember.getId()));

        clearAllFields();
    }

    @FXML
    protected void clearAllFields() {
        super.clearAllFields();
        selectedBookField.clear();
        selectedBook = null;
    }

    @Override
    protected void bookTableViewOnClicked() {
        selectBook(bookTableView.getSelectionModel().getSelectedItem());
    }

    protected void initializeColumns() {
        super.initializeColumns();
        rentDeadlineColumn.setCellValueFactory(bookStringCellDataFeatures -> new SimpleStringProperty(DateTimeUtils.timestampToFormattedString(bookStringCellDataFeatures.getValue().getRentDeadline())));
    }
}
