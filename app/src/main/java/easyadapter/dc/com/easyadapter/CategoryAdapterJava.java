package easyadapter.dc.com.easyadapter;

import android.support.annotation.NonNull;

import easyadapter.dc.com.easyadapter.databinding.InflaterCategoryBinding;
import easyadapter.dc.com.library.EasyAdapter;

/**
 * Created by HB on 1/10/18.
 */
public class CategoryAdapterJava extends EasyAdapter<Category, InflaterCategoryBinding> {
    public CategoryAdapterJava() {
        super(R.layout.inflater_category);
    }

    @Override
    public void onBind(@NonNull InflaterCategoryBinding binding, @NonNull Category model) {
        binding.tvName.setText(model.getName());
    }
}
