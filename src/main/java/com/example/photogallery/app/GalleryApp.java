package com.example.photogallery.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class GalleryApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        URL fxmlUrl = getClass().getResource("/fxml/gallery-view.fxml");
        if (fxmlUrl == null) {
            System.err.println("FXML file not found!");
            return;
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(loader.load(), 1240, 940);

        URL cssUrl = getClass().getResource("/css/gallery.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        stage.setTitle("Альбом впечатлений");
        stage.setScene(scene);

        
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}