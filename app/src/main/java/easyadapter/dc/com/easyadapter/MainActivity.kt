package easyadapter.dc.com.easyadapter

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import easyadapter.dc.com.easyadapter.databinding.ActivityMainBinding
import easyadapter.dc.com.easyadapter.databinding.InflaterCategoryNameBinding
import easyadapter.dc.com.library.EasyAdapter
import easyadapter.dc.com.library.EasyArrayAdapter
import easyadapter.dc.com.library.EasySpinner


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: CategoryAdapter
    private lateinit var spinnerAdapter: CategoryAdapter

    private val names: List<Category>
        get() {
            val temp = ArrayList<Category>()
            temp.add(Category.createDummy("Krupal Mehta"))
            temp.add(Category.createDummy("Aagam Mehta"))
            temp.add(Category.createDummy("Anand Patel"))
            temp.add(Category.createDummy("Sagar Panchal"))
            temp.add(Category.createDummy("Pankaj Sharma"))
            temp.add(Category.createDummy("Darshak jani"))
            temp.add(Category.createDummy("Sanket Chauhan"))
            temp.add(Category.createDummy("Dhruv"))
            temp.add(Category.createDummy("Sagar Panchal"))
            temp.add(Category.createDummy("Pankaj Sharma"))
            temp.add(Category.createDummy("Darshak jani"))
            temp.add(Category.createDummy("Sanket Chauhan"))
            temp.add(Category.createDummy("Dhruv"))
            return temp
        }

    private val categories: List<Category>
        get() {
            val list = ArrayList<Category>()
            list.add(Category.createDummy("Android Developer", "1", "1"))
            list.add(Category.createDummy("Java Developer", "4", "2"))
            list.add(Category.createDummy("Python Developer", "1", "3"))
            list.add(Category.createDummy("Php Developer", "3", "4"))
            list.add(Category.createDummy("ROR Developer", "2", "5"))


            return list
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivityLog", "true");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.recyclerView.isNestedScrollingEnabled = false

        adapterExample()
        spinnerExample()
        autocomplete()

    }

    private fun autocomplete() {

        val adapter = object : EasyArrayAdapter<Category, InflaterCategoryNameBinding>(this@MainActivity,
                R.layout.inflater_category_name, object : EasyAdapter.OnFilter<Category> {
            override fun onFilterResult(filteredList: java.util.ArrayList<Category>?) {
            }

            override fun onFilterApply(filter: Any?, model: Category): Boolean {
                return if (filter != null) model.parentId.equals("1") else false
            }
        }) {
            override fun onBind(binding: InflaterCategoryNameBinding, model: Category) {
                binding.tvName.text = model.name
            }
        }
        adapter.addAll(categories, true)
        binding.autcomplete.setItemSelectionCallback { position: Int, view: View ->
            binding.autcomplete.setText(adapter.data[position].name)
        }

        binding.autcomplete.setAdapter(adapter)

    }

    private fun adapterExample() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CategoryAdapter(true)
        binding.recyclerView.adapter = adapter

        adapter.addAll(names, true)
        adapter.notifyDataSetChanged()


        //Filter
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter.performFilter(newText, filter)
                return false
            }
        })

        //Load More
        adapter.setLoadMoreRes(R.layout.layout_progress)
        /*adapter.setOnLoadMoreListener(binding.recyclerView) { true }*/

        //Item View Event callback
        adapter.setRecyclerViewItemCheckChange { view, isCheck, model ->
            Toast.makeText(this@MainActivity, isCheck.toString(), Toast.LENGTH_SHORT).show()
        }


        //Swipe Action
        adapter.enableSwipeAction(binding.recyclerView)

        //Observe Data change (so you can show no data view if there is no data to display)
        adapter.addOnDataUpdateListener {
            if (it.size <= 0) {
                /*Toast.makeText(this@MainActivity, "No Data Found", Toast.LENGTH_SHORT).show()*/
            }
        }
    }

    private fun spinnerExample() {
        //Spinner Configuration

        binding.spRecyclerView.setPopupBackground(ContextCompat.getDrawable(this, R.drawable.rect_background))
        binding.spRecyclerView.setPopupType(EasySpinner.POPUP_TYPE_DROP_DOWN)
        binding.spRecyclerView.setPopupWidth(800)
        binding.spRecyclerView.setAnimation(R.style.Popwindow_Anim_Down)

        //Adapter Configuration
        spinnerAdapter = CategoryAdapter(false)
        spinnerAdapter.addAll(categories, true)
        spinnerAdapter.notifyDataSetChanged()
        spinnerAdapter.setRecyclerViewItemClick { view, model ->
            binding.spRecyclerView.setText(model.name)
            binding.spRecyclerView.hide()
        }

        binding.spRecyclerView.setAdapter(spinnerAdapter)
        binding.spRecyclerView.enableAutoCompleteMode { easySpinner, text ->
            spinnerAdapter.performFilter(text, spinnerFilter)
        }
        binding.spRecyclerView.setOnDropDownVisibilityListener {
            /* if(it)
             binding.spRecyclerView.setText("")*/
        }
    }

    val filter = object : EasyAdapter.OnFilter<Category> {
        override fun onFilterApply(filter: Any?, model: Category): Boolean {
            return model.name.toLowerCase().contains(filter.toString().toLowerCase())
        }

        override fun onFilterResult(filteredList: ArrayList<Category>?) {
            adapter.clear(false)
            adapter.addAll(filteredList, false)
            adapter.notifyDataSetChanged()
        }
    }

    private val spinnerFilter = object : EasyAdapter.OnFilter<Category> {
        override fun onFilterApply(filter: Any?, model: Category): Boolean {
            return model.name.toLowerCase().contains(filter.toString().toLowerCase())
        }

        override fun onFilterResult(filteredList: ArrayList<Category>?) {
            spinnerAdapter.clear(false)
            spinnerAdapter.addAll(filteredList, false)
            spinnerAdapter.notifyDataSetChanged()
        }
    }
}

