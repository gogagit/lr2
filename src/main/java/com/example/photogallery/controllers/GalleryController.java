package com.example.photogallery.controllers;

import com.example.photogallery.factories.ExcitedReactionFactory;
import com.example.photogallery.factories.HappyReactionFactory;
import com.example.photogallery.factories.NeutralReactionFactory;
import com.example.photogallery.factories.ReactionFactory;
import com.example.photogallery.factories.SadReactionFactory;
import com.example.photogallery.iterators.ImageIterator;
import com.example.photogallery.models.ConcreteAggregate;
import com.example.photogallery.models.MediaItem;
import com.example.photogallery.models.ProgressStatus;
import com.example.photogallery.models.SimpleProgressBuilder;
import com.example.photogallery.utils.ProgressDirector;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GalleryController {

    @FXML private StackPane slidePane;
    @FXML private ImageView displayArea;
    @FXML private Label emotionLabel;
    @FXML private Label overlayTextLabel;
    @FXML private Label stickerLabel;
    @FXML private Label counterLabel;
    @FXML private Label folderLabel;
    @FXML private Label musicLabel;

    @FXML private ComboBox<String> emotionSelector;
    @FXML private ComboBox<String> formatSelector;
    @FXML private ComboBox<String> stickerSelector;
    @FXML private ComboBox<String> shapeSelector;
    @FXML private ComboBox<String> animationSelector;

    @FXML private TextField intervalInput;
    @FXML private TextField overlayInput;
    @FXML private TextField animationDurationInput;

    @FXML private TextArea noteArea;
    @FXML private ProgressBar progressBar;

    @FXML private Button playButton;
    @FXML private Button stopButton;

    private ConcreteAggregate slides;
    private ImageIterator iterator;
    private Timeline slideshowTimer;
    private Shape currentShape;
    private Clip audioClip;

    private double slideInterval = 2000;
    private boolean isPlaying = false;
    private boolean updatingUi = false;

    private Path currentDirectory;
    private Path musicPath;

    private final ProgressDirector progressDirector = new ProgressDirector();

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
        formatSelector.setValue("*.*");

        stickerSelector.getItems().addAll("Нет", "⭐", "🔥", "❤️", "💬", "✅", "⚡");
        stickerSelector.setValue("Нет");

        shapeSelector.getItems().addAll("Нет фигуры", "Круг", "Квадрат", "Звезда");
        shapeSelector.setValue("Нет фигуры");

        animationSelector.getItems().addAll(
                "Плавное появление",
                "Масштаб",
                "Поворот",
                "Сдвиг",
                "Без анимации"
        );
        animationSelector.setValue("Плавное появление");

        intervalInput.setText("2000");
        animationDurationInput.setText("350");

        noteArea.setWrapText(true);
        musicLabel.setText("Музыка не выбрана");

        noteArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!updatingUi) {
                MediaItem current = getCurrentSlide();
                if (current != null) {
                    current.setNote(newValue);
                }
            }
        });

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
        stopSlideshowOnly();

        currentDirectory = directory;
        slides = new ConcreteAggregate(directory, extension, new NeutralReactionFactory());
        iterator = slides.getIterator();

        folderLabel.setText(directory.toAbsolutePath().toString());

        if (slides.isEmpty()) {
            updateEmptyState();
            return;
        }

        iterator.setCurrentIndex(0);
        showSlide(slides.getItemAt(0));
        updateProgress();
    }

    private void updateEmptyState() {
        displayArea.setImage(null);
        emotionLabel.setText("");
        overlayTextLabel.setText("");
        stickerLabel.setText("");
        noteArea.clear();
        overlayInput.clear();

        removeCurrentShape();

        counterLabel.setText("Нет изображений по выбранному фильтру");
        progressBar.setProgress(0);
    }

    private MediaItem getCurrentSlide() {
        if (slides == null || slides.isEmpty() || iterator == null || iterator.getCurrentIndex() < 0) {
            return null;
        }

        return slides.getItemAt(iterator.getCurrentIndex());
    }

    private void saveCurrentUiState() {
        if (updatingUi) {
            return;
        }

        MediaItem current = getCurrentSlide();
        if (current == null) {
            return;
        }

        current.setNote(noteArea.getText());
        current.setOverlayText(overlayInput.getText());
        current.setSticker(normalizeSticker(stickerSelector.getValue()));
        current.setShapeType(shapeSelector.getValue());
    }

    private void showSlide(MediaItem item) {
        showSlide(item, true);
    }

    private void showSlide(MediaItem item, boolean animate) {
        if (item == null) {
            updateEmptyState();
            return;
        }

        updatingUi = true;

        displayArea.setImage(item.getImage());

        emotionLabel.setText(item.getEmotion());

        overlayTextLabel.setText(item.getOverlayText());
        overlayTextLabel.setVisible(!item.getOverlayText().isBlank());

        stickerLabel.setText(item.getSticker());
        stickerLabel.setVisible(!item.getSticker().isBlank());

        noteArea.setText(item.getNote());
        overlayInput.setText(item.getOverlayText());

        syncEmotionSelector(item.getEmotion());

        stickerSelector.setValue(item.getSticker().isBlank() ? "Нет" : item.getSticker());
        shapeSelector.setValue(item.getShapeType());

        updatingUi = false;

        renderShape(item.getShapeType());

        if (animate) {
            playSelectedAnimation();
        } else {
            resetImageTransforms();
        }
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

        ProgressStatus status = progressDirector.build(
                new SimpleProgressBuilder(),
                current,
                total
        );

        counterLabel.setText(current + " из " + total + " · " + status.getStatusMessage());
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
        if (updatingUi || slides == null || slides.isEmpty() || iterator == null || iterator.getCurrentIndex() < 0) {
            return;
        }

        saveCurrentUiState();

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

        saveCurrentUiState();

        MediaItem item = (MediaItem) iterator.next();
        showSlide(item);
        updateProgress();
    }

    private void showPreviousSlide() {
        if (iterator == null || slides == null || slides.isEmpty()) {
            return;
        }

        saveCurrentUiState();

        MediaItem item = (MediaItem) iterator.previous();
        showSlide(item);
        updateProgress();
    }

    private void playSelectedAnimation() {
        resetImageTransforms();

        double duration = getAnimationDuration();
        String mode = animationSelector.getValue();

        if (mode == null || mode.equals("Без анимации")) {
            return;
        }

        switch (mode) {
            case "Масштаб" -> {
                displayArea.setScaleX(0.92);
                displayArea.setScaleY(0.92);

                ScaleTransition transition = new ScaleTransition(Duration.millis(duration), displayArea);
                transition.setToX(1);
                transition.setToY(1);
                transition.play();
            }

            case "Поворот" -> {
                displayArea.setRotate(-2);

                RotateTransition transition = new RotateTransition(Duration.millis(duration), displayArea);
                transition.setToAngle(0);
                transition.play();
            }

            case "Сдвиг" -> {
                displayArea.setTranslateX(25);
                displayArea.setOpacity(0);

                TranslateTransition transition = new TranslateTransition(Duration.millis(duration), displayArea);
                transition.setToX(0);

                FadeTransition fade = new FadeTransition(Duration.millis(duration), displayArea);
                fade.setToValue(1);

                transition.play();
                fade.play();
            }

            default -> {
                displayArea.setOpacity(0);

                FadeTransition transition = new FadeTransition(Duration.millis(duration), displayArea);
                transition.setFromValue(0);
                transition.setToValue(1);
                transition.play();
            }
        }
    }

    private void resetImageTransforms() {
        displayArea.setOpacity(1);
        displayArea.setScaleX(1);
        displayArea.setScaleY(1);
        displayArea.setRotate(0);
        displayArea.setTranslateX(0);
        displayArea.setTranslateY(0);
    }

    private double getAnimationDuration() {
        try {
            double value = Double.parseDouble(animationDurationInput.getText().trim());

            if (value < 100 || value > 3000) {
                return 350;
            }

            return value;
        } catch (Exception e) {
            return 350;
        }
    }

    private String normalizeSticker(String value) {
        if (value == null || value.equals("Нет")) {
            return "";
        }

        return value;
    }

    private void renderShape(String shapeType) {
        removeCurrentShape();

        if (shapeType == null || shapeType.equals("Нет фигуры")) {
            return;
        }

        currentShape = switch (shapeType) {
            case "Круг" -> new Circle(28);
            case "Квадрат" -> new Rectangle(56, 56);
            case "Звезда" -> createStar();
            default -> null;
        };

        if (currentShape == null) {
            return;
        }

        currentShape.setFill(Color.rgb(31, 111, 235, 0.72));
        currentShape.setStroke(Color.WHITE);
        currentShape.setStrokeWidth(3);

        StackPane.setAlignment(currentShape, Pos.BOTTOM_LEFT);
        StackPane.setMargin(currentShape, new Insets(0, 0, 34, 34));

        slidePane.getChildren().add(currentShape);
    }

    private Shape createStar() {
        Polygon star = new Polygon();

        double centerX = 35;
        double centerY = 35;
        double outer = 34;
        double inner = 15;

        for (int i = 0; i < 10; i++) {
            double angle = Math.toRadians(-90 + i * 36);
            double radius = i % 2 == 0 ? outer : inner;

            star.getPoints().add(centerX + Math.cos(angle) * radius);
            star.getPoints().add(centerY + Math.sin(angle) * radius);
        }

        return star;
    }

    private void removeCurrentShape() {
        if (currentShape != null) {
            slidePane.getChildren().remove(currentShape);
            currentShape = null;
        }
    }

    @FXML
    private void onChooseFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Выберите папку со слайдами");

        if (currentDirectory != null && Files.exists(currentDirectory)) {
            chooser.setInitialDirectory(currentDirectory.toFile());
        }

        Window window = displayArea.getScene() != null
                ? displayArea.getScene().getWindow()
                : null;

        java.io.File selectedDirectory = chooser.showDialog(window);

        if (selectedDirectory != null) {
            loadSlides(selectedDirectory.toPath(), formatSelector.getValue());
        }
    }

    @FXML
    private void onFormatChanged() {
        if (!updatingUi && currentDirectory != null) {
            loadSlides(currentDirectory, formatSelector.getValue());
        }
    }

    @FXML
    private void onEmotionChanged() {
        applyEmotionToCurrentSlide();
    }

    @FXML
    private void onApplyOverlay() {
        MediaItem current = getCurrentSlide();

        if (current == null) {
            showInfo("Сначала загрузите изображение");
            return;
        }

        saveCurrentUiState();
        showSlide(current);
        updateProgress();
    }

    @FXML
    private void onSaveNote() {
        MediaItem current = getCurrentSlide();

        if (current == null) {
            showInfo("Сначала загрузите изображение");
            return;
        }

        current.setNote(noteArea.getText());
        showInfo("Заметка сохранена для текущего слайда");
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
    private void onMoveSlideLeft() {
        moveCurrentSlide(-1);
    }

    @FXML
    private void onMoveSlideRight() {
        moveCurrentSlide(1);
    }

    private void moveCurrentSlide(int delta) {
        if (slides == null || slides.isEmpty() || iterator == null) {
            return;
        }

        saveCurrentUiState();

        int currentIndex = iterator.getCurrentIndex();
        int newIndex = slides.moveItem(currentIndex, currentIndex + delta);

        iterator.setCurrentIndex(newIndex);

        showSlide(slides.getItemAt(newIndex));
        updateProgress();
    }

    @FXML
    private void onStartSlideshow() {
        if (slides == null || slides.isEmpty()) {
            showInfo("Нет изображений для показа");
            return;
        }

        saveCurrentUiState();

        isPlaying = true;
        slideshowTimer.play();

        playButton.setDisable(true);
        stopButton.setDisable(false);

        if (musicPath != null) {
            onPlayMusic();
        }
    }

    @FXML
    private void onStopSlideshow() {
        stopSlideshowOnly();
    }

    private void stopSlideshowOnly() {
        isPlaying = false;

        if (slideshowTimer != null) {
            slideshowTimer.stop();
        }

        if (playButton != null) {
            playButton.setDisable(false);
        }

        if (stopButton != null) {
            stopButton.setDisable(true);
        }
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

    @FXML
    private void onSaveCurrentSlide() {
        if (getCurrentSlide() == null) {
            showInfo("Нет текущего слайда для сохранения");
            return;
        }

        saveCurrentUiState();
        showSlide(getCurrentSlide(), false);

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранить текущий слайд");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG изображение", "*.png")
        );
        chooser.setInitialFileName("slide_with_content.png");

        java.io.File file = chooser.showSaveDialog(displayArea.getScene().getWindow());

        if (file == null) {
            return;
        }

        try {
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);

            WritableImage snapshot = slidePane.snapshot(parameters, null);

            ImageIO.write(toBufferedImage(snapshot), "png", file);

            showInfo("Слайд сохранен вместе с текстом и рисунками");
        } catch (IOException e) {
            showInfo("Не удалось сохранить слайд: " + e.getMessage());
        }
    }

    @FXML
    private void onSaveAlbum() {
        if (slides == null || slides.isEmpty()) {
            showInfo("Нет слайдов для сохранения");
            return;
        }

        saveCurrentUiState();

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранить альбом");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Файл альбома", "*.album")
        );
        chooser.setInitialFileName("gallery.album");

        java.io.File file = chooser.showSaveDialog(displayArea.getScene().getWindow());

        if (file == null) {
            return;
        }

        Properties properties = new Properties();

        properties.setProperty("format", formatSelector.getValue());
        properties.setProperty("interval", String.valueOf((int) slideInterval));
        properties.setProperty("animation", animationSelector.getValue());
        properties.setProperty("animationDuration", animationDurationInput.getText());
        properties.setProperty("currentIndex", String.valueOf(iterator.getCurrentIndex()));
        properties.setProperty("count", String.valueOf(slides.size()));

        if (currentDirectory != null) {
            properties.setProperty("directory", currentDirectory.toAbsolutePath().toString());
        }

        if (musicPath != null) {
            properties.setProperty("musicPath", musicPath.toAbsolutePath().toString());
        }

        for (int i = 0; i < slides.size(); i++) {
            MediaItem item = slides.getItemAt(i);
            Path imagePath = slides.getImagePathAt(i);

            properties.setProperty("path." + i, imagePath.toAbsolutePath().toString());
            properties.setProperty("emotion." + i, item.getEmotion());
            properties.setProperty("overlayText." + i, item.getOverlayText());
            properties.setProperty("sticker." + i, item.getSticker());
            properties.setProperty("shapeType." + i, item.getShapeType());
            properties.setProperty("note." + i, item.getNote());
        }

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            properties.store(outputStream, "Photo gallery album configuration");
            showInfo("Альбом сохранен: порядок, заметки, режим показа и музыка записаны в файл");
        } catch (IOException e) {
            showInfo("Не удалось сохранить альбом: " + e.getMessage());
        }
    }

    @FXML
    private void onLoadAlbum() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Открыть альбом");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Файл альбома", "*.album")
        );

        java.io.File file = chooser.showOpenDialog(displayArea.getScene().getWindow());

        if (file == null) {
            return;
        }

        Properties properties = new Properties();

        try (FileInputStream inputStream = new FileInputStream(file)) {
            properties.load(inputStream);
        } catch (IOException e) {
            showInfo("Не удалось открыть альбом: " + e.getMessage());
            return;
        }

        int count = parseInt(properties.getProperty("count"), 0);

        List<Path> restoredPaths = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String rawPath = properties.getProperty("path." + i);

            if (rawPath != null) {
                restoredPaths.add(Paths.get(rawPath));
            }
        }

        slides = new ConcreteAggregate(restoredPaths, new NeutralReactionFactory());
        iterator = slides.getIterator();

        for (int i = 0; i < slides.size(); i++) {
            MediaItem item = slides.getItemAt(i);

            item.setEmotion(properties.getProperty("emotion." + i, "😐"));
            item.setOverlayText(properties.getProperty("overlayText." + i, ""));
            item.setSticker(properties.getProperty("sticker." + i, ""));
            item.setShapeType(properties.getProperty("shapeType." + i, "Нет фигуры"));
            item.setNote(properties.getProperty("note." + i, ""));
        }

        updatingUi = true;

        formatSelector.setValue(properties.getProperty("format", "*.*"));

        slideInterval = parseDouble(properties.getProperty("interval"), 2000);
        intervalInput.setText(String.valueOf((int) slideInterval));
        refreshTimer();

        animationSelector.setValue(properties.getProperty("animation", "Плавное появление"));
        animationDurationInput.setText(properties.getProperty("animationDuration", "350"));

        updatingUi = false;

        String directory = properties.getProperty("directory");
        currentDirectory = directory == null || directory.isBlank()
                ? null
                : Paths.get(directory);

        String savedMusicPath = properties.getProperty("musicPath");
        musicPath = savedMusicPath == null || savedMusicPath.isBlank()
                ? null
                : Paths.get(savedMusicPath);

        musicLabel.setText(
                musicPath == null
                        ? "Музыка не выбрана"
                        : musicPath.getFileName().toString()
        );

        prepareAudioClip();

        folderLabel.setText("Открыт альбом: " + file.getAbsolutePath());

        if (slides.isEmpty()) {
            updateEmptyState();
            showInfo("Альбом открыт, но изображения не найдены по сохраненным путям");
            return;
        }

        int currentIndex = parseInt(properties.getProperty("currentIndex"), 0);

        iterator.setCurrentIndex(currentIndex);

        showSlide(slides.getItemAt(iterator.getCurrentIndex()));
        updateProgress();
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private double parseDouble(String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @FXML
    private void onChooseMusic() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Выберите музыкальное сопровождение");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("WAV аудио", "*.wav"),
                new FileChooser.ExtensionFilter("Все файлы", "*.*")
        );

        java.io.File file = chooser.showOpenDialog(displayArea.getScene().getWindow());

        if (file == null) {
            return;
        }

        musicPath = file.toPath();
        musicLabel.setText(file.getName());

        prepareAudioClip();
    }

    @FXML
    private void onPlayMusic() {
        if (musicPath == null) {
            showInfo("Сначала выберите музыкальный файл");
            return;
        }

        prepareAudioClipIfNeeded();

        if (audioClip != null) {
            audioClip.stop();
            audioClip.setFramePosition(0);
            audioClip.loop(Clip.LOOP_CONTINUOUSLY);
            audioClip.start();
        }
    }

    @FXML
    private void onStopMusic() {
        if (audioClip != null) {
            audioClip.stop();
            audioClip.setFramePosition(0);
        }
    }

    private void prepareAudioClipIfNeeded() {
        if (audioClip == null) {
            prepareAudioClip();
        }
    }

    private void prepareAudioClip() {
        closeAudioClip();

        if (musicPath == null || !Files.exists(musicPath)) {
            return;
        }

        try (AudioInputStream stream = AudioSystem.getAudioInputStream(musicPath.toFile())) {
            audioClip = AudioSystem.getClip();
            audioClip.open(stream);

            audioClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP && audioClip != null) {
                    audioClip.setFramePosition(0);
                }
            });
        } catch (Exception e) {
            audioClip = null;
            musicLabel.setText("Не удалось загрузить музыку. Используйте WAV");
        }
    }

    private void closeAudioClip() {
        if (audioClip != null) {
            audioClip.stop();
            audioClip.close();
            audioClip = null;
        }
    }

    private BufferedImage toBufferedImage(WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        BufferedImage bufferedImage = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_ARGB
        );

        PixelReader pixelReader = image.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bufferedImage.setRGB(x, y, pixelReader.getArgb(x, y));
            }
        }

        return bufferedImage;
    }

    private void showInfo(String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setTitle("Информация");
        alert.setContentText(text);
        alert.showAndWait();
    }
}