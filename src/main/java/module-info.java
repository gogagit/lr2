module com.example.photogallery {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.photogallery.controllers to javafx.fxml;
    opens com.example.photogallery.models to javafx.base;
    opens com.example.photogallery.factories to javafx.base;

    exports com.example.photogallery.app;
    exports com.example.photogallery.controllers;
    exports com.example.photogallery.models;
    exports com.example.photogallery.factories;
    exports com.example.photogallery.iterators;
}