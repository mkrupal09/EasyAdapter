package easyadapter.dc.com.easyadapter;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

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

        adapter.addAll(getTemp());
        adapter.add(Category.Companion.createDummy("Last Row"));

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

        adapter.setLoadMoreRes(R.layout.layout_progress);

        adapter.setOnLoadMoreListener(binding.recyclerView, new EasyAdapter.OnLoadMoreListener() {
            @Override
            public boolean onLoadMore() {
                return true;
            }
        });

        adapter.setRecyclerViewItemCheckChange(new EasyAdapter.OnRecyclerViewItemCheckChange<Category>() {
            @Override
            public void onRecyclerViewItemCheckChange(View view, boolean isCheck, Category model) {
                Toast.makeText(MainActivity.this, String.valueOf(isCheck), Toast.LENGTH_SHORT).show();
            }
        });

        adapter.enableSwipeAction(binding.recyclerView);

    }

    @NonNull
    private List<Category> getTemp() {
        List<Category> temp = new ArrayList<>();
        temp.add(Category.Companion.createDummy("Krupal"));
        temp.add(Category.Companion.createDummy("Dhruv"));
        temp.add(Category.Companion.createDummy("Aagam"));
        temp.add(Category.Companion.createDummy("Krupal"));
        temp.add(Category.Companion.createDummy("Dhruv"));
        temp.add(Category.Companion.createDummy("Aagam"));
        temp.add(Category.Companion.createDummy("Krupal"));
        temp.add(Category.Companion.createDummy("Dhruv"));
        return temp;
    }


}
