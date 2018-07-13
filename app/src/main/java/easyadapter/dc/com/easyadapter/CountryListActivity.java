package easyadapter.dc.com.easyadapter;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import java.util.ArrayList;

import easyadapter.dc.com.easyadapter.databinding.ActivityCountryBinding;
import easyadapter.dc.com.easyadapter.databinding.InflaterCategoryNameBinding;
import easyadapter.dc.com.library.EasyAdapter;
import easyadapter.dc.com.library.EasyArrayAdapter;
import easyadapter.dc.com.library.EasyAutoComplete;

/**
 * Created by HB on 11/7/18.
 */
public class CountryListActivity extends AppCompatActivity {

    private ActivityCountryBinding binding;
    private Region selectedCountry, selectedCity, selectedState;
    private EasyArrayAdapter<Region, InflaterCategoryNameBinding> countryAdapter, stateAdapter, cityAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_country);

        initCountryAdapter();

        initStateAdapter();

        initCityAdapter();

        cityAdapter.addAll(getCities(), true);
        stateAdapter.addAll(getStates(), true);
        countryAdapter.addAll(getCountries(), true);


        binding.autocompleteState.enableAutoComplete(true);
        binding.autocompleteCity.enableAutoComplete(false);
        binding.autocompleteCountry.enableAutoComplete(true);
    }


    private void initCountryAdapter() {
        binding.autocompleteCountry.setAdapter(countryAdapter = new EasyArrayAdapter<Region, InflaterCategoryNameBinding>(this, R.layout.inflater_category_name, new EasyAdapter.OnFilter<Region>() {
            @Override
            public boolean onFilterApply(@Nullable Object filter, @NonNull Region model) {
                return true;s
            }

            @Override
            public void onFilterResult(ArrayList<Region> filteredList) {

            }
        }) {
            @Override
            public void onBind(@NonNull InflaterCategoryNameBinding binding, @NonNull Region model) {
                binding.tvName.setText(model.name);
            }
        });
        binding.autocompleteCountry.setItemSelectionCallback(new EasyAutoComplete.OnItemCallback() {
            @Override
            public void onItemCallback(int position, View view) {
                selectedCountry = countryAdapter.getData().get(position);
                binding.autocompleteCountry.setText(selectedCountry.name);

                selectedState = null;
                selectedCity = null;
                binding.autocompleteState.setText("");
                binding.autocompleteCity.setText("");
            }
        });
    }

    private void initStateAdapter() {
        binding.autocompleteState.setAdapter(stateAdapter = new EasyArrayAdapter<Region, InflaterCategoryNameBinding>(this, R.layout.inflater_category_name, new EasyAdapter.OnFilter<Region>() {
            @Override
            public boolean onFilterApply(@Nullable Object filter, @NonNull Region model) {
                if (model.parentId.equalsIgnoreCase(selectedCountry.id)) {

                    if (filter != null && !TextUtils.isEmpty(filter.toString())) {
                        return model.name.contains(filter.toString());
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void onFilterResult(ArrayList<Region> filteredList) {

            }
        }) {
            @Override
            public void onBind(@NonNull InflaterCategoryNameBinding binding, @NonNull Region model) {
                binding.tvName.setText(model.name);
            }
        });

        binding.autocompleteState.setItemSelectionCallback(new EasyAutoComplete.OnItemCallback() {
            @Override
            public void onItemCallback(int position, View view) {
                selectedState = stateAdapter.getData().get(position);
                binding.autocompleteState.setText(selectedState.name);
                selectedCity = null;
                binding.autocompleteCity.setText("");

            }
        });
    }

    private void initCityAdapter() {
        binding.autocompleteCity.setAdapter(cityAdapter = new EasyArrayAdapter<Region, InflaterCategoryNameBinding>(this, R.layout.inflater_category_name, new EasyAdapter.OnFilter<Region>() {
            @Override
            public boolean onFilterApply(@Nullable Object filter, @NonNull Region model) {
                if (model.parentId.equalsIgnoreCase(selectedState.id)) {
                    if (filter != null && !TextUtils.isEmpty(filter.toString())) {
                        return model.name.contains(filter.toString());
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void onFilterResult(ArrayList<Region> filteredList) {

            }
        }) {
            @Override
            public void onBind(@NonNull InflaterCategoryNameBinding binding, @NonNull Region model) {
                binding.tvName.setText(model.name);
            }
        });
        binding.autocompleteCity.setItemSelectionCallback(new EasyAutoComplete.OnItemCallback() {
            @Override
            public void onItemCallback(int position, View view) {
                selectedCity = cityAdapter.getData().get(position);
                binding.autocompleteCity.setText(selectedCity.name);
            }
        });
    }


    public ArrayList<Region> getCountries() {
        ArrayList<Region> regions = new ArrayList<>();
        regions.add(Region.createDummy("1", "India", "0"));
        regions.add(Region.createDummy("2", "Canada", "0"));
        regions.add(Region.createDummy("3", "USA", "0"));
        regions.add(Region.createDummy("4", "Australia", "0"));
        return regions;
    }

    public ArrayList<Region> getStates() {
        ArrayList<Region> regions = new ArrayList<>();
        regions.add(Region.createDummy("1", "Gujarat", "1"));
        regions.add(Region.createDummy("2", "Maharashtra", "1"));
        regions.add(Region.createDummy("3", "Uttar Pradesh", "1"));
        regions.add(Region.createDummy("4", "California", "3"));
        return regions;
    }

    public ArrayList<Region> getCities() {
        ArrayList<Region> regions = new ArrayList<>();
        regions.add(Region.createDummy("1", "Ahmedabad", "1"));
        regions.add(Region.createDummy("2", "Baroda", "1"));
        regions.add(Region.createDummy("3", "Mumbai", "2"));
        regions.add(Region.createDummy("4", "Los Angeles", "4"));
        regions.add(Region.createDummy("5", "San Diego", "4"));
        regions.add(Region.createDummy("6", "San Francisco", "4"));
        return regions;
    }
}
