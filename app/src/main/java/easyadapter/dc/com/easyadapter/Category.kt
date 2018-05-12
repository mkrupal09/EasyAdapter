package easyadapter.dc.com.easyadapter

import java.io.Serializable

/**
 * Created by HB on 21/3/18.
 */


public class Category : Serializable {


    var name: String? = ""
    var id: String? = ""
    var parentId: String? = ""
    var image: String? = ""

    var isSelected: Boolean = false


    companion object {
        val BUN_SEL_CATEGORY_LIST = "SEL_CATEGORY_LIST"
        const val SEND_OBJECT = "category"
        const val SEND_LIST = "category_list"

        /*const val SEND_PARENT_OBJECT = "parent_category"*/
        fun createDummy(name: String): Category {
            val category = Category()
            category.name = name
            category.id = "1"
            return category
        }

        fun createDummy(name: String, parentId: String, categoryId: String): Category {
            val category = Category()
            category.name = name
            category.parentId = parentId
            category.id = categoryId
            return category
        }

    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Category)
            return false
        return name == other.name && id == other.id
    }

}