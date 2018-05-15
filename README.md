# EasyAdapter (Support only with DataBinding)

- Removes Boilerplate code to create adapter and holder.
- You can filter adapter without coding much.
- You wil have load more feature with loading at bottom.
- It has swipe to action functionality.
- View Events callbacks (ClickEvent,CheckChangeEvent)
- and many more..

Download
--------
```groovy
implementation 'com.dc.easyadapter:easyadapter:1.0'
```

To enable data binding
-------------------------

inside app build.gradle
```groovy
android {
    dataBinding {
        enabled = true
    }
}
```

For Kotlin also add
 ```groovy
 dependencies{
        kapt 'com.android.databinding:compiler:3.1.2'
}

apply plugin: 'kotlin-kapt' //Top at build.gradle
```

## Adapter Creation

``` kotlin
class CategoryAdapter() :EasyAdapter<Category, InflaterCategoryBinding>(R.layout.inflater_category) {

    override fun onBind(binding: InflaterCategoryBinding, model: Category) {
        binding.apply {
            tvName.text = model.name
            cbCategory.isChecked = model.isSelected
        }
    }
}
```

## Usage

#### To Handle recycler View item Events 

``` kotlin
//Override in Adapter
override fun onCreatingHolder(binding: InflaterCategoryBinding, baseHolder: BaseHolder) {
        super.onCreatingHolder(binding, baseHolder)
        binding.root.setOnClickListener(baseHolder.clickListener)
    }
```

``` kotlin
adapter.setRecyclerViewItemClick { itemView, model -> 
//Perform Operation here 
}
```

#### Filter (Search,etc..)
``` kotlin
adapter.performFilter(text, object :EasyAdapter.OnFilter<Category>{
                    override fun onFilterApply(text: String, model: Category): Boolean {
                        if (model.name?.toLowerCase()?.contains(text.toLowerCase())!!) {
                            return true //Return True if you want to include this model in this text search
                        }
                        return false //It will not include model if you return false
                    }

                    override fun onResult(data: java.util.ArrayList<Category>?) {
                        //Filtered List to do any operation
                    }
                })

```

#### Load More
``` kotlin
adapter.enableLoadMore(binding.recyclerView, EasyAdapter.OnLoadMoreListener {
            if (paging != -1) {
                requestLoadMore() //Your Method
                return@OnLoadMoreListener true // Returns True if you have more data
            }
            return@OnLoadMoreListener false // Return false if you don't have more data
        })

```

License
=======

    Copyright 2013 DC, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
