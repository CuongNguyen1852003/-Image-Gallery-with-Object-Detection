package com.example.pixabaymlgalleryjava;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pixabaymlgalleryjava.db.AnalysisResult; // Import entity
import com.example.pixabaymlgalleryjava.db.ImageAnalysisDao; // Import DAO
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import coil.Coil;
import coil.request.ImageRequest;
import coil.target.Target;

public class ImageAnalyzer {

    private final Context context;
    private final ImageLabeler labeler;
    private final ImageAnalysisDao analysisDao; // Có thể null nếu không cache
    private final Executor dbExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public interface AnalysisCallback {
        void onResult(List<String> tags);
    }

    public ImageAnalyzer(Context context, @Nullable ImageAnalysisDao dao) {
        this.context = context.getApplicationContext();
        ImageLabelerOptions options = new ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.65f)
                .build();
        this.labeler = ImageLabeling.getClient(options);
        this.analysisDao = dao;
    }

    public void analyzeImage(@NonNull String imageUrl, long imageId, @NonNull AnalysisCallback callback) {
        if (analysisDao != null) {
            dbExecutor.execute(() -> {
                List<String> cachedTags = analysisDao.getAnalysisById(imageId);
                if (cachedTags != null) {
                    mainThreadHandler.post(() -> callback.onResult(cachedTags));
                } else {
                    loadAndAnalyze(imageUrl, imageId, callback);
                }
            });
        } else {
            loadAndAnalyze(imageUrl, imageId, callback);
        }
    }

    private void loadAndAnalyze(@NonNull String imageUrl, long imageId, @NonNull AnalysisCallback callback) {
        ImageRequest request = new ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .target(new Target() {
                    @Override
                    public void onSuccess(@NonNull Drawable result) {
                        if (result instanceof BitmapDrawable) {
                            Bitmap bitmap = ((BitmapDrawable) result).getBitmap();
                            processBitmapWithMLKit(bitmap, imageId, callback);
                        } else {
                            mainThreadHandler.post(() -> callback.onResult(Collections.emptyList()));
                        }
                    }
                    @Override
                    public void onError(@Nullable Drawable errorDrawable) {
                        System.err.println("Coil Error loading image: " + imageUrl);
                        mainThreadHandler.post(() -> callback.onResult(Collections.emptyList()));
                    }
                })
                .build();
        Coil.imageLoader(context).enqueue(request);
    }

    private void processBitmapWithMLKit(Bitmap bitmap, long imageId, AnalysisCallback callback) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        labeler.process(image)
                .addOnSuccessListener(labels -> {
                    List<String> extractedTags = new ArrayList<>();
                    labels.sort(Comparator.comparing(ImageLabel::getConfidence).reversed());
                    int count = 0;
                    for (ImageLabel label : labels) {
                        if (count < 5) {
                            extractedTags.add(label.getText());
                            count++;
                        } else break;
                    }

                    if (analysisDao != null && !extractedTags.isEmpty()) {
                        dbExecutor.execute(() -> {
                            analysisDao.insertAnalysis(new AnalysisResult(imageId, extractedTags));
                        });
                    }
                    mainThreadHandler.post(() -> callback.onResult(extractedTags));
                })
                .addOnFailureListener(e -> {
                    System.err.println("ML Kit Error processing image ID " + imageId + ": " + e.getMessage());
                    mainThreadHandler.post(() -> callback.onResult(Collections.emptyList()));
                });
    }
}