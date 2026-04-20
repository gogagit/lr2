package com.example.photogallery.iterators;

public interface ImageIterator {
    boolean hasNext();
    Object next();
    Object previous();
    void reset();
    int getCurrentIndex();
}
