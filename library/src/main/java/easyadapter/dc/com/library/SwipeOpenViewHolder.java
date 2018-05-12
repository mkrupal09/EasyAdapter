package easyadapter.dc.com.library;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Interface for interacting with a swipe open ViewHolder.
 * ViewHolders that are to be swiped <b>must</b> implement this interface
 */
public interface SwipeOpenViewHolder {

    /**
     * Returns the {@link View} that will be swiped opened and closed.
     *
     * @return a non-null view to swipe
     */
    @NonNull
    View getSwipeView();

    /**
     * Returns the {@link RecyclerView.ViewHolder} that contains the Swipe View
     *
     * @return the view holder
     */
    @NonNull
    RecyclerView.ViewHolder getViewHolder();

    /**
     * Size of the hidden view at the END of the SwipeOpenViewHolder.
     * This will be the view at the RIGHT/END of the holder when horizontal swiping is supported,
     * and will be BOTTOM/DOWN when vertical swiping is supported.
     *
     * @return the width (if horizontal swiping) or height (if vertical swiping) of the view to reveal,
     * Return 0 if you want to return to a closed position after every swipe in that direciton
     */
    float getEndHiddenViewSize();

    /**
     * Size of the hidden view at the START of the SwipeOpenViewHolder.
     * This will be the view at the LEFT/START of the holder when horizontal swiping is supported,
     * and will be TOP/UP when vertical swiping is supported.
     *
     * @return the width (if horizontal swiping) or height (if vertical swiping) of the view to reveal.
     * Return 0 if you want to return to a closed position after every swipe in that direciton
     */
    float getStartHiddenViewSize();

    /**
     * Notify the SwipeOpenHolder that the START view has become visible from a swipe.
     * Ex: This could be used to set a background color to the underlying view so that it matches your
     * hidden view during an over-swipe
     */
    void notifyStartOpen();

    /**
     * Notify the SwipeOpenHolder that the END View has become visible from a swipe
     * Ex: This could be used to set a background color to the underlying view so that it matches your
     * hidden view during an over-swipe
     */
    void notifyEndOpen();

}