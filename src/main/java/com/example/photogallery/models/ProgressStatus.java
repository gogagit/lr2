package com.example.photogallery.models;

public class ProgressStatus {
    private int currentIndex;
    private int totalCount;
    private String statusMessage;

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int index) {
        this.currentIndex = index;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int count) {
        this.totalCount = count;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String message) {
        this.statusMessage = message;
    }

    @Override
    public String toString() {
        return statusMessage;
    }
}