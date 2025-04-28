package com.example.pixabaymlgalleryjava;

import java.util.List;

public class PixabayResponse {
    private int totalHits;
    private List<ImageItem> hits;
    private int total;

    public int getTotalHits() { return totalHits; }
    public List<ImageItem> getHits() { return hits; }
    public int getTotal() { return total; }
}