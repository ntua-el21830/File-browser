package com.medialab.ui;

import java.io.File;

import com.medialab.app.model.User;
import com.medialab.launcher.Boot;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    private Boot boot;

    @Override
    public void init() {
        boot = new Boot();
        System.out.println("Ο φάκελος αποθήκευσης είναι: " + System.getProperty("user.home") + File.separator + "medialab");
        boot.initFromJson();
        boot.installShutdownSave();
    }

    @Override
    public void start(Stage primaryStage) {
        LoginView loginView = new LoginView(boot, (User authenticatedUser) -> showMainDashboard(primaryStage, authenticatedUser));

        primaryStage.setTitle("Sign in");
        primaryStage.setScene(new Scene(loginView.getRoot(), 420, 320));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void showMainDashboard(Stage loginStage, User user) {
        MainView mainView = new MainView(boot, user);
        
        Stage mainStage = new Stage();
        mainStage.setTitle("MediaLab Documents");

        Scene scene = new Scene(mainView.getRoot(), 1024, 768);
        
        mainStage.setScene(scene);
        mainStage.show();
        
        loginStage.close();
    }

    @Override
    public void stop() {
    
        if (boot != null) {
            System.out.println("Η εφαρμογή τερματίζεται. Τελική αποθήκευση στο JSON.");
            boot.saveAll();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}