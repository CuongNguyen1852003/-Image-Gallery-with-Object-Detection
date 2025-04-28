package com.example.pixabaymlgalleryjava;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import coil.Coil;
import coil.request.ImageRequest;
import com.example.pixabaymlgalleryjava.db.ImageAnalysisDao; // Import DAO
import com.example.pixabaymlgalleryjava.R; // Thay bằng package của bạn nếu khác
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class ImagePagingAdapter extends PagingDataAdapter<ProcessedImage, ImagePagingAdapter.ImageViewHolder> {

    private final ImageAnalyzer imageAnalyzer;
    private final ConcurrentHashMap<Long, Boolean> analysisInProgress = new ConcurrentHashMap<>();
    private final ImageAnalysisDao analysisDao; // Có thể null

    public ImagePagingAdapter(@NonNull DiffUtil.ItemCallback<ProcessedImage> diffCallback, Context context, @Nullable ImageAnalysisDao dao) {
        super(diffCallback);
        this.analysisDao = dao;
        this.imageAnalyzer = new ImageAnalyzer(context, this.analysisDao);
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ProcessedImage processedImage = getItem(position);
        if (processedImage == null) {
            holder.clear();
            return;
        }

        long imageId = processedImage.getImageItem().getId();
        holder.bindBasic(processedImage.getImageItem());

        boolean isAnalyzing = analysisInProgress.getOrDefault(imageId, false);
        List<String> currentAiTags = processedImage.getAiTags(); // Ban đầu sẽ rỗng

        // Kiểm tra xem đã có tag từ cache chưa (do ImageAnalyzer xử lý)
        // Vì PagingSource không load lại tag, ta phải trigger analyze nếu chưa có
        if (analysisDao != null) { // Chỉ kiểm tra cache nếu DAO tồn tại
            // Tối ưu: Kiểm tra cache MỘT LẦN ở đây thay vì trong ImageAnalyzer để tránh gọi DB nhiều lần
            new Thread(() -> { // Chạy kiểm tra cache trên background thread
                List<String> cachedTags = analysisDao.getAnalysisById(imageId);
                holder.itemView.post(() -> { // Post kết quả về UI thread
                    ProcessedImage currentItemInHolder = getItem(holder.getBindingAdapterPosition());
                    if (currentItemInHolder != null && currentItemInHolder.getImageItem().getId() == imageId) {
                        if (cachedTags != null && !cachedTags.isEmpty()) {
                            holder.aiTagsTextView.setText("AI Tags: " + String.join(", ", cachedTags));
                            analysisInProgress.remove(imageId); // Đã có cache, không cần phân tích
                        } else {
                            // Không có cache, tiến hành phân tích nếu chưa làm
                            triggerAnalysisIfNeeded(holder, processedImage, imageId);
                        }
                    }
                });
            }).start();
        } else {
            // Không có cache, tiến hành phân tích nếu chưa làm
            triggerAnalysisIfNeeded(holder, processedImage, imageId);
        }
    }

    // Hàm helper để trigger phân tích
    private void triggerAnalysisIfNeeded(@NonNull ImageViewHolder holder, @NonNull ProcessedImage processedImage, long imageId) {
        boolean isAnalyzing = analysisInProgress.getOrDefault(imageId, false);
        if (!isAnalyzing) { // Chỉ trigger nếu chưa phân tích
            holder.aiTagsTextView.setText("AI Tags: Analyzing...");
            analysisInProgress.put(imageId, true);

            imageAnalyzer.analyzeImage(
                    processedImage.getImageItem().getWebformatURL(),
                    imageId,
                    tags -> {
                        ProcessedImage currentItemInHolder = getItem(holder.getBindingAdapterPosition());
                        if (currentItemInHolder != null && currentItemInHolder.getImageItem().getId() == imageId) {
                            if (tags.isEmpty()) {
                                holder.aiTagsTextView.setText("AI Tags: (None detected)");
                            } else {
                                holder.aiTagsTextView.setText("AI Tags: " + String.join(", ", tags));
                            }
                        }
                        analysisInProgress.remove(imageId);
                    }
            );
        } else {
            holder.aiTagsTextView.setText("AI Tags: Analyzing..."); // Vẫn đang phân tích
        }
    }


    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView pixabayTagsTextView;
        final TextView aiTagsTextView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            pixabayTagsTextView = itemView.findViewById(R.id.textPixabayTags);
            aiTagsTextView = itemView.findViewById(R.id.textAiTags);
        }

        public void bindBasic(ImageItem imageItem) {
            Context context = itemView.getContext();
            ImageRequest request = new ImageRequest.Builder(context)
                    .data(imageItem.getWebformatURL())
                    .target(imageView)
                    .placeholder(R.drawable.ic_launcher_background) // Thay bằng placeholder của bạn
                    .error(R.drawable.ic_launcher_foreground)     // Thay bằng error drawable
                    .crossfade(true)
                    .build();
            Coil.imageLoader(context).enqueue(request);
            pixabayTagsTextView.setText("Pixabay Tags: " + imageItem.getTags());
            // Không set aiTagsTextView ở đây nữa, để onBindViewHolder xử lý
            aiTagsTextView.setText("AI Tags: Loading..."); // Trạng thái chờ ban đầu
        }

        public void clear() {
            imageView.setImageDrawable(null);
            pixabayTagsTextView.setText("");
            aiTagsTextView.setText("");
        }
    }

    public static final DiffUtil.ItemCallback<ProcessedImage> PROCESSED_IMAGE_COMPARATOR =
            new DiffUtil.ItemCallback<ProcessedImage>() {
                @Override
                public boolean areItemsTheSame(@NonNull ProcessedImage oldItem, @NonNull ProcessedImage newItem) {
                    return oldItem.getImageItem().getId() == newItem.getImageItem().getId();
                }
                @Override
                public boolean areContentsTheSame(@NonNull ProcessedImage oldItem, @NonNull ProcessedImage newItem) {
                    return oldItem.equals(newItem);
                }
            };
}