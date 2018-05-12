package easyadapter.dc.com.library;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HB on 20/3/18.
 */

public abstract class EasyAdapter<M, B extends ViewDataBinding> extends RecyclerView.Adapter<EasyAdapter.BaseHolder> {

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROGRESS = 0;
    private boolean loading = false;
    private boolean isLoadMoreEnabled = false;
    private int loadMoreRes = -1;
    private final ArrayList<M> data;
    private final ArrayList<M> temp;
    private int layout;
    private OnRecyclerViewItemClick<M> recyclerViewItemClick;


    public interface OnRecyclerViewItemClick<M> {
        void onRecyclerViewItemClick(View view, M model);
    }

    public interface OnHolderItemClick {
        void onHolderItemClick(View view, int position);
    }

    public interface OnFilter<M> {
        boolean onFilterApply(@NonNull String text, @NonNull M model);

        void onResult(ArrayList<M> data);
    }

    public interface OnLoadMoreListener {
        boolean onLoadMore();
    }


    public EasyAdapter(int layout) {
        data = new ArrayList<>();
        temp = new ArrayList<>();
        temp.addAll(data);
        this.layout = layout;
    }

    public void onCreatingHolder(@NonNull B binding, @NonNull BaseHolder baseHolder) {

    }

    public abstract void onBind(@NonNull B binding, @NonNull M model);


    public EasyAdapter<M, B> setRecyclerViewItemClick(OnRecyclerViewItemClick<M> recyclerViewItemClick) {
        this.recyclerViewItemClick = recyclerViewItemClick;
        return this;
    }


    public final ArrayList<M> getData() {
        return data;
    }

    public final ArrayList<M> getTemp() {
        return temp;
    }


    public void onItemClick(View view, M model) {
        if (recyclerViewItemClick != null)
            recyclerViewItemClick.onRecyclerViewItemClick(view, model);
    }


    public void clear() {
        data.clear();
        temp.clear();
    }

    private void clearFilter() {
        data.clear();
        data.addAll(temp);
    }

    public void addAll(List<M> addAll) {
        data.addAll(addAll);
        temp.addAll(addAll);
        notifyDataSetChanged();
    }

    public void add(M model) {
        data.add(model);
        temp.add(model);
        notifyDataSetChanged();
    }

    public void remove(M model) {
        data.remove(model);
        temp.remove(model);
        notifyDataSetChanged();
    }

