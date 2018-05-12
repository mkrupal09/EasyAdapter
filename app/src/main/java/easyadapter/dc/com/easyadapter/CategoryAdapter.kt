package easyadapter.dc.com.easyadapter

import easyadapter.dc.com.easyadapter.databinding.InflaterCategoryBinding
import easyadapter.dc.com.library.EasyAdapter

/**
 * Created by Krupal on 21/3/18.
 */
class CategoryAdapter() :
        EasyAdapter<Category, InflaterCategoryBinding>(R.layout.inflater_category) {

    override fun onCreatingHolder(binding: InflaterCategoryBinding, baseHolder: BaseHolder) {
        super.onCreatingHolder(binding, baseHolder)


        binding.root.setOnClickListener(baseHolder.clickListener)
    }

    override fun onBind(binding: InflaterCategoryBinding, model: Category) {
        binding.apply {
            tvName.text = model.name
            tvName.isSelected = model.isSelected
        }
    }
}