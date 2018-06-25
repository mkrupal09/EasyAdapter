package easyadapter.dc.com.easyadapter

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.widget.Toast
import easyadapter.dc.com.easyadapter.databinding.ActivityMainBinding
import easyadapter.dc.com.library.EasyAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: CategoryAdapter
    private lateinit var spinnerAdapter: CategoryAdapter

    private val temp: List<Category>
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
            return temp
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        adapterExample()
        spinnerExample()
    }

    private fun adapterExample() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CategoryAdapter()
        binding.recyclerView.adapter = adapter

        adapter.addAll(temp, true)
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
        adapter.setOnDataUpdateListener {
            if (it.size <= 0) {
                /*Toast.makeText(this@MainActivity, "No Data Found", Toast.LENGTH_SHORT).show()*/
            }
        }
    }

    private fun spinnerExample() {
        spinnerAdapter = CategoryAdapter()
        val list = ArrayList<Category>()
        list.add(Category.createDummy("Android Developer"))
        list.add(Category.createDummy("Java Developer"))
        list.add(Category.createDummy("Python Developer"))
        list.add(Category.createDummy("Php Developer"))
        spinnerAdapter.addAll(list, true)
        spinnerAdapter.notifyDataSetChanged()
        spinnerAdapter.setRecyclerViewItemClick { view, model ->
            binding.spRecyclerView.setText(model.name)
            binding.spRecyclerView.hide()
        }
        binding.spRecyclerView.setAdapter(spinnerAdapter)
        /*binding.spRecyclerView.enableAutoCompleteMode { easySpinner, text ->
            adapter.performFilter(text, filter)
        }*/
    }

    val filter = object : EasyAdapter.OnFilter<Category> {
        override fun onFilterApply(filter: Any, model: Category): Boolean {
            return model.name.toLowerCase().contains(filter.toString().toLowerCase())
        }

        override fun onFilterResult(filteredList: ArrayList<Category>?) {
            adapter.clear(false)
            adapter.addAll(filteredList, false)
            adapter.notifyDataSetChanged()
        }
    }

}