    public void performFilter(String text, OnFilter<M> onFilter) {
        if (text.length() <= 0) {
            clearFilter();
        } else {
            data.clear();
            for (M d : temp) {
                if (d instanceof OnFilter) {
                    onFilter = (OnFilter<M>) d;
                }
                if (onFilter != null) {
                    if (onFilter.onFilterApply(text, d)) {
                        data.add(d);
                    }
                }
            }
        }
        if (onFilter != null) {
            onFilter.onResult(data);
        }
        notifyDataSetChanged();
    }


    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_ITEM) {
            BaseHolder baseHolder = new BaseHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                    layout, parent, false));
            onCreatingHolder((B) baseHolder.binding, baseHolder);
            baseHolder.setHolderItemClick(new OnHolderItemClick() {
                @Override
                public void onHolderItemClick(View view, int position) {
                    if (position != -1)
                        onItemClick(view, data.get(position));
                }
            });
            return baseHolder;
        } else {
            View view;
            if (loadMoreRes == -1) {
                view = getProgressView(parent.getContext());
            } else {
                view = LayoutInflater.from(parent.getContext()).inflate(loadMoreRes, parent, false);
            }
            return new ProgressViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseHolder holder, int position) {
        if (!holder.isLoadingView) {
            onBind((B) holder.binding, data.get(position));
        }


    }

    @Override
    public int getItemCount() {
        if (data == null) return 0;
        if (isLoadMoreEnabled && loading)
            return data.size() + 1;
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoadMoreEnabled && loading) {
            if (position == getItemCount() - 1) {
                return VIEW_PROGRESS;
            } else return VIEW_ITEM;
        }
        return VIEW_ITEM;
    }

    private View getProgressView(Context context) {
        View view = new FrameLayout(context);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleSmall);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        progressBar.setLayoutParams(lp);
        ((ViewGroup) view).addView(progressBar);
        return view;
    }


    public void setLoadMoreComplete() {
        loading = false;
        notifyDataSetChanged();
    }

    public void enableLoadMore(RecyclerView recyclerView, OnLoadMoreListener onLoadMoreListener) {
        enableLoadMore(recyclerView, loadMoreRes, onLoadMoreListener);
    }

    public void enableLoadMore(RecyclerView recyclerView, int loadMoreRes, final OnLoadMoreListener onLoadMoreListener) {
        if (recyclerView != null && onLoadMoreListener != null) {

            final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

            if (layoutManager instanceof GridLayoutManager) {
                ((GridLayoutManager) layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        if (getItemViewType(position) == VIEW_PROGRESS)
                            return ((GridLayoutManager) layoutManager).getSpanCount();
                        return 1;
                    }
                });
            }

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    int totalItemCount = layoutManager.getItemCount();
                    int lastVisibleItem = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();

                    if (layoutManager instanceof StaggeredGridLayoutManager) {
                        int[] lastVisibleItemPositions = ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(null);
                        // get maximum element within the list
                        lastVisibleItem = getLastVisibleItem(lastVisibleItemPositions);
//                        firstVisibleItem = getFirstVisibleItem(((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(null));
                    } else if (layoutManager instanceof GridLayoutManager) {
                        lastVisibleItem = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
//                        firstVisibleItem = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
                    } else if (layoutManager instanceof LinearLayoutManager) {
                        lastVisibleItem = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                    }

                    if (!loading && totalItemCount <= (lastVisibleItem + 2)) {
                        new android.os.Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                boolean previous = loading;
                                loading = onLoadMoreListener.onLoadMore();
                                if (loading != previous) {
                                    if (previous == false && loading) {
                                        notifyItemInserted(getItemCount() - 1);
                                    } else if (previous == true && loading == false) {
                                        notifyItemRemoved(getItemCount() - 1);
                                    }
                                }
                            }
                        });
                    }
                }
            });

            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
//                    loading = onLoadMoreListener.onLoadMore();
//                    notifyDataSetChanged();
                }
            }, 2000);
            isLoadMoreEnabled = true;
            this.loadMoreRes = loadMoreRes;
        }

    }


    private int getLastVisibleItem(int[] lastVisibleItemPositions) {
        int maxSize = 0;
        for (int i = 0; i < lastVisibleItemPositions.length; i++) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i];
            } else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i];
            }
        }
        return maxSize;
    }


    public static class BaseHolder extends RecyclerView.ViewHolder implements SwipeOpenViewHolder {

        private ViewDataBinding binding;
        boolean isLoadingView;
        private OnHolderItemClick holderItemClick;
        public View swipeView;
        public int startViewSize = 0, endViewSize = 0;

        public BaseHolder(ViewDataBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;

        }

        public BaseHolder(View view) {
            super(view);
        }

        void setHolderItemClick(OnHolderItemClick holderItemClick) {
            this.holderItemClick = holderItemClick;

        }

        private View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holderItemClick.onHolderItemClick(view, getAdapterPosition());
            }
        };

        public View.OnClickListener getClickListener() {
            return mOnClickListener;
        }

        public CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            }
        };

        public CompoundButton.OnCheckedChangeListener getCheckedChangeListener() {
            return checkedChangeListener;
        }

        public void setEnableSwipeToDelete(View swipeView, int startViewSize, int endViewSize) {
            this.swipeView = swipeView;
            this.startViewSize = startViewSize;
            this.endViewSize = endViewSize;
        }

        @NonNull
        @Override
        public View getSwipeView() {
            return swipeView;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder getViewHolder() {
            return this;
        }

        @Override
        public float getEndHiddenViewSize() {
            return endViewSize;
        }

        @Override
        public float getStartHiddenViewSize() {
            return startViewSize;
        }

        @Override
        public void notifyStartOpen() {

        }

        @Override
        public void notifyEndOpen() {

        }
    }

    private class ProgressViewHolder extends BaseHolder {
        ProgressViewHolder(View v) {
            super(v);
            isLoadingView = true;
            swipeView = v;
            startViewSize = 0;
            endViewSize = 0;
        }
    }


}



