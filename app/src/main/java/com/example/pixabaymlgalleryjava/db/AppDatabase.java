package com.example.pixabaymlgalleryjava.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {AnalysisResult.class}, version = 1, exportSchema = false)
@TypeConverters({StringListConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract ImageAnalysisDao imageAnalysisDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "image_analysis_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}