package com.library.utils;

import com.library.application.controllers.MainController;
import com.library.application.controllers.abstractions.AbstractDetailsController;
import com.library.application.controllers.main.BookDetailsController;
import com.library.application.controllers.main.MemberDetailsController;
import com.library.data.model.Book;
import com.library.data.model.Member;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;

public class ControllerUtils {
    public static void setDefaultTableColumnsWidth(TableView<?> tableView) {
        final double tableWidth = tableView.getWidth();

        ObservableList<? extends TableColumn<?, ?>> tableColumns = tableView.getColumns();

        final double defaultColumnWidth = (tableWidth - tableColumns.size()) / tableColumns.size();

        for (TableColumn<?, ?> tableColumn : tableColumns) {
            tableColumn.setPrefWidth(TextField.USE_COMPUTED_SIZE);
            tableColumn.setPrefWidth(defaultColumnWidth);
        }
    }

    public static boolean textFieldsUnicodeMatches(AnchorPane anchorPane) {
        ArrayList<TextInputControl> textInputControls = getAllInputControls(anchorPane);

        for (TextInputControl inputControl : textInputControls) {
            if (inputControl.getText().length() > 0) {
                if (!inputControl.getText().matches(CommonUtils.unicodeMatcher)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static ArrayList<TextInputControl> getAllInputControls(AnchorPane anchorPane) {
        ArrayList<TextInputControl> textInputControls = new ArrayList<>();

        ObservableList<Node> nodes = FXCollections.observableArrayList();
        getAllNodes(anchorPane, nodes);
        for (Node node : nodes) {
            if (node instanceof TextField || node instanceof TextArea) {
                textInputControls.add((TextInputControl) node);
            }
        }

        return textInputControls;
    }

    private static void getAllNodes(Parent parent, ObservableList<Node> nodes) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            nodes.add(node);
            if (node instanceof Parent) {
                getAllNodes((Parent) node, nodes);
            }
        }
    }

    public static ObservableList<Node> getAllNodes(Parent parent) {
        ObservableList<Node> nodes = FXCollections.observableArrayList();
        getAllNodes(parent, nodes);
        return nodes;
    }

    public static ArrayList<PairKey<String, String>> getChoiceBoxNotEmptyItems(ChoiceBox<PairKey<String, String>> choiceBox) {
        ObservableList<PairKey<String, String>> choiceBoxItems = choiceBox.getItems();
        ArrayList<PairKey<String, String>> arrayList = new ArrayList<>();

        for (PairKey<String, String> pairKey : choiceBoxItems) {
            if (pairKey.getKey() != null && !pairKey.getKey().isEmpty()) {
                arrayList.add(pairKey);
            }
        }

        return arrayList;
    }

    public static String[] getNotEmptyKeys(ChoiceBox<PairKey<String, String>> choiceBox) {
        return getChoiceBoxNotEmptyItems(choiceBox)
                .stream()
                .map(PairKey::getKey)
                .toArray(String[]::new);
    }

    public static void clearAllTextFields(AnchorPane anchorPane) {
        getAllInputControls(anchorPane).forEach(inputControl -> inputControl.setText(null));
    }

    public static void redirectToBookDetails(Book book) {
        SingleSelectionModel<Tab> singleSelectionModel = MainController.getInstance().getTabPane().getSelectionModel();
        singleSelectionModel.select(MainController.getInstance().getBookDetailsTab());

        BookDetailsController bookDetailsController = BookDetailsController.getInstance();
        if (bookDetailsController.isEditMode()) {
            bookDetailsController.disableEditMode();
        }

        bookDetailsController.setFilterType(BookDetailsController.BookSearchFilter.ID);
        bookDetailsController.setFilterID(String.valueOf(book.getId()));
        bookDetailsController.setFieldsData(book);
    }

    public static void redirectToMemberDetails(Member member) {
        SingleSelectionModel<Tab> singleSelectionModel = MainController.getInstance().getTabPane().getSelectionModel();
        singleSelectionModel.select(MainController.getInstance().getMemberDetailsTab());

        MemberDetailsController bookDetailsController = MemberDetailsController.getInstance();
        if (bookDetailsController.isEditMode()) {
            bookDetailsController.disableEditMode();
        }

        bookDetailsController.setFilterType(AbstractDetailsController.SearchFilter.ID);
        bookDetailsController.setFilterID(String.valueOf(member.getId()));
        bookDetailsController.setFieldsData(member);
    }
}
