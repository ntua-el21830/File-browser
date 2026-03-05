package com.medialab.ui;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.medialab.app.model.DocCategory;
import com.medialab.app.model.DocVersion;
import com.medialab.app.model.Follow;
import com.medialab.app.model.User;
import com.medialab.launcher.Boot;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;

public class FollowsView {
    private final Boot boot;
    private final User currentUser;
    private final BorderPane root = new BorderPane();
    private final Runnable refreshUi;

    private final TableView<Row> table = new TableView<>();
    private final Button btnRefresh = new Button("Ανανέωση");
    private final Button btnOpen = new Button("Προβολή");
    private final Button btnRemove = new Button("Αφαίρεση");

    public FollowsView(Boot boot, User currentUser) {
        this(boot, currentUser, () -> {});
    }

    public FollowsView(Boot boot, User currentUser, Runnable refreshUi) {
        this.boot = boot;
        this.currentUser = currentUser;
        this.refreshUi = (refreshUi == null) ? (() -> {}) : refreshUi;
        build();
        loadData();
    }

    private void build() {
        TableColumn<Row, String> c1 = new TableColumn<>("Τίτλος");
        c1.setCellValueFactory(d -> d.getValue().title);
        
        TableColumn<Row, String> c2 = new TableColumn<>("Κατηγορία");
        c2.setCellValueFactory(d -> d.getValue().category);
        
        TableColumn<Row, String> c3 = new TableColumn<>("Τελευταία έκδοση");
        c3.setCellValueFactory(d -> d.getValue().latestVersion);
        
        TableColumn<Row, String> c4 = new TableColumn<>("Τελευταία που ειδόθηκε");
        c4.setCellValueFactory(d -> d.getValue().lastSeen);

        table.getColumns().setAll(List.of(c1, c2, c3, c4));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        btnRefresh.setOnAction(e -> loadData());
        btnOpen.setOnAction(e -> openSelected());
        btnRemove.setOnAction(e -> removeSelected());

        ToolBar toolbar = new ToolBar(btnRefresh, btnOpen, btnRemove);
        toolbar.setPadding(new Insets(6));

        root.setTop(toolbar);
        root.setCenter(table);
        BorderPane.setMargin(table, new Insets(8));
    }

    private void loadData() {
        List<Row> rows = boot.follows.findByUser(currentUser.username()).stream()
                .map(this::toRow)
                .collect(Collectors.toList());
        table.setItems(FXCollections.observableArrayList(rows));
    }

    private Row toRow(Follow f) {
        var doc = boot.docs.findById(f.documentId()).orElse(null);
        String title = (doc == null) ? "(διαγράφηκε)" : doc.title();
        String catName = (doc == null) ? "-" :
                boot.categories.findById(doc.categoryId()).map(DocCategory::name).orElse("-");
        
        List<DocVersion> versions = boot.versions.findByDocument(f.documentId());
        int latest = versions.stream().mapToInt(DocVersion::version).max().orElse(0);
        
        return Row.of(f.documentId(), title, catName, latest, f.lastSeenVersion());
    }

    private void openSelected() {
        Row row = table.getSelectionModel().getSelectedItem();
        if (row == null) return;
    
        List<DocVersion> versions = boot.reading.visibleVersions(currentUser.username(), row.documentId);
        if (versions.isEmpty()) {
            alert("Προβολή", "Δεν υπάρχουν διαθέσιμες εκδόσεις.");
            return;
        }
        
        DocVersion last = versions.get(versions.size() - 1);
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(row.title.getValue() + " — v" + last.version());
        a.setContentText(last.content());
        a.getDialogPane().setMinWidth(520);
        a.showAndWait();

        try {
            boot.followUse.markSeen(currentUser.username(), row.documentId, last.version());
            loadData();
        } catch (Exception ex) {
            alert("Σφάλμα", "Αδυναμία ενημέρωσης κατάστασης ανάγνωσης.");
        }
    }

    private void removeSelected() {
        Row row = table.getSelectionModel().getSelectedItem();
        if (row == null) return;
        
        if (!confirm("Διακοπή", "Διακοπή παρακολούθησης για «" + row.title.getValue() + "»;")) return;
        
        try {
            boot.followUse.unfollow(currentUser.username(), row.documentId);
            loadData();
            refreshUi.run();
        } catch (Exception ex) {
            alert("Σφάλμα", ex.getMessage());
        }
    }

    public BorderPane getRoot() { return root; }

    private void alert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(title);
        a.setContentText(msg);
        a.showAndWait();
    }

    private boolean confirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setHeaderText(title);
        a.setContentText(msg);
        return a.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }

    private static class Row {
        final UUID documentId;
        final SimpleStringProperty title;
        final SimpleStringProperty category;
        final SimpleStringProperty latestVersion;
        final SimpleStringProperty lastSeen;

        private Row(UUID id, String t, String c, int latest, int seen) {
            this.documentId = id;
            this.title = new SimpleStringProperty(t);
            this.category = new SimpleStringProperty(c);
            this.latestVersion = new SimpleStringProperty(latest == 0 ? "-" : "v" + latest);
            this.lastSeen = new SimpleStringProperty(seen == 0 ? "-" : "v" + seen);
        }

        static Row of(UUID id, String t, String c, int latest, int seen) {
            return new Row(id, t, c, latest, seen);
        }
    }
}