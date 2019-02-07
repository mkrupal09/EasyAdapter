package easyadapter.dc.com.easyadapter

import android.widget.ImageView
import com.bumptech.glide.Glide
import easyadapter.dc.com.easyadapter.databinding.InflaterCategoryBinding
import easyadapter.dc.com.library.EasyAdapter

/**
 * Created by Krupal on 21/3/18.
 */
class CategoryAdapter(val enableSwipToDelete: Boolean) :
        EasyAdapter<Category, InflaterCategoryBinding>(R.layout.inflater_category) {

    override fun onCreatingHolder(binding: InflaterCategoryBinding, baseHolder: EasyHolder) {
        super.onCreatingHolder(binding, baseHolder)
        binding.cbCategory.setOnCheckedChangeListener(baseHolder.checkedChangeListener)
        binding.root.setOnClickListener(baseHolder.clickListener)
        if (enableSwipToDelete) {
            binding.llDelete.post {
                baseHolder.setEnableSwipeToDelete(binding.llCategory, 0, binding.llDelete.measuredWidth)
            }
        }

    }

    override fun onBind(binding: InflaterCategoryBinding, model: Category) {
    }

    override fun onBind(binding: InflaterCategoryBinding, model: Category, holder: EasyHolder?) {
        super.onBind(binding, model, holder)
        binding.apply {
            tvName.text = model.name
            tvName.isSelected = model.isSelected
        }
        binding.ivCategoryIcon.setImageResource(R.drawable.abc_ic_ab_back_material)


        if (data.indexOf(model) % 2 == 0) {
            binding.ivCategoryIcon.layoutParams.height = 100
            binding.ivCategoryIcon.scaleType = ImageView.ScaleType.FIT_CENTER
        } else {
            binding.ivCategoryIcon.layoutParams.height = 500
            binding.ivCategoryIcon.scaleType = ImageView.ScaleType.CENTER_CROP
        }

        Glide.with(binding.ivCategoryIcon).load("https://www.healthywage.com/wp-content/uploads/2015/11/Kristin-W-BeforeAfter2-540x345.jpg").into(binding.ivCategoryIcon);
        helper.startSwipe(holder)
    }
}