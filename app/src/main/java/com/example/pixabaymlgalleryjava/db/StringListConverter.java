package com.example.pixabaymlgalleryjava.db;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class StringListConverter {
    private static Gson gson = new Gson();

    @TypeConverter
    public static List<String> fromString(String data) {
        if (data == null) {
            return Collections.emptyList();
        }
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String toString(List<String> list) {
        return gson.toJson(list);
    }
}