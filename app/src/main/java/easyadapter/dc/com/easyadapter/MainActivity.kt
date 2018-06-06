package easyadapter.dc.com.easyadapter

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.widget.Toast
import easyadapter.dc.com.easyadapter.databinding.ActivityMainBinding
import easyadapter.dc.com.library.EasyAdapter
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: CategoryAdapter

    private val temp: List<Category>
        get() {
            val temp = ArrayList<Category>()
            temp.add(Category.createDummy("Krupal"))
            temp.add(Category.createDummy("Dhruv"))
            temp.add(Category.createDummy("Aagam"))
            temp.add(Category.createDummy("Krupal"))
            temp.add(Category.createDummy("Dhruv"))
            temp.add(Category.createDummy("Aagam"))
            temp.add(Category.createDummy("Krupal"))
            temp.add(Category.createDummy("Dhruv"))
            temp.add(Category.createDummy("Krupal"))
            temp.add(Category.createDummy("Dhruv"))
            temp.add(Category.createDummy("Aagam"))
            temp.add(Category.createDummy("Krupal"))
            temp.add(Category.createDummy("Dhruv"))
            temp.add(Category.createDummy("Aagam"))
            temp.add(Category.createDummy("Krupal"))
            temp.add(Category.createDummy("Dhruv"))
            return temp
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)


        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CategoryAdapter()
        binding.recyclerView.adapter = adapter

        adapter.addAll(temp, true)
        adapter.add(Category.createDummy("Last Row"))
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
        adapter.setRecyclerViewItemClick { view, model ->
            binding.spRecyclerView.setText(model.name)
            binding.spRecyclerView.hide()
        }

        //Swipe Action
        adapter.enableSwipeAction(binding.recyclerView)

        //Observe Data change (so you can show no data view if there is no data to display)
        adapter.setOnDataUpdateListener {
            if (it.size <= 0) {
                /*Toast.makeText(this@MainActivity, "No Data Found", Toast.LENGTH_SHORT).show()*/
            }
        }

        //Experiment
        binding.spRecyclerView.setAdapter(adapter)
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
