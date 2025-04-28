package com.example.pixabaymlgalleryjava.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import java.util.List;

@Entity(tableName = "analyses")
@TypeConverters(StringListConverter.class)
public class AnalysisResult {
    @PrimaryKey
    public long imageId;
    public List<String> tags;

    public AnalysisResult(long imageId, List<String> tags) {
        this.imageId = imageId;
        this.tags = tags;
    }
}