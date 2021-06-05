package com.library.application.controllers.settings;

import com.fasterxml.jackson.core.type.TypeReference;
import com.library.LibraryConfig;
import com.library.Main;
import com.library.application.controllers.abstractions.AbstractController;
import com.library.application.scene.loaders.elements.ChangesScene;
import com.library.application.scene.loaders.elements.CreateAdminScene;
import com.library.cache.SessionCache;
import com.library.data.config.ConfigurationImpl;
import com.library.data.config.data.model.Course;
import com.library.i18n.I18nProvider;
import com.library.i18n.LocaleLanguage;
import com.library.managers.windows.TrayManager;
import com.library.worker.admin.AdminActionsLogger;
import com.library.worker.admin.enums.LogTarget;
import com.library.worker.admin.enums.LogType;
import com.library.utils.HashUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.MenuItem;
import javafx.util.Callback;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public final class CommonController extends AbstractController {
    @FXML
    private TreeView<String> treeView;
    @FXML
    private ChoiceBox<LocaleLanguage> languageChoiceBox;

    private static final int maxLettersSize = 10;
    private static final int minCourse = 1;
    private static final int maxCourse = 100;

    private final LibraryConfig<ConfigurationImpl> libraryConfig = Main.getLibraryConfig();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        CoursesTreeItem<String> root = new CoursesTreeItem<>();

        CoursesTreeItem<String> courses = new CoursesTreeItem<>(resourceBundle.getString("controller.settings.common.courses"), TreeItemType.COURSES_LIST);
        libraryConfig.getConfiguration().getCourses().sort(Comparator.comparing(Course::getNumber));
        for (Course course : libraryConfig.getConfiguration().getCourses()) {
            CoursesTreeItem<String> courseTreeView = new CoursesTreeItem<>(course.getNumber().toString(), TreeItemType.COURSE_NUMBER);
            for (String letter : course.getLetters()) {
                courseTreeView.getChildren().add(new CoursesTreeItem<>(letter, TreeItemType.COURSE_LETTER));
            }
            courses.getChildren().add(courseTreeView);
        }

        CoursesTreeItem<String> languages = new CoursesTreeItem<>(resourceBundle.getString("controller.settings.common.languages"), TreeItemType.LANGUAGES_LIST);
        libraryConfig.getConfiguration().getLanguages().sort(Comparator.comparing(c -> c));
        for (String language : libraryConfig.getConfiguration().getLanguages()) {
            languages.getChildren().add(new CoursesTreeItem<>(language, TreeItemType.LANGUAGE));
        }

        CoursesTreeItem<String> categories = new CoursesTreeItem<>(resourceBundle.getString("controller.settings.common.categories"), TreeItemType.CATEGORIES_LIST);
        for (String category : libraryConfig.getConfiguration().getCategories()) {
            categories.getChildren().add(new CoursesTreeItem<>(category, TreeItemType.CATEGORY));
        }

        CoursesTreeItem<String> publishHouses = new CoursesTreeItem<>(resourceBundle.getString("controller.settings.common.publishHouses"), TreeItemType.PUBLISH_HOUSE_LIST);
        for (String publishHouse : libraryConfig.getConfiguration().getPublishHouses()) {
            publishHouses.getChildren().add(new CoursesTreeItem<>(publishHouse, TreeItemType.PUBLISH_HOUSE));
        }

        root.getChildren().add(courses);
        root.getChildren().add(categories);
        root.getChildren().add(languages);
        root.getChildren().add(publishHouses);

        treeView.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
            @Override
            public TreeCell<String> call(TreeView<String> stringTreeView) {
                return new TreeCellImpl();
            }
        });

        treeView.setShowRoot(false);
        treeView.setRoot(root);

        languageChoiceBox.setItems(FXCollections.observableList(I18nProvider.localeLanguages));
        LocaleLanguage selectedLocaleLanguage = languageChoiceBox.getItems().stream().filter(r -> r.getKey().equals(I18nProvider.getInstance().getLocale().getLanguage())).findFirst().orElse(null);
        languageChoiceBox.getSelectionModel().select(selectedLocaleLanguage);

        languageChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<LocaleLanguage>() {
            @Override
            public void changed(ObservableValue<? extends LocaleLanguage> observableValue, LocaleLanguage localeLanguage, LocaleLanguage t1) {
                if (t1 != null) {
                    I18nProvider.getInstance().setLocalization(t1.getKey());
                    TrayManager.sendNotification(resourceBundle.getString("tray.rebootToApply"), TrayIcon.MessageType.INFO);
                    AdminActionsLogger.newLog(LogType.UPDATE, LogTarget.SETTINGS, "Lang.: " + localeLanguage.getKey() + " -> " + t1.getKey());
                }
            }
        });
    }

    @FXML
    private void openChangesWindow() throws IOException {
        ChangesScene.load().getStage().show();
    }

    @FXML
    private void openCreateAdminAccountWindow() throws Exception {
        CreateAdminScene.load().getStage().show();
    }

    @FXML
    private void openDeleteAdminAccountWindow() throws Exception {
        String password = JOptionPane.showInputDialog(null, MessageFormat.format(resourceBundle.getString("controller.subMain.accountDeleteConfirm"), SessionCache.userData.get("fullName")), "", JOptionPane.QUESTION_MESSAGE);

        if (password == null || password.isEmpty()) {
            return;
        }

        String passwordHash = HashUtils.stringToSHA256(password);
        if (!SessionCache.userData.get("passwordHash").equals(passwordHash)) {
            JOptionPane.showMessageDialog(null, resourceBundle.getString("controller.subMain.passwordsNotEqual"), null, JOptionPane.ERROR_MESSAGE);
            return;
        }

        databaseHandler.executeUpdate("DELETE FROM admin_accounts WHERE id='" + SessionCache.userData.get("userID") + "'");
        JOptionPane.showMessageDialog(null, MessageFormat.format(resourceBundle.getString("alert.adminAccountDeleted"), SessionCache.userData.get("fullName")), null, JOptionPane.INFORMATION_MESSAGE);
        AdminActionsLogger.newLog(LogType.DELETE, LogTarget.SYSTEM, resourceBundle.getString("log.adminDeleted"));
        logout();
    }

    private class ObjectMapperManager<T> {
        protected ArrayList<T> data;
        protected List<T> dataList;
        private final String configProperty;

        public ObjectMapperManager(String configProperty) {
            data = (ArrayList<T>) libraryConfig.getConfig().get(configProperty);
            if (dataList == null) {
                dataList = libraryConfig.getObjectMapper().convertValue(data, new TypeReference<>() {
                });
            }
            this.configProperty = configProperty;
        }

        public void addItem(String value, TreeItemType treeItemType) {
            overwriteData();
            treeView.getSelectionModel().getSelectedItem().getChildren().add(new CoursesTreeItem<>(value, treeItemType));
            treeView.getSelectionModel().getSelectedItem().getChildren().setAll(treeView.getSelectionModel().getSelectedItem().getChildren().sorted());
        }

        public void removeItem() {
            overwriteData();
            treeView.getSelectionModel().getSelectedItem().getParent().getChildren().remove(treeView.getSelectionModel().getSelectedItem());
        }

        protected void overwriteData() {
            ((ArrayList<T>) libraryConfig.getConfig().get(configProperty)).clear();
            ((ArrayList<T>) libraryConfig.getConfig().get(configProperty)).addAll(dataList);
            writeData();
        }

        public void writeData() {
            try {
                libraryConfig.getObjectMapper().writeValue(libraryConfig.getConfigFile(), libraryConfig.getConfig());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public List<T> getDataList() {
            return dataList;
        }
    }

    private class CourseObjectManager extends ObjectMapperManager<Course> {
        private Course course;

        public CourseObjectManager(boolean newWrite, boolean parent) {
            super("courses");
            dataList = libraryConfig.getObjectMapper().convertValue(data, new TypeReference<>() {
            });
            if (!newWrite) {
                if (parent) {
                    course = dataList.stream().filter(c -> c.getNumber().equals(Integer.valueOf(treeView.getSelectionModel().getSelectedItem().getParent().getValue()))).findFirst().orElse(null);
                } else {
                    course = dataList.stream().filter(c -> c.getNumber().equals(Integer.valueOf(treeView.getSelectionModel().getSelectedItem().getValue()))).findFirst().orElse(null);
                }
            }
        }

        public Course newCourse(int number) {
            Course course = new Course();
            course.setNumber(number);
            course.setLetters(new ArrayList<>());
            dataList.add(course);
            overwriteData();
            return course;
        }

        public void removeCourse() {
            dataList.remove(course);
            super.removeItem();
        }

        @Override
        protected void overwriteData() {
            dataList.sort(Comparator.comparing(Course::getNumber));
            super.overwriteData();
        }

        public Course getCourse() {
            return course;
        }
    }

    private class TreeCellImpl extends TreeCell<String> {
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                setText(getItem() == null ? "" : getItem());
                setGraphic(getTreeItem().getGraphic());

                ContextMenu contextMenu = new ContextMenu();
                MenuItem addCourse = new MenuItem(resourceBundle.getString("controller.settings.common.addCourse"));
                MenuItem addLetter = new MenuItem(resourceBundle.getString("controller.settings.common.addLetter"));
                MenuItem removeCourse = new MenuItem(resourceBundle.getString("controller.settings.common.removeCourse"));
                MenuItem removeLetter = new MenuItem(resourceBundle.getString("controller.settings.common.removeLetter"));
                MenuItem addLanguage = new MenuItem(resourceBundle.getString("controller.settings.common.addLanguage"));
                MenuItem removeLanguage = new MenuItem(resourceBundle.getString("controller.settings.common.removeLanguage"));
                MenuItem addCategory = new MenuItem(resourceBundle.getString("controller.settings.common.addCategory"));
                MenuItem removeCategory = new MenuItem(resourceBundle.getString("controller.settings.common.removeCategory"));
                MenuItem addPublishHouse = new MenuItem(resourceBundle.getString("controller.settings.common.addPublishHouse"));
                MenuItem removePublishHouse = new MenuItem(resourceBundle.getString("controller.settings.common.removePublishHouse"));

                addCourse.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        String value = JOptionPane.showInputDialog(null, resourceBundle.getString("controller.settings.common.enterCourse"));
                        if (value == null) {
                            return;
                        }
                        int number;
                        try {
                            number = Integer.parseInt(value);
                            if (number < minCourse || number > maxCourse) {
                                JOptionPane.showMessageDialog(null, MessageFormat.format(resourceBundle.getString("controller.settings.common.availableCourseRow"), minCourse, maxCourse), null, JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        } catch (NumberFormatException numberFormatException) {
                            JOptionPane.showMessageDialog(null, resourceBundle.getString("alert.notANumber"), null, JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        List<Integer> courseNumbersChildren = treeView.getSelectionModel().getSelectedItem().getChildren().stream().map(course -> Integer.valueOf(course.getValue())).collect(Collectors.toList());
                        if (courseNumbersChildren.contains(number)) {
                            JOptionPane.showMessageDialog(null, resourceBundle.getString("controller.settings.common.courseExists"), null, JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        CourseObjectManager courseObjectManager = new CourseObjectManager(true, false);
                        courseObjectManager.newCourse(number);
                        treeView.getSelectionModel().getSelectedItem().getChildren().add(new CoursesTreeItem<>(String.valueOf(number), TreeItemType.COURSE_NUMBER));
                        List<?> sortedItems = treeView.getSelectionModel().getSelectedItem().getChildren().stream().sorted(Comparator.comparing(c -> Integer.parseInt(c.getValue()))).collect(Collectors.toList());
                        treeView.getSelectionModel().getSelectedItem().getChildren().setAll((Collection<? extends TreeItem<String>>) new ArrayList<>(sortedItems));
                        AdminActionsLogger.newLog(LogType.CREATE, LogTarget.SETTINGS, MessageFormat.format(resourceBundle.getString("log.newCourse"), number));
                        Main.getLibraryConfig().reloadConfig();
                    }
                });

                addLetter.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        String value = JOptionPane.showInputDialog(null, resourceBundle.getString("controller.settings.common.enterLetter"));
                        if (value == null) {
                            return;
                        }
                        if (value.length() > maxLettersSize) {
                            JOptionPane.showMessageDialog(null, MessageFormat.format(resourceBundle.getString("controller.settings.common.maxCourseLetters"), maxLettersSize), null, JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        CourseObjectManager courseObjectManager = new CourseObjectManager(false, false);
                        if (courseObjectManager.getCourse().getLetters().stream().map(String::toLowerCase).collect(Collectors.toList()).contains(value.toLowerCase())) {
                            JOptionPane.showMessageDialog(null, resourceBundle.getString("controller.settings.common.letterExists"), null, JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        courseObjectManager.getCourse().getLetters().add(value.toUpperCase());
                        courseObjectManager.addItem(value.toUpperCase(), TreeItemType.COURSE_LETTER);
                        treeView.getSelectionModel().getSelectedItem().getChildren().setAll(treeView.getSelectionModel().getSelectedItem().getChildren().sorted());
                        AdminActionsLogger.newLog(LogType.CREATE, LogTarget.SETTINGS, MessageFormat.format(resourceBundle.getString("log.newLetter"), treeView.getSelectionModel().getSelectedItem().getValue() + value));
                        Main.getLibraryConfig().reloadConfig();
                    }
                });

                removeCourse.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        CourseObjectManager objectMapperManager = new CourseObjectManager(false, false);
                        AdminActionsLogger.newLog(LogType.DELETE, LogTarget.SETTINGS, MessageFormat.format(resourceBundle.getString("log.deleteCourse"), treeView.getSelectionModel().getSelectedItem().getValue()));
                        objectMapperManager.removeCourse();
                        Main.getLibraryConfig().reloadConfig();
                    }
                });

                removeLetter.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        CourseObjectManager objectMapperManager = new CourseObjectManager(false, true);
                        AdminActionsLogger.newLog(LogType.DELETE, LogTarget.SETTINGS, MessageFormat.format(resourceBundle.getString("log.deleteLetter"), treeView.getSelectionModel().getSelectedItem().getParent().getValue() + treeView.getSelectionModel().getSelectedItem().getValue()));
                        objectMapperManager.dataList.remove(objectMapperManager.getCourse());
                        objectMapperManager.getCourse().getLetters().remove(treeView.getSelectionModel().getSelectedItem().getValue());
                        objectMapperManager.dataList.add(objectMapperManager.getCourse());
                        objectMapperManager.removeItem();
                        Main.getLibraryConfig().reloadConfig();
                    }
                });

                addLanguage.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        String value = JOptionPane.showInputDialog(null, resourceBundle.getString("controller.settings.common.enterLanguage"));
                        if (value == null) {
                            return;
                        }
                        ObjectMapperManager<String> objectMapperManager = new ObjectMapperManager<>("languages");
                        if (objectMapperManager.data.stream().map(String::toLowerCase).collect(Collectors.toList()).contains(value.toLowerCase())) {
                            JOptionPane.showMessageDialog(null, resourceBundle.getString("controller.settings.common.languageExists"), null, JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        objectMapperManager.dataList.add(value);
                        objectMapperManager.addItem(value, TreeItemType.LANGUAGE);
                        AdminActionsLogger.newLog(LogType.CREATE, LogTarget.SETTINGS, MessageFormat.format(resourceBundle.getString("log.newLanguage"), value));
                        Main.getLibraryConfig().reloadConfig();
                    }
                });

                removeLanguage.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        ObjectMapperManager<String> objectMapperManager = new ObjectMapperManager<>("languages");
                        AdminActionsLogger.newLog(LogType.DELETE, LogTarget.SETTINGS, MessageFormat.format(resourceBundle.getString("log.deleteLanguage"), treeView.getSelectionModel().getSelectedItem().getValue()));
                        objectMapperManager.dataList.remove(treeView.getSelectionModel().getSelectedItem().getValue());
                        objectMapperManager.removeItem();
                        Main.getLibraryConfig().reloadConfig();
                    }
                });

                addCategory.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        String value = JOptionPane.showInputDialog(null, resourceBundle.getString("controller.settings.common.enterCategory"));
                        if (value == null) {
                            return;
                        }
                        ObjectMapperManager<String> objectMapperManager = new ObjectMapperManager<>("categories");
                        if (objectMapperManager.data.stream().map(String::toLowerCase).collect(Collectors.toList()).contains(value.toLowerCase())) {
                            JOptionPane.showMessageDialog(null, resourceBundle.getString("controller.settings.common.categoryExists"), null, JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        AdminActionsLogger.newLog(LogType.CREATE, LogTarget.SETTINGS, MessageFormat.format(resourceBundle.getString("log.newCategory"), value));
                        objectMapperManager.dataList.add(value);
                        objectMapperManager.addItem(value, TreeItemType.CATEGORY);
                        Main.getLibraryConfig().reloadConfig();
                    }
                });

                removeCategory.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        ObjectMapperManager<String> objectMapperManager = new ObjectMapperManager<>("categories");
                        AdminActionsLogger.newLog(LogType.DELETE, LogTarget.SETTINGS, MessageFormat.format(resourceBundle.getString("log.deleteCategory"), treeView.getSelectionModel().getSelectedItem().getValue()));
                        objectMapperManager.dataList.remove(treeView.getSelectionModel().getSelectedItem().getValue());
                        objectMapperManager.removeItem();
                        Main.getLibraryConfig().reloadConfig();
                    }
                });

                addPublishHouse.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        String value = JOptionPane.showInputDialog(null, resourceBundle.getString("controller.settings.common.enterPublishHouse"));
                        if (value == null) {
                            return;
                        }
                        ObjectMapperManager<String> objectMapperManager = new ObjectMapperManager<>("publishHouses");
                        if (objectMapperManager.data.stream().map(String::toLowerCase).collect(Collectors.toList()).contains(value.toLowerCase())) {
                            JOptionPane.showMessageDialog(null, resourceBundle.getString("controller.settings.common.publishHouseExists"), null, JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        AdminActionsLogger.newLog(LogType.CREATE, LogTarget.SETTINGS, MessageFormat.format(resourceBundle.getString("log.newPublishHouse"), value));
                        objectMapperManager.dataList.add(value);
                        objectMapperManager.addItem(value, TreeItemType.PUBLISH_HOUSE);
                        Main.getLibraryConfig().reloadConfig();
                    }
                });

                removePublishHouse.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        ObjectMapperManager<String> objectMapperManager = new ObjectMapperManager<>("publishHouses");
                        AdminActionsLogger.newLog(LogType.DELETE, LogTarget.SETTINGS, MessageFormat.format(resourceBundle.getString("log.removePublishHouse"), treeView.getSelectionModel().getSelectedItem().getValue()));
                        objectMapperManager.dataList.remove(treeView.getSelectionModel().getSelectedItem().getValue());
                        objectMapperManager.removeItem();
                        Main.getLibraryConfig().reloadConfig();
                    }
                });

                switch (((CoursesTreeItemInterface) getTreeItem()).getTreeItemType()) {
                    case COURSES_LIST: {
                        contextMenu.getItems().add(addCourse);
                        break;
                    }
                    case COURSE_NUMBER: {
                        contextMenu.getItems().add(removeCourse);
                        contextMenu.getItems().add(addLetter);
                        break;
                    }
                    case COURSE_LETTER: {
                        contextMenu.getItems().add(removeLetter);
                        break;
                    }
                    case LANGUAGES_LIST: {
                        contextMenu.getItems().add(addLanguage);
                        break;
                    }
                    case LANGUAGE: {
                        contextMenu.getItems().add(removeLanguage);
                        break;
                    }
                    case CATEGORIES_LIST: {
                        contextMenu.getItems().add(addCategory);
                        break;
                    }
                    case CATEGORY: {
                        contextMenu.getItems().add(removeCategory);
                        break;
                    }
                    case PUBLISH_HOUSE_LIST: {
                        contextMenu.getItems().add(addPublishHouse);
                        break;
                    }
                    case PUBLISH_HOUSE: {
                        contextMenu.getItems().add(removePublishHouse);
                        break;
                    }
                    default:
                        break;
                }

                setContextMenu(contextMenu);
            }
        }
    }

    private static <T> TreeItem<T> getTreeViewItem(TreeItem<T> item, T value) {
        if (item != null) {
            if (item.getValue().equals(value)) return item;
            for (TreeItem<T> child : item.getChildren()) {
                TreeItem<T> s = getTreeViewItem(child, value);
                if (s != null) {
                    return s;
                }
            }
        }
        return null;
    }

    private static class CoursesTreeItem<T> extends TreeItem<T> implements CoursesTreeItemInterface {
        private TreeItemType treeItemType;

        CoursesTreeItem() {
            super();
        }

        CoursesTreeItem(T string, TreeItemType treeItemType) {
            super(string);
            this.treeItemType = treeItemType;
        }

        public TreeItemType getTreeItemType() {
            return treeItemType;
        }
    }

    private interface CoursesTreeItemInterface {
        TreeItemType getTreeItemType();
    }

    private enum TreeItemType {
        COURSES_LIST,
        COURSE_NUMBER,
        COURSE_LETTER,
        LANGUAGES_LIST,
        LANGUAGE,
        CATEGORIES_LIST,
        CATEGORY,
        PUBLISH_HOUSE_LIST,
        PUBLISH_HOUSE
    }
}
