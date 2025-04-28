package com.example.pixabaymlgalleryjava;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingSource;
import androidx.paging.PagingState;

// Import lại Continuation từ package đúng (nếu cần - thử không import trước)
// import kotlin.coroutines.Continuation; // Thử không import dòng này trước

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.HttpException;
import retrofit2.Response;

public class PixabayPagingSource extends PagingSource<Integer, ProcessedImage> {

    private final PixabayApiService pixabayApiService;
    private final String apiKey;
    private final String query;

    public PixabayPagingSource(PixabayApiService pixabayApiService, String apiKey, String query) {
        this.pixabayApiService = pixabayApiService;
        this.apiKey = apiKey;
        this.query = query;
    }

    // *** THỬ LẠI CHỮ KÝ VỚI CONTINUATION NHƯ LỖI YÊU CẦU ***
    // Trả về Object và thêm tham số Continuation
    @Nullable // Có thể trả về null trong trường hợp đặc biệt (nhưng thường là LoadResult)
    @Override // Annotation Override giờ có thể sẽ hoạt động
    public Object load(@NonNull LoadParams<Integer> params, @NonNull kotlin.coroutines.Continuation<? super LoadResult<Integer, ProcessedImage>> continuation) {
        Integer pageNumber = params.getKey();
        if (pageNumber == null) {
            pageNumber = 1;
        }

        int pageSize = params.getLoadSize();
        String currentQuery = (query == null || query.isEmpty()) ? "nature" : query;

        try {
            Call<PixabayResponse> call = pixabayApiService.searchImages(apiKey, currentQuery, "photo", pageNumber, pageSize);
            Response<PixabayResponse> response = call.execute(); // Vẫn gọi đồng bộ

            if (response.isSuccessful() && response.body() != null) {
                PixabayResponse data = response.body();
                List<ImageItem> images = data.getHits() != null ? data.getHits() : Collections.emptyList();

                List<ProcessedImage> processedImages = new ArrayList<>();
                for (ImageItem item : images) {
                    processedImages.add(new ProcessedImage(item, Collections.emptyList()));
                }

                Integer prevKey = (pageNumber == 1) ? null : pageNumber - 1;
                boolean isLastPage = images.isEmpty() || (pageNumber * pageSize >= data.getTotalHits());
                Integer nextKey = isLastPage ? null : pageNumber + 1;

                // Vẫn trả về đối tượng LoadResult.Page
                return new LoadResult.Page<>(processedImages, prevKey, nextKey);
            } else {
                // Vẫn trả về đối tượng LoadResult.Error
                return new LoadResult.Error<>(new HttpException(response));
            }
        } catch (IOException e) {
            return new LoadResult.Error<>(e);
        } catch (Exception e) {
            return new LoadResult.Error<>(e);
        }
        // Không sử dụng tham số 'continuation' trong code Java này
    }


    @Nullable
    @Override
    public Integer getRefreshKey(@NonNull PagingState<Integer, ProcessedImage> pagingState) {
        Integer anchorPosition = pagingState.getAnchorPosition();
        if (anchorPosition == null) return null;
        // Phải dùng kiểu đầy đủ nếu getRefreshKey không biết context của LoadResult
        PagingSource.LoadResult.Page<Integer, ProcessedImage> closestPage = pagingState.closestPageToPosition(anchorPosition);
        if (closestPage == null) return null;
        Integer prevKey = closestPage.getPrevKey();
        if (prevKey != null) return prevKey + 1;
        Integer nextKey = closestPage.getNextKey();
        if (nextKey != null) return nextKey - 1;
        return null;
    }
}