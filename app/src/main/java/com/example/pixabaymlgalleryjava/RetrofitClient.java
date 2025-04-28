package com.example.pixabaymlgalleryjava;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://pixabay.com";
    private static Retrofit retrofit = null;
    private static PixabayApiService instance = null;

    public static synchronized PixabayApiService getInstance() {
        if (instance == null) {
            if (retrofit == null) {
                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
            instance = retrofit.create(PixabayApiService.class);
        }
        return instance;
    }
}