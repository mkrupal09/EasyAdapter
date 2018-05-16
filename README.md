# EasyAdapter (Support only with DataBinding)

- Reduce Boilerplate code to create adapter and holder.
- you can filter adapter without coding much.
- You wil have load more feature with progress bar at bottom.
- includes swipe to action.
- includes View Events callbacks (ClickEvent,CheckChangeEvent)
- and many more..

Download
--------

Grab via Maven:
```xml
<dependency>
  <groupId>com.dc.easyadapter</groupId>
  <artifactId>easyadapter</artifactId>
  <version>1.1</version>
  <type>pom</type>
</dependency>
```
or Gradle:
```groovy
implementation 'com.dc.easyadapter:easyadapter:1.1'
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
adapter.setLoadMoreRes(R.layout.layout_progress)
adapter.setOnLoadMoreListener(binding.recyclerView, EasyAdapter.OnLoadMoreListener {
            if (paging != -1) {
                requestLoadMore() //Your Method
                return@OnLoadMoreListener true // Returns True if you have more data
            }
            return@OnLoadMoreListener false // Return false if you don't have more data
        })

```

#### Swipe Action

```kotlin
adapter.enableSwipeAction(binding.recyclerView)
```

```kotlin
override fun onCreatingHolder(binding: InflaterCategoryBinding, baseHolder: BaseHolder) {
        binding.llDelete.post {
            baseHolder.setEnableSwipeToDelete(binding.llCategory, 0, binding.llDelete.measuredWidth)
        }
    }
    
```
```xml
 <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        //Swipe Reveal Layout
        <LinearLayout
            android:id="@+id/llDelete"
            android:padding="10dp"
            android:layout_gravity="end"
            android:background="@android:color/holo_red_dark"
            android:layout_width="wrap_content"
            android:layout_height="match_parent">

            <ImageView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:src="@android:drawable/ic_input_delete" />
        </LinearLayout>

        <LinearLayout
            android:background="@android:color/white"
            android:id="@+id/llCategory"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:gravity="center_vertical"
            android:orientation="horizontal"
            android:padding="5dp"/>
            
</FrameLayout>
```

#### Data Observe

```kotlin
adapter.setOnDataUpdateListener {
            if (it.size <= 0) {
                Toast.makeText(this@MainActivity, "No Data Found", Toast.LENGTH_SHORT).show()
            }
        }
```

#### Pro Tips

1. Use tools attribute for previewing Layout, so you don't need to always run application

 - inside recyclerview
  
  ```xml
 tools:listitem="@layout/inflater_category"
 tools:itemCount="5"
 tools:orientation="horizontal"
 app:layoutManager="android.support.v7.widget.GridLayoutManager"

```
 
 - inside any layout
 
 ```xml
 tools:text="Sample Text"
 tools:visibility="VISIBLE"
 tools:background="@color/colorPrimary"
```
 
 - you can also use android predefine sample data by
 
```xml
 tools:text="@tools:sample/cities,first_names,us_phones,lorem,lorem/random"
 tools:background="@tools:sample/backgrounds/scenic"
 tools:src="@tools/avatars"
```
 
 - you can also make your own sample data
 
    To create your fake/sample data folder,
    just right click on the “app” folder then “new > Sample Data directory” <br />
    create new file with "filename" and write each text by new lines
 
    file contains -
    
    Georgia <br />
    Illinois <br />
    Paris <br />
    London <br />
    
    so it will randomly pick names and display in layout by

```xml
tools:text="@sample/filename" 
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
