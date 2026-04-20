package com.example.photogallery.utils;

import com.example.photogallery.models.ProgressBuilder;
import com.example.photogallery.models.ProgressStatus;

public class ProgressDirector {
    public ProgressStatus build(ProgressBuilder builder, int current, int total) {
        builder.setCurrentIndex(current);
        builder.setTotalItems(total);
        builder.buildStatusMessage();
        return builder.getResult();
    }
}