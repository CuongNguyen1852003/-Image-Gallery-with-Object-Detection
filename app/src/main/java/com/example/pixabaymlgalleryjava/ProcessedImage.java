package com.example.pixabaymlgalleryjava;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ProcessedImage {
    private final ImageItem imageItem;
    private final List<String> aiTags;

    public ProcessedImage(ImageItem imageItem, List<String> aiTags) {
        this.imageItem = imageItem;
        this.aiTags = (aiTags != null) ? aiTags : Collections.emptyList();
    }

    public ImageItem getImageItem() { return imageItem; }
    public List<String> getAiTags() { return aiTags; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessedImage that = (ProcessedImage) o;
        return Objects.equals(imageItem, that.imageItem) && Objects.equals(aiTags, that.aiTags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageItem, aiTags);
    }
}