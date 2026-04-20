package com.example.photogallery.factories;

import javafx.scene.image.Image;
import java.nio.file.Path;

public abstract class BaseReactionFactory implements ReactionFactory {

    @Override
    public Image createMainImage(Path imagePath) {
        try {
            String uri = imagePath.toUri().toString();
            Image image = new Image(uri, true);
            return image;
        } catch (Exception e) {
            System.err.println("Error loading image: " + imagePath);
            return null;
        }
    }

    @Override
    public Image createAnimatedEffect(Path imagePath, int position) {
        return null;
    }

    @Override
    public String createDescription(Path imagePath, int position) {
        return "Item #" + position + ": " + imagePath.getFileName().toString();
    }
}