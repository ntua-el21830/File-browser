package com.medialab.ui;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.medialab.app.model.DocCategory;
import com.medialab.app.model.Role;
import com.medialab.app.model.User;
import com.medialab.launcher.Boot;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class UsersView {
    private final Boot boot;
    private final BorderPane root = new BorderPane();
    private final ListView<User> userListView = new ListView<>();
    private final TextField txtUsername = new TextField();
    private final PasswordField txtPassword = new PasswordField();
    private final TextField txtFullName = new TextField();
    private final ComboBox<Role> cmbRole = new ComboBox<>();
    private final ListView<DocCategory> catListView = new ListView<>();

    private final Button btnCreate = new Button("Προσθήκη");
    private final Button btnUpdate = new Button("Ενημέρωση");
    private final Button btnDelete = new Button("Διαγραφή");

    public UsersView(Boot boot) {
        this.boot = boot;
        buildLayout();
        refreshData();
        loadCategories();
    }

    private void buildLayout() {
        userListView.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.username() + " [" + item.role() + "]");
            }
        });
        userListView.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> fillForm(sel));

        cmbRole.setItems(FXCollections.observableArrayList(Role.values()));

        catListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        catListView.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(DocCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name());
            }
        });
        catListView.setPrefHeight(180);

        btnCreate.setOnAction(e -> {
            try {
                boot.usersAdmin.create(
                        txtUsername.getText(),
                        txtPassword.getText(),
                        txtFullName.getText(),
                        cmbRole.getValue(),
                        selectedCategoryIds()
                );
                refreshData();
                clearForm();
            } catch (Exception ex) { alert("Σφάλμα", ex.getMessage()); }
        });

        btnUpdate.setOnAction(e -> {
            User sel = userListView.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            try {
                boot.usersAdmin.update(
                        sel.id(),
                        txtFullName.getText(),
                        cmbRole.getValue(),
                        selectedCategoryIds()
                );
                refreshData();
            } catch (Exception ex) { alert("Σφάλμα", ex.getMessage()); }
        });

        btnDelete.setOnAction(e -> {
            User sel = userListView.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            if (confirm("Διαγραφή", "Ο χρήστης θα διαγραφεί. Συνέχεια;")) {
                try {
                    boot.usersAdmin.delete(sel.id());
                    refreshData();
                    clearForm();
                } catch (Exception ex) { alert("Σφάλμα", ex.getMessage()); }
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(10);
        form.add(new Label("Username:"), 0, 0); form.add(txtUsername, 1, 0);
        form.add(new Label("Full Name:"), 0, 1); form.add(txtFullName, 1, 1);
        form.add(new Label("Role:"), 0, 2); form.add(cmbRole, 1, 2);
        form.add(new Label("Password:"), 0, 3); form.add(txtPassword, 1, 3);

        VBox catsBox = new VBox(6,
                new Label("Κατηγορίες πρόσβασης:"),
                catListView
        );

        HBox actions = new HBox(10, btnCreate, btnUpdate, btnDelete);
        VBox rightSide = new VBox(15, form, catsBox, actions);
        rightSide.setPadding(new Insets(15));

        root.setLeft(userListView);
        root.setCenter(rightSide);
    }

    private void fillForm(User u) {
        if (u == null) { clearForm(); return; }
        txtUsername.setText(u.username());
        txtUsername.setDisable(true);
        txtFullName.setText(u.fullName());
        cmbRole.setValue(u.role());

        catListView.getSelectionModel().clearSelection();
        if (u.allowedCategoryUuids() != null) {
            for (int i = 0; i < catListView.getItems().size(); i++) {
                DocCategory c = catListView.getItems().get(i);
                if (u.allowedCategoryUuids().contains(c.id())) {
                    catListView.getSelectionModel().select(i);
                }
            }
        }
    }

    private void refreshData() {
        userListView.getItems().setAll(boot.usersAdmin.list());
    }

    private void clearForm() {
        txtUsername.clear(); txtUsername.setDisable(false);
        txtFullName.clear(); txtPassword.clear();
        cmbRole.setValue(null);
        catListView.getSelectionModel().clearSelection();
    }

    public BorderPane getRoot() { return root; }

    private void alert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private boolean confirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        return a.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }

    private void loadCategories() {
        catListView.setItems(FXCollections.observableArrayList(boot.categories.findAll()));
    }

    private Set<UUID> selectedCategoryIds() {
        Set<UUID> ids = catListView.getSelectionModel().getSelectedItems().stream()
                .map(DocCategory::id)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("Πρέπει να επιλεγεί τουλάχιστον μία κατηγορία πρόσβασης.");
        }
        return ids;
    }
}
