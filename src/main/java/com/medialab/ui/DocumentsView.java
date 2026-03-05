package com.medialab.ui;

import java.util.List;

import com.medialab.app.model.DocCategory;
import com.medialab.app.model.DocVersion;
import com.medialab.app.model.Document;
import com.medialab.app.model.Role;
import com.medialab.app.model.User;
import com.medialab.launcher.Boot;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class DocumentsView {
    private final Boot boot;
    private final BorderPane root = new BorderPane();
    private final User currentUser;
    private final Runnable refreshUi;

    private final ComboBox<DocCategory> cmbCat = new ComboBox<>();
    private final ListView<Document> lstDocs = new ListView<>();
    private final ListView<DocVersion> lstVersions = new ListView<>();

    private final Button btnCreate = new Button("Νέο Έγγραφο");
    private final Button btnEdit = new Button("Τροποποίηση");
    private final Button btnDelete = new Button("Διαγραφή");
    private final Button btnFollow = new Button("Follow");
    private final Button btnUnfollow = new Button("Unfollow");

    public DocumentsView(Boot boot, User user) {
        this(boot, user, () -> {});
    }

    public DocumentsView(Boot boot, User user, Runnable refreshUi) {
        this.boot = boot;
        this.currentUser = user;
        this.refreshUi = (refreshUi == null) ? (() -> {}) : refreshUi;
        build();
        refreshCats();
    }

    private void build() {
        cmbCat.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(DocCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name());
            }
        });
        cmbCat.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(DocCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name());
            }
        });

        lstDocs.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(Document item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.title() + " (Author: " + item.authorUsername() + ")");
            }
        });

        lstVersions.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(DocVersion item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String dateStr = item.date() == null ? "-" : Date_Form.DMY.format(item.date());
                    setText("v" + item.version() + "  •  " + dateStr);
                }
            }
        });

        cmbCat.valueProperty().addListener((obs, o, n) -> refreshDocs());
        lstDocs.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> refreshVersions());

        btnCreate.setOnAction(e -> onCreate());
        btnEdit.setOnAction(e -> onEdit());
        btnDelete.setOnAction(e -> onDelete());
        btnFollow.setOnAction(e -> onFollow(true));
        btnUnfollow.setOnAction(e -> onFollow(false));

        HBox toolbar = new HBox(10, new Label("Κατηγορία:"), cmbCat, new Separator(), btnCreate, btnEdit, btnDelete, btnFollow, btnUnfollow);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(15));
        toolbar.setStyle("-fx-background-color: #eeeeee;");

        SplitPane contentSplit = new SplitPane(wrap("Λίστα Εγγράφων", lstDocs), wrap("Ιστορικό Εκδόσεων", lstVersions));
        contentSplit.setDividerPositions(0.6);

        root.setTop(toolbar);
        root.setCenter(contentSplit);
    }

    private TitledPane wrap(String title, Control c) {
        VBox box = new VBox(c);
        VBox.setVgrow(c, Priority.ALWAYS);
        TitledPane pane = new TitledPane(title, box);
        pane.setCollapsible(false);
        pane.setMaxHeight(Double.MAX_VALUE);
        return pane;
    }

    private void refreshCats() {
        var all = boot.categories.findAll();
        cmbCat.setItems(FXCollections.observableArrayList(all));
    
        if (!cmbCat.getItems().isEmpty()) {
            cmbCat.getSelectionModel().select(0);
        }
    }

    private void refreshDocs() {
        var cat = cmbCat.getValue();
        if (cat == null) {
            lstDocs.getItems().clear();
            lstVersions.getItems().clear();
            return;
        }
        List<Document> docs = boot.reading.searchAccessible(currentUser.username(), null, null, cat.id());
        lstDocs.getItems().setAll(docs);
        if (!lstDocs.getItems().isEmpty()) {
            lstDocs.getSelectionModel().selectFirst();
        } else {
            lstVersions.getItems().clear();
        }
    }

    private void refreshVersions() {
        var doc = lstDocs.getSelectionModel().getSelectedItem();
        if (doc == null) {
            lstVersions.getItems().clear();
            return;
        }
        var versions = boot.reading.visibleVersions(currentUser.username(), doc.id());
        lstVersions.getItems().setAll(versions);
        if (!lstVersions.getItems().isEmpty()) {
            lstVersions.getSelectionModel().selectLast();
        }
    }

    private void onCreate() {
        var cat = cmbCat.getValue();
        if (cat == null) return;
        if (currentUser.role() == Role.USER) {
            alert("Πρόσβαση", "Μόνο Συγγραφείς και Διαχειριστές μπορούν να δημιουργούν έγγραφα.");
            return;
        }

        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Νέο Έγγραφο");
        dialog.setHeaderText("Δημιουργία εγγράφου στην κατηγορία: " + cat.name());

        TextField txtTitle = new TextField();
        txtTitle.setPromptText("Τίτλος");
        TextArea txtContent = new TextArea();
        txtContent.setPromptText("Αρχικό περιεχόμενο...");
        txtContent.setPrefRowCount(8);

        VBox layout = new VBox(10, new Label("Τίτλος:"), txtTitle, new Label("Περιεχόμενο:"), txtContent);
        layout.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> btn == ButtonType.OK ? List.of(txtTitle.getText(), txtContent.getText()) : null);

        dialog.showAndWait().ifPresent(results -> {
            try {
                String title = results.get(0);
                String content = results.get(1);
                if (title.isBlank()) throw new Exception("Ο τίτλος είναι υποχρεωτικός.");
                boot.authoring.createDocument(currentUser.username(), cat.id(), title, content);
                refreshDocs();
                refreshUi.run();
            } catch (Exception ex) { alert("Σφάλμα", ex.getMessage()); }
        });
    }

    private void onEdit() {
        var doc = lstDocs.getSelectionModel().getSelectedItem();
        if (doc == null) return;
        if (currentUser.role() == Role.USER) {
            alert("Πρόσβαση", "Δεν έχετε δικαίωμα τροποποίησης.");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Τροποποίηση");
        dialog.setHeaderText("Επεξεργασία εγγράφου: " + doc.title());
        
        TextArea textArea = new TextArea();
        textArea.setPromptText("Γράψτε το νέο κείμενο εδώ...");
        dialog.getDialogPane().setContent(new VBox(10, new Label("Περιεχόμενο:"), textArea));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? textArea.getText() : null);

        dialog.showAndWait().ifPresent(newContent -> {
            try {
                boot.authoring.editDocument(currentUser.username(), doc.id(), newContent);
                refreshVersions();
                alert("Επιτυχές", "Δημιουργήθηκε νέα έκδοση.");
            } catch (Exception ex) { alert("Σφάλμα", ex.getMessage()); }
        });
    }

    private void onDelete() {
        var doc = lstDocs.getSelectionModel().getSelectedItem();
        if (doc == null) return;
        if (currentUser.role() == Role.USER) {
            alert("Σφάλμα", "Δεν έχετε δικαίωμα διαγραφής.");
            return;
        }

        if (confirm("Διαγραφή", "Θα διαγραφεί το έγγραφο '" + doc.title() + "' και όλες οι εκδόσεις του. Συνέχεια;")) {
            try {
                boot.authoring.deleteDocument(currentUser.username(), doc.id());
                refreshDocs();
                refreshUi.run();
            } catch (Exception ex) { alert("Σφάλμα", ex.getMessage()); }
        }
    }

    private void onFollow(boolean follow) {
        var doc = lstDocs.getSelectionModel().getSelectedItem();
        if (doc == null) return;
        try {
            if (follow) boot.followUse.follow(currentUser.username(), doc.id());
            else boot.followUse.unfollow(currentUser.username(), doc.id());
            alert("Ενημέρωση", follow ? "Ακολουθείτε το έγγραφο." : "Διακόπηκε η παρακολούθηση.");
            refreshUi.run();
        } catch (Exception ex) { alert("Σφάλμα", ex.getMessage()); }
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
}