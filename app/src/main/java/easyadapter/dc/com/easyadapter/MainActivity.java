package easyadapter.dc.com.easyadapter;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;

import java.util.ArrayList;

import easyadapter.dc.com.easyadapter.databinding.ActivityMainBinding;
import easyadapter.dc.com.library.EasyAdapter;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private CategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter = new CategoryAdapter());

        adapter.add(Category.Companion.createDummy("Krupal"));
        adapter.add(Category.Companion.createDummy("Dhruv"));
        adapter.add(Category.Companion.createDummy("Aagam"));
        adapter.add(Category.Companion.createDummy("Krupal"));
        adapter.add(Category.Companion.createDummy("Dhruv"));
        adapter.add(Category.Companion.createDummy("Aagam"));
        adapter.add(Category.Companion.createDummy("Krupal"));
        adapter.add(Category.Companion.createDummy("Dhruv"));
        adapter.add(Category.Companion.createDummy("Aagam"));

        adapter.notifyDataSetChanged();


        //Filter
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.performFilter(newText, new EasyAdapter.OnFilter<Category>() {
                    @Override
                    public boolean onFilterApply(@NonNull String text, @NonNull Category model) {
                        return model.getName().toLowerCase().contains(text.toLowerCase());
                    }

                    @Override
                    public void onResult(ArrayList<Category> data) {

                    }
                });
                return false;
            }
        });

        adapter.enableLoadMore(binding.recyclerView, R.layout.layout_progress, new EasyAdapter.OnLoadMoreListener() {
            @Override
            public boolean onLoadMore() {
                return true;
            }
        });


    }


}
