package com.javafx.custom.components;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.*;
import java.util.stream.Collectors;

public class AutocompleteTextInputControl {
    private final TextInputControl textInputControl;

    private final SortedSet<String> entries = new TreeSet<>();
    private final ContextMenu contextMenu = new ContextMenu();

    private final ArrayList<String> defaultItems;

    private static final int maxEntries = 10;

    public AutocompleteTextInputControl(TextInputControl textInputControl, ArrayList<String> defaultItems) {
        this.textInputControl = textInputControl;
        this.defaultItems = defaultItems;

        addListener();
    }

    private void addListener() {
        textInputControl.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                entries.addAll(defaultItems);
                if (textInputControl.isEditable()) {
                    if (textInputControl.getText() == null || textInputControl.getText().isEmpty()) {
                        showAllItems();
                    } else {
                        List<String> filteredEntries = entries.stream()
                                .filter(e -> e.toLowerCase().contains(textInputControl.getText().toLowerCase()))
                                .collect(Collectors.toList());
                        if (!filteredEntries.isEmpty()) {
                            populatePopup(filteredEntries, textInputControl.getText());
                            if (!contextMenu.isShowing()) {
                                contextMenu.show(textInputControl, Side.BOTTOM, 0, 0);
                            }
                        } else {
                            contextMenu.hide();
                        }
                    }
                }
            }
        });

        textInputControl.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                showAllItems();
            }
        });
    }

    private void showAllItems() {
        if (textInputControl.getText() == null || textInputControl.getText().isEmpty()) {
            if (textInputControl.isEditable()) {
                populatePopup(new ArrayList<>(entries), "");
                if (!contextMenu.isShowing()) {
                    contextMenu.show(textInputControl, Side.BOTTOM, 0, 0);
                }
            }
        }
    }

    private void populatePopup(List<String> filteredEntries, String search) {
        List<CustomMenuItem> customMenuItems = new LinkedList<>();
        int count = Math.min(filteredEntries.size(), maxEntries);

        for (int i = 0; i < count; i++) {
            final String text = filteredEntries.get(i);
            Label label = new Label();
            if (!search.isEmpty()) {
                label.setGraphic(buildTextFlow(text, search));
            } else {
                label.setGraphic(new TextFlow(new Text(text)));
            }
            label.setPrefHeight(10);
            CustomMenuItem customMenuItem = new CustomMenuItem(label, true);
            customMenuItems.add(customMenuItem);
            customMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    textInputControl.setText(text);
                    textInputControl.positionCaret(text.length());
                    contextMenu.hide();
                }
            });
        }

        contextMenu.getItems().clear();
        contextMenu.getItems().addAll(customMenuItems);
        entries.clear();
    }

    private static TextFlow buildTextFlow(String text, String filter) {
        int filterIndex = text.toLowerCase().indexOf(filter.toLowerCase());
        Text textBefore = new Text(text.substring(0, filterIndex));
        Text textAfter = new Text(text.substring(filterIndex + filter.length()));
        Text textFilter = new Text(text.substring(filterIndex, filterIndex + filter.length()));
        textFilter.setFill(Color.ORANGE);
        textFilter.setFont(Font.font("HELVETICA", FontWeight.BOLD, 12));
        return new TextFlow(textBefore, textFilter, textAfter);
    }

    public SortedSet<String> getEntries() {
        return entries;
    }
}
