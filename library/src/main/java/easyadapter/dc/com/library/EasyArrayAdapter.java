package easyadapter.dc.com.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by HB on 6/7/18.
 */
public abstract class EasyArrayAdapter<M, B> extends ArrayAdapter<M> implements Filterable, SpinnerAdapter {

    private final ArrayList<M> data;
    private final ArrayList<M> temp;
    private int layout;
    private EasyAdapter.OnFilter<M> onFilter;

    public EasyArrayAdapter(@NonNull Context context, int resource, EasyAdapter.OnFilter<M> onFilter) {
        super(context, resource);
        data = new ArrayList<>();
        temp = new ArrayList<>();
        this.onFilter = onFilter;
        this.layout = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return makeView(position, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return makeView(position, parent);
    }

    @NonNull
    private View makeView(final int position, @NonNull ViewGroup parent) {
        @SuppressLint("ViewHolder") ViewDataBinding view = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), layout, parent, false);
        onBind((B) view, data.get(position));
        return view.getRoot();
    }

    @Nullable
    @Override
    public M getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public abstract void onBind(@NonNull B binding, @NonNull M model);


    @Override
    public int getCount() {
        return data.size();
    }

    public ArrayList<M> performFilter(Object text) {
        ArrayList<M> result = new ArrayList<>();
        result.clear();
        for (M d : temp) {
            if (onFilter.onFilterApply(text, d)) {
                result.add(d);
            }
        }
        return result;
    }

    public void clear(boolean deepClean) {
        data.clear();
        if (deepClean) {
            temp.clear();
        }
    }

    public void remove(M model) {
        data.remove(model);
        temp.remove(model);
        notifyDataSetChanged();
    }


    private void clearFilter() {
        data.clear();
        data.addAll(temp);
    }

    public void add(M model) {
        data.add(model);
        temp.add(model);
        notifyDataSetChanged();
    }

    public void addAll(List<M> addAll, boolean deepCopy) {
        data.addAll(addAll);
        if (deepCopy) {
            temp.addAll(addAll);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            ArrayList<M> filtered = performFilter(constraint);
            FilterResults filterResults = new FilterResults();
            filterResults.count = filtered.size();
            filterResults.values = filtered;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            data.clear();
            if (results != null && results.values!=null) {
                data.addAll((Collection<? extends M>) results.values);
            }
            notifyDataSetChanged();
        }
    };


    public final ArrayList<M> getData() {
        return data;
    }

    public final ArrayList<M> getTemp() {
        return temp;
    }


}
