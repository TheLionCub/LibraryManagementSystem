package com.library.application.controllers;

import com.library.application.controllers.abstractions.AbstractTableController;
import com.library.application.controllers.main.BookDetailsController;
import com.library.application.controllers.main.MemberDetailsController;
import com.library.application.scene.loaders.elements.BookDetailsScene;
import com.library.application.scene.loaders.elements.MemberDetailsScene;
import com.library.application.scene.loaders.elements.ReturnBookScene;
import com.library.data.model.Book;
import com.library.data.model.Debtor;
import com.library.data.model.Member;
import com.library.utils.CommonUtils;
import com.library.utils.DateTimeUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DebtorsController extends AbstractTableController<Debtor> {
    @FXML
    private DatePicker datePicker;
    @FXML
    private TableColumn<Debtor, String> memberColumn;
    @FXML
    private TableColumn<Debtor, String> bookColumn;
    @FXML
    private TableColumn<Debtor, String> rentDeadline;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        datePicker.setValue(LocalDate.now());

        tableView.setRowFactory(new Callback<TableView<Debtor>, TableRow<Debtor>>() {
            @Override
            public TableRow<Debtor> call(TableView<Debtor> debtorTableView) {
                TableRow<Debtor> tableRow = new TableRow<>();

                ContextMenu contextMenu = new ContextMenu();
                MenuItem memberItem = new MenuItem("Детали участника");
                MenuItem bookItem = new MenuItem("Детали книги");
                MenuItem returnBook = new MenuItem("Вернуть книгу");

                tableRow.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if (mouseEvent.getButton() == MouseButton.SECONDARY && tableRow.getItem() != null) {
                            contextMenu.getItems().clear();
                            contextMenu.getItems().add(memberItem);
                            contextMenu.getItems().add(bookItem);
                            contextMenu.getItems().add(returnBook);
                            tableRow.contextMenuProperty().bind(Bindings
                                    .when(tableRow.emptyProperty())
                                    .then((ContextMenu) null)
                                    .otherwise(contextMenu));
                        }
                    }
                });

                memberItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        try {
                            MemberDetailsScene memberDetailsScene = MemberDetailsScene.load();
                            MemberDetailsController memberDetailsController = memberDetailsScene.getFxmlLoader().getController();
                            memberDetailsController.setFieldsData(tableRow.getItem().getMember());
                            memberDetailsScene.getStage().show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                bookItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        try {
                            BookDetailsScene bookDetailsScene = BookDetailsScene.load();
                            BookDetailsController bookDetailsController = bookDetailsScene.getFxmlLoader().getController();
                            bookDetailsController.setFieldsData(tableRow.getItem().getBook());
                            bookDetailsScene.getStage().show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                returnBook.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        try {
                            ReturnBookScene returnBookScene = ReturnBookScene.loadWithData(tableRow.getItem().getMember(), tableRow.getItem().getBook());
                            returnBookScene.getStage().show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                return tableRow;
            }
        });

        try {
            setData();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    @FXML
    private void setData() throws SQLException {
        LocalDate localDate = datePicker.getValue();
        if (localDate == null) {
            return;
        }

        ObservableList<Debtor> observableList = FXCollections.observableArrayList();

        Timestamp selectedTimestamp = Timestamp.valueOf(localDate.atStartOfDay());

        ResultSet resultSet = databaseHandler.executeQuery("SELECT * FROM books WHERE rent_deadline <= " + "'" + selectedTimestamp.getTime() + "'");
        while (resultSet.next()) {
            String bookID = resultSet.getString("id");
            String memberID = resultSet.getString("member_id");
            ArrayList<Member> members = CommonUtils.getMembersData("SELECT * FROM members WHERE id='" + memberID + "'");
            ArrayList<Book> books = CommonUtils.getBooksData("SELECT * FROM books WHERE id='" + bookID + "'");
            if (!members.isEmpty() && !books.isEmpty()) {
                Member member = members.get(0);
                Book book = books.get(0);
                observableList.add(new Debtor(member, book, book.getRentDeadline()));
            }
        }

        tableView.setItems(observableList);
    }

    @Override
    protected void initializeColumns() {
        memberColumn.setCellValueFactory(debtorStringCellDataFeatures -> new SimpleStringProperty(debtorStringCellDataFeatures.getValue().getMember().toString()));
        bookColumn.setCellValueFactory(debtorStringCellDataFeatures -> new SimpleStringProperty(debtorStringCellDataFeatures.getValue().getBook().toString()));
        rentDeadline.setCellValueFactory(debtorStringCellDataFeatures -> new SimpleStringProperty(DateTimeUtils.timestampToFormattedString(debtorStringCellDataFeatures.getValue().getRentDeadline())));
    }
}
