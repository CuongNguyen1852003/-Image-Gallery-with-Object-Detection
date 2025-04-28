package com.example.pixabaymlgalleryjava;

import java.util.Objects;

public class ImageItem {
    private long id;
    private String webformatURL;
    private String largeImageURL;
    private String user;
    private String tags;

    // Getters
    public long getId() { return id; }
    public String getWebformatURL() { return webformatURL; }
    public String getLargeImageURL() { return largeImageURL; }
    public String getUser() { return user; }
    public String getTags() { return tags; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageItem imageItem = (ImageItem) o;
        return id == imageItem.id && Objects.equals(webformatURL, imageItem.webformatURL) && Objects.equals(tags, imageItem.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, webformatURL, tags);
    }
}