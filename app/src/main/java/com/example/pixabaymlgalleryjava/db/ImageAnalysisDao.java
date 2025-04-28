package com.example.pixabaymlgalleryjava.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ImageAnalysisDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAnalysis(AnalysisResult result); // Room tự chạy trên background thread nếu return Flowable/LiveData/suspend

    // Vì không return Flowable/LiveData/suspend, cần gọi từ background thread
    @Query("SELECT tags FROM analyses WHERE imageId = :id")
    List<String> getAnalysisById(long id);
}