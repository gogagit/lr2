package com.example.photogallery.models;

import javafx.scene.image.Image;

import java.nio.file.Path;

public class MediaItem {
    private final Image mainImage;
    private final Image animatedImage;
    private final String description;
    private String emotion;
    private final Path sourcePath;

    private String overlayText = "";
    private String sticker = "";
    private String shapeType = "Нет фигуры";
    private String note = "";

    public MediaItem(Image image, Image animation, String notes, String reaction) {
        this(image, animation, notes, reaction, null);
    }

    public MediaItem(Image image, Image animation, String notes, String reaction, Path sourcePath) {
        this.mainImage = image;
        this.animatedImage = animation;
        this.description = notes;
        this.emotion = reaction;
        this.sourcePath = sourcePath;
    }

    public Image getImage() {
        return mainImage;
    }

    public Image getAnimation() {
        return animatedImage;
    }

    public String getDescription() {
        return description;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion == null ? "😐" : emotion;
    }

    public Path getSourcePath() {
        return sourcePath;
    }

    public String getOverlayText() {
        return overlayText;
    }

    public void setOverlayText(String overlayText) {
        this.overlayText = overlayText == null ? "" : overlayText;
    }

    public String getSticker() {
        return sticker;
    }

    public void setSticker(String sticker) {
        this.sticker = sticker == null ? "" : sticker;
    }

    public String getShapeType() {
        return shapeType;
    }

    public void setShapeType(String shapeType) {
        this.shapeType = shapeType == null || shapeType.isBlank() ? "Нет фигуры" : shapeType;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note == null ? "" : note;
    }

    public void copyUserContentFrom(MediaItem source) {
        if (source == null) {
            return;
        }
        setOverlayText(source.getOverlayText());
        setSticker(source.getSticker());
        setShapeType(source.getShapeType());
        setNote(source.getNote());
    }
}
