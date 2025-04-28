package com.example.pixabaymlgalleryjava;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;
import kotlinx.coroutines.CoroutineScope;


public class GalleryViewModel extends ViewModel {

    private final PixabayApiService pixabayService = RetrofitClient.getInstance();
    private final String apiKey = BuildConfig.PIXABAY_API_KEY;

    public MutableLiveData<String> searchQuery = new MutableLiveData<>("nature"); // Bắt đầu với query "nature"
    public LiveData<PagingData<ProcessedImage>> imagesLiveData;

    public GalleryViewModel() {
        CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);

        imagesLiveData = Transformations.switchMap(searchQuery, query -> {
            Pager<Integer, ProcessedImage> pager = new Pager<>(
                    new PagingConfig(20, 5, false),
                    () -> new PixabayPagingSource(pixabayService, apiKey, query)
            );
            LiveData<PagingData<ProcessedImage>> liveData = PagingLiveData.getLiveData(pager);
            return PagingLiveData.cachedIn(liveData, viewModelScope);
        });
    }
}