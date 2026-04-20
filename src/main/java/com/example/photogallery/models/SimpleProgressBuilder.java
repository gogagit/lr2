package com.example.photogallery.models;

public class SimpleProgressBuilder implements ProgressBuilder {
    private final ProgressStatus status;

    public SimpleProgressBuilder() {
        this.status = new ProgressStatus();
    }

    @Override
    public void setCurrentIndex(int index) {
        status.setCurrentIndex(index);
    }

    @Override
    public void setTotalItems(int total) {
        status.setTotalCount(total);
    }

    @Override
    public void buildStatusMessage() {
        int current = status.getCurrentIndex();
        int total = status.getTotalCount();
        int remaining = Math.max(total - current, 0);

        String message;
        if (remaining == 0) {
            message = "последний";
        } else if (remaining == 1) {
            message = "остался 1";
        } else if (remaining >= 2 && remaining <= 4) {
            message = "осталось " + remaining;
        } else {
            message = "осталось " + remaining;
        }

        status.setStatusMessage(message);
    }

    @Override
    public ProgressStatus getResult() {
        return status;
    }
}