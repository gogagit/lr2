package com.example.photogallery.models;

import com.example.photogallery.factories.ReactionFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MediaCollection {
    private Path sourceDirectory;
    private List<Path> mediaFiles;
    private List<MediaItem> mediaItems;
    private final ReactionFactory defaultFactory;

    public MediaCollection(Path directory, ReactionFactory factory) {
        this.defaultFactory = factory;
        this.sourceDirectory = directory;
        loadMedia();
    }

    private void loadMedia() {
        mediaFiles = new ArrayList<>();
        mediaItems = new ArrayList<>();

        if (sourceDirectory == null || !Files.exists(sourceDirectory)) return;

        try (var stream = Files.list(sourceDirectory)) {
            mediaFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(this::isValidImage)
                    .sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase()))
                    .toList();
        } catch (IOException e) {
            mediaFiles = new ArrayList<>();
        }

        for (int i = 0; i < mediaFiles.size(); i++) {
            mediaItems.add(defaultFactory.createMediaItem(mediaFiles.get(i), i + 1));
        }
    }

    private boolean isValidImage(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                name.endsWith(".png") || name.endsWith(".gif") ||
                name.endsWith(".bmp") || name.endsWith(".webp");
    }

    public int getItemCount() {
        return mediaItems.size();
    }

    public MediaItem getItemAt(int position) {
        int index = position - 1;
        if (index < 0 || index >= mediaItems.size()) return null;
        return mediaItems.get(index);
    }

    public void applyFactoryToItem(int position, ReactionFactory factory) {
        int index = position - 1;
        if (index < 0 || index >= mediaFiles.size()) return;

        MediaItem updated = factory.createMediaItem(mediaFiles.get(index), position);
        mediaItems.set(index, updated);
    }

    public MediaIterator createIterator() {
        return new MediaCollectionIterator();
    }

    private class MediaCollectionIterator implements MediaIterator {
        private int cursor = -1;

        @Override
        public boolean hasNext(int steps) {
            int nextPos = cursor + steps;
            return nextPos >= 0 && nextPos < mediaItems.size();
        }

        @Override
        public Object next() {
            if (mediaItems.isEmpty()) return null;

            if (hasNext(1)) {
                cursor++;
            } else {
                cursor = 0;
            }
            return mediaItems.get(cursor);
        }

        @Override
        public Object previous() {
            if (mediaItems.isEmpty()) return null;

            if (hasNext(-1)) {
                cursor--;
            } else {
                cursor = mediaItems.size() - 1;
            }
            return mediaItems.get(cursor);
        }
    }
}