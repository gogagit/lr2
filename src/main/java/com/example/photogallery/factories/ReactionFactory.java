package com.example.photogallery.factories;

import com.example.photogallery.models.MediaItem;
import javafx.scene.image.Image;
import java.nio.file.Path;

public interface ReactionFactory {
    Image createMainImage(Path imagePath);
    Image createAnimatedEffect(Path imagePath, int position);
    String createDescription(Path imagePath, int position);
    String createReactionEmoji();

    default MediaItem createMediaItem(Path imagePath, int position) {
        return new MediaItem(
                createMainImage(imagePath),
                createAnimatedEffect(imagePath, position),
                createDescription(imagePath, position),
                createReactionEmoji(),
                imagePath
        );
    }
}
