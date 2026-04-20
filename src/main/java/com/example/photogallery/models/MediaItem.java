package com.example.photogallery.models;

import javafx.scene.image.Image;

public class MediaItem {
    private final Image mainImage;
    private final Image animatedImage;
    private final String description;
    private final String emotion;

    public MediaItem(Image image, Image animation, String notes, String reaction) {
        this.mainImage = image;
        this.animatedImage = animation;
        this.description = notes;
        this.emotion = reaction;
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
}