package com.example.photogallery.models;

public interface ProgressBuilder {
    void setCurrentIndex(int index);
    void setTotalItems(int total);
    void buildStatusMessage();
    ProgressStatus getResult();
}