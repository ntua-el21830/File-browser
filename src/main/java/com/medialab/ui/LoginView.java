package com.medialab.ui;

import java.util.function.Consumer;

import com.medialab.app.model.User;
import com.medialab.launcher.Boot;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class LoginView {
    private final Boot boot;
    private final Consumer<User> onLoginSuccess;
    private final VBox root = new VBox(15);

    private final TextField txtUsername = new TextField();
    private final PasswordField txtPassword = new PasswordField();
    private final Button btnLogin = new Button("Είσοδος");

    public LoginView(Boot boot, Consumer<User> onLoginSuccess) {
        this.boot = boot;
        this.onLoginSuccess = onLoginSuccess;
        build();
    }

    private void build() {
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #f4f7f6;");

        Label title = new Label("MediaLab Documents");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));

        txtUsername.setPromptText("Username");
        txtUsername.setPrefHeight(35);
        
        txtPassword.setPromptText("Password");
        txtPassword.setPrefHeight(35);

        btnLogin.setDefaultButton(true);
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setPrefHeight(40);
        btnLogin.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-cursor: hand;");

        btnLogin.setOnAction(e -> {
            try {
                String username = txtUsername.getText().trim();
                String password = txtPassword.getText();
                
                if (username.isEmpty() || password.isEmpty()) {
                    throw new Exception("Παρακαλώ συμπληρώστε τα πεδία.");
                }

                User user = boot.auth.login(username, password);
                onLoginSuccess.accept(user);
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Σφάλμα Σύνδεσης");
                alert.setHeaderText(null);
                alert.setContentText(ex.getMessage().contains("IllegalArgumentException") ? "Λάθος στοιχεία σύνδεσης." : ex.getMessage());
                alert.showAndWait();
            }
        });

        root.getChildren().addAll(
            title,
            txtUsername,
            txtPassword,
            btnLogin
        );
    }

    public VBox getRoot() {
        return root;
    }
}