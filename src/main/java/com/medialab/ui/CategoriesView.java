package com.medialab.ui;

import com.medialab.app.model.DocCategory;
import com.medialab.launcher.Boot;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class CategoriesView {
    private final Boot boot;
    private final BorderPane root = new BorderPane();
    private final ListView<DocCategory> listView = new ListView<>();
    private final TextField txtName = new TextField();
    private final Button btnAdd = new Button("Προσθήκη");
    private final Button btnRename = new Button("Μετονομασία");
    private final Button btnDelete = new Button("Διαγραφή");

    private final Runnable onChanged;

    public CategoriesView(Boot boot, Runnable onChanged) {
    this.boot = boot;
    this.onChanged = onChanged;
    buildLayout();
    refreshData();
}

    public CategoriesView(Boot boot) {
    this(boot, null);
}

    private void buildLayout() {
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override 
            protected void updateItem(DocCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name());
            }
        });

        txtName.setPromptText("Όνομα νέας κατηγορίας");
        HBox.setHgrow(txtName, Priority.ALWAYS);

        btnAdd.setOnAction(e -> {
            try {
                if (txtName.getText().isBlank()) throw new Exception("Το όνομα δεν μπορεί να είναι κενό.");
                boot.catAdmin.create(txtName.getText().trim());
                txtName.clear();
                refreshData();
                if (onChanged != null) onChanged.run();

            } catch (Exception ex) {
                alert("Σφάλμα", ex.getMessage());
            }
        });

        btnRename.setOnAction(e -> {
            DocCategory selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                alert("Σφάλμα", "Επιλέξτε κατηγορία για μετονομασία.");
                return;
            }
            
            TextInputDialog dialog = new TextInputDialog(selected.name());
            dialog.setTitle("Μετονομασία");
            dialog.setHeaderText("Αλλαγή ονόματος για: " + selected.name());
            dialog.setContentText("Νέο όνομα:");
            
            dialog.showAndWait().ifPresent(newName -> {
                try {
                    boot.catAdmin.rename(selected.id(), newName.trim());
                    refreshData();
                    if (onChanged != null) onChanged.run();

                } catch (Exception ex) {
                    alert("Σφάλμα", ex.getMessage());
                }
            });
        });

        btnDelete.setOnAction(e -> {
            DocCategory selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                alert("Σφάλμα", "Επιλέξτε κατηγορία για διαγραφή.");
                return;
            }
            
            boolean confirmed = confirm("Διαγραφή Κατηγορίας", 
                "Θα διαγραφούν οριστικά όλα τα έγγραφα και οι εκδόσεις αυτής της κατηγορίας.\n\nΘέλετε να συνεχίσετε;");
            
            if (confirmed) {
                try {
                    boot.catAdmin.deleteCascade(selected.id());
                    refreshData();
                    if (onChanged != null) onChanged.run();
                } catch (Exception ex) {
                    alert("Σφάλμα", ex.getMessage());
                }
            }
        });

        HBox actions = new HBox(10, txtName, btnAdd, btnRename, btnDelete);
        actions.setPadding(new Insets(15));
        actions.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

        root.setCenter(listView);
        root.setBottom(actions);
        BorderPane.setMargin(listView, new Insets(10));
    }

    private void refreshData() {
        listView.getItems().setAll(boot.catAdmin.listAll());
    }

    public BorderPane getRoot() { return root; }

    private void alert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private boolean confirm(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        return alert.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }
}