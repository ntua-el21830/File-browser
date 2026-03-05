package com.medialab.ui;

import com.medialab.app.model.Role;
import com.medialab.app.model.User;
import com.medialab.launcher.Boot;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MainView {
    private final Boot boot;
    private final User currentUser;
    private final BorderPane root = new BorderPane();

    private final Label lblCats = new Label("-");
    private final Label lblDocs = new Label("-");
    private final Label lblFollows = new Label("-");

    public MainView(Boot boot, User user) {
        this.boot = boot;
        this.currentUser = user;
        build();
        refreshCounters();
        checkNotifications();
    }

    private void build() {

        Label title = new Label("MediaLab Documents");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(30);
        statsGrid.add(new Label("Κατηγορίες:"), 0, 0); statsGrid.add(lblCats, 1, 0);
        statsGrid.add(new Label("Έγγραφα:"), 2, 0); statsGrid.add(lblDocs, 3, 0);
        statsGrid.add(new Label("Παρακολουθήσεις:"), 4, 0); statsGrid.add(lblFollows, 5, 0);

        VBox header = new VBox(10, title, statsGrid);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #f0f2f5; -fx-border-color: #dcdfe6; -fx-border-width: 0 0 1 0;");

        root.setTop(header);

        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(15));
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dcdfe6; -fx-border-width: 0 1 0 0;");

        Button btnDocs = createMenuButton("Έγγραφα");
        Button btnSearch = createMenuButton("Αναζήτηση");
        Button btnFolls = createMenuButton("Παρακολουθήσεις");

        if (currentUser.role() == Role.ADMIN) {
            Button btnUsers = createMenuButton("Χρήστες");
            Button btnCats = createMenuButton("Κατηγορίες");
            sidebar.getChildren().addAll( btnUsers, btnCats, new Separator());

            btnUsers.setOnAction(e -> root.setCenter(new UsersView(boot).getRoot()));
            btnCats.setOnAction(e -> root.setCenter(new CategoriesView(boot, this::refreshCounters).getRoot()));
        }

        sidebar.getChildren().addAll( btnDocs, btnSearch, btnFolls);

        btnDocs.setOnAction(e -> root.setCenter(new DocumentsView(boot, currentUser, this::refreshStats).getRoot()));
        btnSearch.setOnAction(e -> root.setCenter(new SearchView(boot, currentUser).getRoot()));
        btnFolls.setOnAction(e -> root.setCenter(new FollowsView(boot, currentUser, this::refreshStats).getRoot()));

        root.setLeft(sidebar);
        root.setCenter(new StackPane());
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(8, 12, 8, 12));
        return btn;
    }

    private void refreshCounters() {
        lblCats.setText(String.valueOf(boot.categories.findAll().size()));
        lblDocs.setText(String.valueOf(boot.docs.findAll().size()));
        lblFollows.setText(String.valueOf(boot.follows.findByUser(currentUser.username()).size()));
    }

    public void refreshStats() {
        Platform.runLater(this::refreshCounters);
    }

    private void checkNotifications() {
        Platform.runLater(() -> {
            var updates = boot.followUse.notifyOnLogin(currentUser.username());
            if (!updates.isEmpty()) {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Ενημερώσεις");
                a.setHeaderText("Νέες εκδόσεις στα έγγραφα που ακολουθείτε.");
                a.showAndWait();
            }
        });
    }

    public BorderPane getRoot() { return root; }
}