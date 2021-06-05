package com.library.application.controllers;

import com.library.application.controllers.abstractions.AbstractIssueReturnBookController;
import com.library.application.controllers.main.BookDetailsController;
import com.library.application.controllers.main.BookSearchController;
import com.library.application.scene.loaders.components.alertLabel.AlertLabel;
import com.library.application.scene.loaders.components.alertLabel.enums.AlertLabelType;
import com.library.data.model.Book;
import com.library.data.model.Member;
import com.library.utils.CommonUtils;
import com.library.utils.DateTimeUtils;
import com.library.utils.PairKey;
import com.library.utils.StringUtils;
import com.library.worker.admin.AdminActionsLogger;
import com.library.worker.admin.enums.LogTarget;
import com.library.worker.admin.enums.LogType;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public final class IssueBookController extends AbstractIssueReturnBookController {
    private static final int maxBooksPerRequest = 10;

    @FXML
    protected ChoiceBox<PairKey<String, String>> filterChoiceBoxBook;
    @FXML
    private ListView<Book> selectedBooks;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        filterChoiceBoxBook.setItems(CommonUtils.getBookSearchFilters());
        filterChoiceBoxBook.getSelectionModel().select(BookDetailsController.BookSearchFilter.SERIAL.getValue());

        filterTextFieldBook.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                findBooks();
            }
        });

        datePicker.setDayCellFactory(datePicker1 -> new DateCell() {
            @Override
            public void updateItem(LocalDate localDate, boolean empty) {
                super.updateItem(localDate, empty);
                LocalDate now = LocalDate.now();

                setDisable(empty || localDate.compareTo(now) < 0);
            }
        });

        selectedBooks.setCellFactory(bookListView -> new ListCell<Book>() {
            @Override
            protected void updateItem(Book book, boolean empty) {
                super.updateItem(book, empty);

                if (empty || book == null) {
                    setText(null);
                } else {
                    setText(book.getTitle() + " [" + book.getSerial() + "]");
                }

                ContextMenu contextMenu = new ContextMenu();
                MenuItem removeItem = new MenuItem(resourceBundle.getString("button.remove"));

                contextMenu.getItems().add(removeItem);

                removeItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        removeSelectedBookFromList();
                    }
                });

                contextMenuProperty().setValue(contextMenu);
            }
        });

        selectedBooks.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.DELETE) {
                if (selectedBooks.getSelectionModel().getSelectedItem() != null) {
                    selectedBooks.getItems().remove(selectedBooks.getSelectionModel().getSelectedItem());
                }
            }
        });

        bookTableView.setRowFactory(bookTableView1 -> {
            TableRow<Book> bookTableRow = new TableRow<>();

            ContextMenu contextMenu = new ContextMenu();
            MenuItem selectItem = new MenuItem(resourceBundle.getString("button.select"));

            contextMenu.getItems().add(selectItem);

            selectItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    addBookToList(bookTableRow.getItem());
                }
            });

            bookTableRow.contextMenuProperty().setValue(contextMenu);
            return bookTableRow;
        });
    }

    @FXML
    private void findBooks() {
        if (filterTextFieldBook.getText() == null || filterTextFieldBook.getText().isEmpty()) {
            return;
        }

        try {
            ArrayList<Book> bookArrayList;
            if (BookDetailsController
                    .getFilterType(filterChoiceBoxBook.getSelectionModel().getSelectedIndex())
                    .getValue() == BookDetailsController.BookSearchFilter.TITLE.getValue()) {
                bookArrayList = CommonUtils.getBooksData("SELECT * FROM books WHERE title LIKE '%" + filterTextFieldBook.getText() + "%' COLLATE utf8_general_ci");
            } else {
                bookArrayList = CommonUtils.getFilteredBooks(filterTextFieldBook, filterChoiceBoxBook);
            }
            bookTableView.setItems(FXCollections.observableList(bookArrayList.stream().filter(Book::getAvailable).collect(Collectors.toList())));
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    private void addSelectedBookToList() {
        addBookToList(bookTableView.getSelectionModel().getSelectedItem());
    }

    private void addBookToList(Book book) {
        if (book != null) {
            if (selectedBooks.getItems().size() > maxBooksPerRequest) {
                new AlertLabel(alertLabel, MessageFormat.format(resourceBundle.getString("controller.issueBook.maxBooksPerRequest"), maxBooksPerRequest), AlertLabelType.ERROR);
            } else {
                if (!selectedBooksHasID(book)) {
                    selectedBooks.getItems().add(book);
                } else {
                    new AlertLabel(alertLabel, MessageFormat.format(resourceBundle.getString("controller.issueBook.alreadySelected"), book.getSerial()), AlertLabelType.ERROR);
                }
            }
        }
    }

    @FXML
    private void addBookToListButton() {
        addSelectedBookToList();
    }

    private boolean selectedBooksHasID(Book book) {
        return selectedBooks.getItems().stream().anyMatch(b -> b.getId().equals(book.getId()));
    }

    @FXML
    private void removeSelectedBookFromList() {
        if (selectedBooks.getSelectionModel().getSelectedItem() != null) {
            selectedBooks.getItems().remove(selectedBooks.getSelectionModel().getSelectedItem());
        }
    }

    @FXML
    protected void clearAllFields() {
        super.clearAllFields();
        filterTextFieldBook.setText(null);
        selectedBooks.getItems().clear();
    }

    @Override
    protected void bookTableViewOnClicked() {
        addSelectedBookToList();
    }

    @FXML
    private void issueBook() throws SQLException {
        String notesText = StringUtils.trimIfNotNull(notes.getText());

        if (selectedMember == null) {
            new AlertLabel(alertLabel, resourceBundle.getString("controller.issueBook.noSelectedMember"), AlertLabelType.ERROR);
            return;
        }

        if (selectedBooks.getItems().size() == 0) {
            new AlertLabel(alertLabel, resourceBundle.getString("controller.issueBook.noSelectedBook"), AlertLabelType.ERROR);
            return;
        }

        LocalDate localDate = datePickerValueToLocalDate(datePicker);

        if (localDate == null) {
            new AlertLabel(alertLabel, resourceBundle.getString("controller.issueBook.noSelectedReturnDate"), AlertLabelType.ERROR);
            return;
        }

        Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());
        Timestamp selectedTimestamp = Timestamp.valueOf(localDate.atStartOfDay());

        if (localDate.compareTo(LocalDate.now()) < 0) {
            new AlertLabel(alertLabel, resourceBundle.getString("controller.issueBook.dateCompareError"), AlertLabelType.ERROR);
            return;
        }

        if (selectedTimestamp.getTime() - nowTimestamp.getTime() > DateTimeUtils.yearMS) {
            new AlertLabel(alertLabel, resourceBundle.getString("controller.issueBook.maxIssueTerm"), AlertLabelType.ERROR);
            return;
        }

        ArrayList<String> memberBookIDList = new ArrayList<>();
        ResultSet resultSet = databaseHandler.executeQuery("SELECT * FROM books WHERE member_id='" + selectedMember.getId() + "'");
        while (resultSet.next()) {
            String bookID = resultSet.getString("id");
            memberBookIDList.add(bookID);
        }

        for (Book book : selectedBooks.getItems()) {
            if (book == null) continue;

            if (memberBookIDList.contains(book.getId().toString())) {
                new AlertLabel(alertLabel, MessageFormat.format(resourceBundle.getString("controller.issueBook.memberAlreadyHasBook"), book.getTitle(), book.getSerial()), AlertLabelType.ERROR);
                return;
            }

            databaseHandler.executeUpdate("UPDATE books SET member_id='" + selectedMember.getId() + "', rent_deadline='" + selectedTimestamp.getTime() + "', rent_notes='" + notesText + "' WHERE id='" + book.getId() + "'");

            Book overwrittenBook = new Book(book.getId(), book.getSerial(), book.getTitle(), book.getAuthor(), book.getDescription(),
                    book.getCategory(), book.getLanguage(), book.getPublishYear(), book.getPublishHouse(), book.getPublishCity(),
                    book.getPages(), selectedMember.getId().toString(), selectedTimestamp.getTime(), notesText, book.getCondition(),
                    book.getNotes(), book.getCreated(), book.getLastUpdated());

            if (BookDetailsController.getInstance().getObjectMemory() != null) {
                if (BookDetailsController.getInstance().getObjectMemory().getId().equals(overwrittenBook.getId())) {
                    BookDetailsController.getInstance().setFieldsData(overwrittenBook);
                }
            }

            BookSearchController.getInstance().overwriteItem(overwrittenBook);

            AdminActionsLogger.newLog(LogType.ISSUE, LogTarget.BOOK,
                    MessageFormat.format(resourceBundle.getString("log.issueBook"), book.getId(), selectedMember.getId(), localDate.toString()));
        }

        new AlertLabel(alertLabel, MessageFormat.format(resourceBundle.getString("controller.issueBook.issued"), selectedBooks.getItems().size(), selectedMember.getFullName(), selectedMember.getId()), AlertLabelType.SUCCESS);

        clearAllFields();
    }

    public void setMemberData(Member member) {
        selectMember(member);
    }

    public void setBookData(Book book) {
        addBookToList(book);
    }
}
