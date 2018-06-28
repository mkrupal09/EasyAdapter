package easyadapter.dc.com.easyadapter

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
        binding.apply {
            tvName.text = model.name
            tvName.isSelected = model.isSelected
        }
    }
}