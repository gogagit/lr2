package com.example.photogallery.models;

public interface MediaIterator {
    boolean hasNext(int step);
    Object next();
    Object previous();
}