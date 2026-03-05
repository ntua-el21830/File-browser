package com.medialab.ui;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.medialab.app.model.DocCategory;
import com.medialab.app.model.DocVersion;
import com.medialab.app.model.Document;
import com.medialab.app.model.User;
import com.medialab.launcher.Boot;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class SearchView {
    private final Boot boot;
    private final User currentUser;
    private final BorderPane root = new BorderPane();

    private final TextField txtTitle = new TextField();
    private final TextField txtAuthor = new TextField();
    private final ComboBox<DocCategory> cmbCat = new ComboBox<>();
    private final Button btnSearch = new Button("Αναζήτηση");

    private final TableView<Row> table = new TableView<>();

    public SearchView(Boot boot, User currentUser) {
        this.boot = boot;
        this.currentUser = currentUser;
        build();
        loadCategories();
    }

    private void build() {
        txtTitle.setPromptText("Τίτλος");
        txtAuthor.setPromptText("Συγγραφέας");

        cmbCat.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(DocCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Όλες οι κατηγορίες" : item.name());
            }
        });
        cmbCat.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(DocCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Όλες οι κατηγορίες" : item.name());
            }
        });

        btnSearch.setOnAction(e -> doSearch());

        HBox form = new HBox(
                8,
                new Label("Τίτλος:"), txtTitle,
                new Label("Συγγραφέας:"), txtAuthor,
                new Label("Κατηγορία:"), cmbCat,
                btnSearch
        );
        form.setPadding(new Insets(15));
        form.setStyle("-fx-background-color: #f4f4f4;");

        TableColumn<Row, String> c1 = new TableColumn<>("Τίτλος");
        c1.setCellValueFactory(d -> d.getValue().title);

        TableColumn<Row, String> c2 = new TableColumn<>("Συγγραφέας");
        c2.setCellValueFactory(d -> d.getValue().author);

        TableColumn<Row, String> c3 = new TableColumn<>("Κατηγορία");
        c3.setCellValueFactory(d -> d.getValue().category);

        TableColumn<Row, String> c4 = new TableColumn<>("Ημερομηνία");
        c4.setCellValueFactory(d -> d.getValue().createdAt);

        TableColumn<Row, String> c5 = new TableColumn<>("Τελευταία έκδοση");
        c5.setCellValueFactory(d -> d.getValue().latestVersion);

        table.getColumns().addAll(List.of(c1, c2, c3, c4, c5));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        table.setRowFactory(tv -> {
            TableRow<Row> r = new TableRow<>();
            r.setOnMouseClicked(evt -> {
                if (evt.getClickCount() == 2 && !r.isEmpty()) {
                    Row row = r.getItem();
                    openDocument(row);
                }
            });
            return r;
        });

        root.setTop(form);
        root.setCenter(table);
        BorderPane.setMargin(table, new Insets(10));
    }

    private void openDocument(Row row) {
        List<DocVersion> versions = boot.reading.visibleVersions(currentUser.username(), row.documentId);
        if (versions == null || versions.isEmpty()) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Προβολή Εγγράφου");
            a.setHeaderText(row.title.get());
            a.setContentText("Δεν υπάρχουν ορατές εκδόσεις για το έγγραφο.");
            a.showAndWait();
            return;
        }

        versions = versions.stream()
                .sorted(Comparator.comparingInt(DocVersion::version).reversed())
                .toList();

        DocVersion selectedVersion;

        if (versions.size() == 1) {
            selectedVersion = versions.get(0);
        } else {
            Map<String, DocVersion> optionsMap = new LinkedHashMap<>();
            for (DocVersion v : versions) {
                String key = "Έκδοση " + v.version();
                optionsMap.put(key, v);
            }

            String defaultChoice = optionsMap.keySet().iterator().next();
            ChoiceDialog<String> dialog = new ChoiceDialog<>(defaultChoice, optionsMap.keySet());
            dialog.setTitle("Επιλογή Έκδοσης");
            dialog.setHeaderText(row.title.get());
            dialog.setContentText("Διαλέξτε έκδοση:");

            String chosen = dialog.showAndWait().orElse(null);
            if (chosen == null) return;

            selectedVersion = optionsMap.get(chosen);
            if (selectedVersion == null) return;
        }

        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Προβολή Εγγράφου");
        a.setHeaderText(row.title.get() + " — Έκδοση " + selectedVersion.version());
        a.setContentText(selectedVersion.content());
        a.getDialogPane().setMinWidth(500);
        a.showAndWait();
    }

    private void loadCategories() {
        List<DocCategory> all = boot.categories.findAll();
        cmbCat.setItems(FXCollections.observableArrayList(all));
        cmbCat.getItems().add(0, null);
        cmbCat.getSelectionModel().select(0);
    }

    private void doSearch() {
        String title = txtTitle.getText().isBlank() ? null : txtTitle.getText().trim();
        String author = txtAuthor.getText().isBlank() ? null : txtAuthor.getText().trim();
        UUID catId = cmbCat.getValue() == null ? null : cmbCat.getValue().id();

        List<Document> docs = boot.reading.searchAccessible(currentUser.username(), title, author, catId);

        var items = FXCollections.<Row>observableArrayList();

        for (Document d : docs) {
            String catName = boot.categories.findById(d.categoryId()).map(DocCategory::name).orElse("-");
            String dateStr = d.createdDate() == null ? "-" : Date_Form.DMY.format(d.createdDate());

            List<DocVersion> versions = boot.reading.visibleVersions(currentUser.username(), d.id());

            String latest = versions.stream()
                    .map(DocVersion::version)
                    .max(Integer::compareTo)
                    .map(v -> "v" + v)
                    .orElse("-");

            items.add(Row.of(d.id(), d.title(), d.authorUsername(), catName, dateStr, latest));
        }

        table.setItems(items);
    }

    private static class Row {
        final UUID documentId;
        final SimpleStringProperty title;
        final SimpleStringProperty author;
        final SimpleStringProperty category;
        final SimpleStringProperty createdAt;
        final SimpleStringProperty latestVersion;

        private Row(UUID id, String t, String a, String c, String dt, String lv) {
            this.documentId = id;
            this.title = new SimpleStringProperty(t);
            this.author = new SimpleStringProperty(a);
            this.category = new SimpleStringProperty(c);
            this.createdAt = new SimpleStringProperty(dt);
            this.latestVersion = new SimpleStringProperty(lv);
        }

        static Row of(UUID id, String t, String a, String c, String dt, String lv) {
            return new Row(id, t, a, c, dt, lv);
        }
    }

    public BorderPane getRoot() {
        return root;
    }
}