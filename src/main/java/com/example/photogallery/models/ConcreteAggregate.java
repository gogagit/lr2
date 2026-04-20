package com.example.photogallery.models;

import com.example.photogallery.factories.ReactionFactory;
import com.example.photogallery.iterators.Aggregate;
import com.example.photogallery.iterators.ImageIterator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class ConcreteAggregate implements Aggregate {
    private final String filetop;
    private final Path rootDirectory;
    private final ReactionFactory reactionFactory;
    private final List<Path> imagePaths = new ArrayList<>();
    private final List<MediaItem> mediaItems = new ArrayList<>();

    public ConcreteAggregate(Path rootDirectory, String filetop, ReactionFactory reactionFactory) {
        this.rootDirectory = rootDirectory;
        this.filetop = normalizeExtension(filetop);
        this.reactionFactory = reactionFactory;
        loadFiles();
    }

    private String normalizeExtension(String extension) {
        if (extension == null || extension.isBlank() || extension.equals("*.*") || extension.equals("*")) {
            return "*";
        }

        String value = extension.trim().toLowerCase(Locale.ROOT);
        if (value.startsWith("*.")) {
            value = value.substring(1);
        }
        if (!value.startsWith(".")) {
            value = "." + value;
        }
        return value;
    }

    private void loadFiles() {
        imagePaths.clear();
        mediaItems.clear();

        if (rootDirectory == null || !Files.exists(rootDirectory)) {
            return;
        }

        try (Stream<Path> stream = Files.walk(rootDirectory)) {
            List<Path> files = stream
                    .filter(Files::isRegularFile)
                    .filter(this::matchesFormat)
                    .sorted(Comparator.comparing(path -> path.toString().toLowerCase(Locale.ROOT)))
                    .toList();

            imagePaths.addAll(files);
        } catch (IOException e) {
            imagePaths.clear();
        }

        for (int index = 0; index < imagePaths.size(); index++) {
            mediaItems.add(reactionFactory.createMediaItem(imagePaths.get(index), index + 1));
        }
    }

    private boolean matchesFormat(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);

        if (!(name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".bmp") || name.endsWith(".gif") || name.endsWith(".webp"))) {
            return false;
        }

        return "*".equals(filetop) || name.endsWith(filetop);
    }

    public int size() {
        return mediaItems.size();
    }

    public boolean isEmpty() {
        return mediaItems.isEmpty();
    }

    public Path getImagePathAt(int index) {
        if (index < 0 || index >= imagePaths.size()) {
            return null;
        }
        return imagePaths.get(index);
    }

    public MediaItem getItemAt(int index) {
        if (index < 0 || index >= mediaItems.size()) {
            return null;
        }
        return mediaItems.get(index);
    }

    public void updateItem(int index, ReactionFactory factory) {
        if (index < 0 || index >= imagePaths.size()) {
            return;
        }
        mediaItems.set(index, factory.createMediaItem(imagePaths.get(index), index + 1));
    }

    public List<Path> getImagePaths() {
        return List.copyOf(imagePaths);
    }

    @Override
    public ImageIterator getIterator() {
        return new DirectoryImageIterator();
    }

    private class DirectoryImageIterator implements ImageIterator {
        private int current = -1;

        @Override
        public boolean hasNext() {
            return !mediaItems.isEmpty();
        }

        @Override
        public Object next() {
            if (mediaItems.isEmpty()) {
                return null;
            }

            current++;
            if (current >= mediaItems.size()) {
                current = 0;
            }
            return mediaItems.get(current);
        }

        @Override
        public Object previous() {
            if (mediaItems.isEmpty()) {
                return null;
            }

            current--;
            if (current < 0) {
                current = mediaItems.size() - 1;
            }
            return mediaItems.get(current);
        }

        @Override
        public void reset() {
            current = -1;
        }

        @Override
        public int getCurrentIndex() {
            return current;
        }
    }
}
