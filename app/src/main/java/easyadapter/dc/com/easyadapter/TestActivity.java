package easyadapter.dc.com.easyadapter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by HB on 28/9/18.
 */
public class TestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        CategoryAdapter categoryAdapter = new CategoryAdapter(false);
        recyclerView.setAdapter(categoryAdapter);
    }
}
