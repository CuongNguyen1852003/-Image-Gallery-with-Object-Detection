package com.example.pixabaymlgalleryjava;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

// Import các lớp Room
import com.example.pixabaymlgalleryjava.db.AppDatabase;
import com.example.pixabaymlgalleryjava.db.ImageAnalysisDao;


public class MainActivity extends AppCompatActivity {

    private GalleryViewModel viewModel;
    private RecyclerView recyclerView;
    private EditText searchEditText;
    private ImagePagingAdapter adapter;
    @Nullable
    private ImageAnalysisDao analysisDao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo DAO từ AppDatabase
        analysisDao = AppDatabase.getDatabase(getApplicationContext()).imageAnalysisDao();

        recyclerView = findViewById(R.id.recyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        viewModel = new ViewModelProvider(this).get(GalleryViewModel.class);

        // Khởi tạo Adapter, truyền cả DAO vào
        adapter = new ImagePagingAdapter(
                ImagePagingAdapter.PROCESSED_IMAGE_COMPARATOR,
                this,
                analysisDao
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Observe LiveData
        viewModel.imagesLiveData.observe(this, pagingData -> {
            adapter.submitData(getLifecycle(), pagingData);
        });

        // TextWatcher để cập nhật query API
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.searchQuery.setValue(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
}