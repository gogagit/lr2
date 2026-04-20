package com.example.photogallery.controllers;

import com.example.photogallery.factories.ExcitedReactionFactory;
import com.example.photogallery.factories.HappyReactionFactory;
import com.example.photogallery.factories.NeutralReactionFactory;
import com.example.photogallery.factories.ReactionFactory;
import com.example.photogallery.factories.SadReactionFactory;
import com.example.photogallery.iterators.ImageIterator;
import com.example.photogallery.models.ConcreteAggregate;
import com.example.photogallery.models.MediaItem;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import javafx.util.Duration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GalleryController {

    @FXML private ImageView displayArea;
    @FXML private Label emotionLabel;
    @FXML private ComboBox<String> emotionSelector;
    @FXML private ComboBox<String> formatSelector;
    @FXML private Label counterLabel;
    @FXML private Label folderLabel;
    @FXML private TextField intervalInput;
    @FXML private ProgressBar progressBar;
    @FXML private Button playButton;
    @FXML private Button stopButton;


    private ConcreteAggregate slides;
    private ImageIterator iterator;
    private Timeline slideshowTimer;
    private double slideInterval = 2000;
    private boolean isPlaying = false;
    private Path currentDirectory;

    @FXML
    public void initialize() {
        emotionSelector.getItems().addAll(
                "😐 Нейтральный",
                "😊 Счастливый",
                "🎉 Восторженный",
                "😢 Грустный"
        );
        emotionSelector.setValue("😐 Нейтральный");

        formatSelector.getItems().addAll("*.*", "*.jpg", "*.jpeg", "*.png", "*.bmp", "*.gif", "*.webp");
        formatSelector.setValue("*.jpg");

        intervalInput.setText("2000");
        setupSlideshowTimer();

        Path defaultPath = Paths.get("src/main/resources/images");
        if (Files.exists(defaultPath)) {
            loadSlides(defaultPath, formatSelector.getValue());
        } else {
            folderLabel.setText("Папка не выбрана");
            updateEmptyState();
        }
    }

    private void setupSlideshowTimer() {
        slideshowTimer = new Timeline();
        slideshowTimer.setCycleCount(Timeline.INDEFINITE);
        refreshTimer();
    }

    private void refreshTimer() {
        slideshowTimer.getKeyFrames().setAll(
                new KeyFrame(Duration.millis(slideInterval), event -> showNextSlide())
        );
    }

    private void loadSlides(Path directory, String extension) {
        currentDirectory = directory;
        slides = new ConcreteAggregate(directory, extension, new NeutralReactionFactory());
        iterator = slides.getIterator();

        folderLabel.setText(directory.toAbsolutePath().toString());

        if (slides.isEmpty()) {
            updateEmptyState();
            return;
        }

        MediaItem firstSlide = (MediaItem) iterator.next();
        showSlide(firstSlide);
        updateProgress();
    }

    private void updateEmptyState() {
        displayArea.setImage(null);
        emotionLabel.setText("");
        counterLabel.setText("Нет изображений по выбранному фильтру");
        progressBar.setProgress(0);
    }

    private void showSlide(MediaItem item) {
        if (item == null) {
            updateEmptyState();
            return;
        }

        displayArea.setOpacity(0);
        displayArea.setImage(item.getImage());
        emotionLabel.setText(item.getEmotion());
        syncEmotionSelector(item.getEmotion());

        FadeTransition transition = new FadeTransition(Duration.millis(350), displayArea);
        transition.setFromValue(0);
        transition.setToValue(1);
        transition.play();
    }

    private void syncEmotionSelector(String emotion) {
        switch (emotion) {
            case "😊" -> emotionSelector.setValue("😊 Счастливый");
            case "🎉" -> emotionSelector.setValue("🎉 Восторженный");
            case "😢" -> emotionSelector.setValue("😢 Грустный");
            default -> emotionSelector.setValue("😐 Нейтральный");
        }
    }

    private void updateProgress() {
        if (slides == null || slides.isEmpty() || iterator == null || iterator.getCurrentIndex() < 0) {
            counterLabel.setText("Нет слайдов");
            progressBar.setProgress(0);
            return;
        }

        int current = iterator.getCurrentIndex() + 1;
        int total = slides.size();
        counterLabel.setText(current + " из " + total);
        progressBar.setProgress((double) current / total);
    }

    private ReactionFactory getFactoryBySelection() {
        String value = emotionSelector.getValue();
        if (value == null) {
            return new NeutralReactionFactory();
        }
        if (value.contains("😊")) {
            return new HappyReactionFactory();
        }
        if (value.contains("🎉")) {
            return new ExcitedReactionFactory();
        }
        if (value.contains("😢")) {
            return new SadReactionFactory();
        }
        return new NeutralReactionFactory();
    }

    private void applyEmotionToCurrentSlide() {
        if (slides == null || slides.isEmpty() || iterator == null || iterator.getCurrentIndex() < 0) {
            return;
        }

        int currentIndex = iterator.getCurrentIndex();
        slides.updateItem(currentIndex, getFactoryBySelection());
        MediaItem currentSlide = slides.getItemAt(currentIndex);
        showSlide(currentSlide);
        updateProgress();
    }

    private void showNextSlide() {
        if (iterator == null || !iterator.hasNext()) {
            return;
        }

        MediaItem item = (MediaItem) iterator.next();
        showSlide(item);
        updateProgress();
    }

    private void showPreviousSlide() {
        if (iterator == null || slides == null || slides.isEmpty()) {
            return;
        }

        MediaItem item = (MediaItem) iterator.previous();
        showSlide(item);
        updateProgress();
    }

    @FXML
    private void onChooseFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Выберите папку со слайдами");

        if (currentDirectory != null && Files.exists(currentDirectory)) {
            chooser.setInitialDirectory(currentDirectory.toFile());
        }

        Window window = displayArea.getScene() != null ? displayArea.getScene().getWindow() : null;
        java.io.File selectedDirectory = chooser.showDialog(window);
        if (selectedDirectory != null) {
            loadSlides(selectedDirectory.toPath(), formatSelector.getValue());
        }
    }

    @FXML
    private void onFormatChanged() {
        if (currentDirectory != null) {
            loadSlides(currentDirectory, formatSelector.getValue());
        }
    }

    @FXML
    private void onEmotionChanged() {
        applyEmotionToCurrentSlide();
    }

    @FXML
    private void onNextClick() {
        if (isPlaying) {
            onStopSlideshow();
        }
        showNextSlide();
    }

    @FXML
    private void onPreviousClick() {
        if (isPlaying) {
            onStopSlideshow();
        }
        showPreviousSlide();
    }

    @FXML
    private void onStartSlideshow() {
        if (slides == null || slides.isEmpty()) {
            showInfo("Нет изображений для показа");
            return;
        }

        isPlaying = true;
        slideshowTimer.play();
        playButton.setDisable(true);
        stopButton.setDisable(false);
    }

    @FXML
    private void onStopSlideshow() {
        isPlaying = false;
        slideshowTimer.stop();
        playButton.setDisable(false);
        stopButton.setDisable(true);
    }

    @FXML
    private void onApplyInterval() {
        try {
            double value = Double.parseDouble(intervalInput.getText().trim());
            if (value < 200 || value > 10000) {
                showInfo("Введите интервал от 200 до 10000 мс");
                intervalInput.setText(String.valueOf((int) slideInterval));
                return;
            }
            slideInterval = value;
            refreshTimer();
            if (isPlaying) {
                slideshowTimer.stop();
                slideshowTimer.play();
            }
        } catch (NumberFormatException e) {
            intervalInput.setText(String.valueOf((int) slideInterval));
            showInfo("Интервал должен быть числом");
        }
    }

    private void showInfo(String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setTitle("Информация");
        alert.setContentText(text);
        alert.showAndWait();
    }
}
