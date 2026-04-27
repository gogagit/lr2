package com.example.photogallery.iterators;

public interface ImageIterator {
    boolean hasNext();
    Object next();
    Object previous();
    Object current();
    void reset();
    int getCurrentIndex();
    void setCurrentIndex(int index);
}
