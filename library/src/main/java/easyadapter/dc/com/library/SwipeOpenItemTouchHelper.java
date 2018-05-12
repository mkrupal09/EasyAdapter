package easyadapter.dc.com.library;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;

import java.util.ArrayList;
import java.util.List;

/*
 * Adapted from Google's android.support.v7.widget.helper.ItemTouchHelper
 * https://github.com/android/platform_frameworks_support/blob/master/v7/recyclerview/src/android/support/v7/widget/helper/ItemTouchHelper.java
 *
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Helper class to allow for swiping open hidden views of a RecyclerView.
 * Adapted from and based off of Google's {@link android.support.v7.widget.helper.ItemTouchHelper}
 */
public class SwipeOpenItemTouchHelper extends RecyclerView.ItemDecoration
        implements RecyclerView.OnChildAttachStateChangeListener {

    private static final String OPENED_STATES = "opened_states";

    /**
     * Up direction, used for swipe to open
     */
    public static final int UP = 1;

    /**
     * Down direction, used for swipe to open
     */
    public static final int DOWN = 1 << 1;

    /**
     * Left direction, used for swipe to open
     */
    public static final int LEFT = 1 << 2;

    /**
     * Right direction, used for swipe to open
     */
    public static final int RIGHT = 1 << 3;

    // If you change these relative direction values, update Callback#convertToAbsoluteDirection,
    // Callback#convertToRelativeDirection.
    /**
     * Horizontal start direction. Resolved to LEFT or RIGHT depending on RecyclerView's layout
     * direction
     */
    public static final int START = LEFT << 2;

    /**
     * Horizontal end direction. Resolved to LEFT or RIGHT depending on RecyclerView's layout
     * direction
     */
    public static final int END = RIGHT << 2;

    /**
     * SwipeOpenItemTouchHelper is in idle state. At this state, either there is no related motion
     * event by the user or latest motion events have not yet triggered a swipe or drag.
     */
    public static final int ACTION_STATE_IDLE = 0;

    /**
     * A View is currently being swiped.
     */
    public static final int ACTION_STATE_SWIPE = 1;

    /**
     * Animation type for views which are swiped and will animate back to an open or closed position
     */
    public static final int ANIMATION_TYPE_SWIPE = 1 << 2;

    private static final String TAG = "SwipeOpenHelper";

    private static final boolean DEBUG = false;

    private static final int ACTIVE_POINTER_ID_NONE = -1;

    private static final int DIRECTION_FLAG_COUNT = 8;

    private static final int ACTION_MODE_IDLE_MASK = (1 << DIRECTION_FLAG_COUNT) - 1;

    private static final int ACTION_MODE_SWIPE_MASK = ACTION_MODE_IDLE_MASK << DIRECTION_FLAG_COUNT;

    /**
     * Re-use array to calculate dx dy for a ViewHolder
     */
    private final float[] tmpPosition = new float[2];

    /**
     * Currently selected view holder
     */
    private SwipeOpenViewHolder selected = null;

    /**
     * Initial touch point for swipe
     */
    float initialTouchX;
    float initialTouchY;

    /**
     * The diff between the last event and initial touch.
     */
    float dX;
    float dY;

    /**
     * The coordinates of the selected view at the time it is selected. We record these values
     * when action starts so that we can consistently position it even if LayoutManager moves the
     * View.
     */
    float selectedStartX;

    float selectedStartY;

    /**
     * The pointer we are tracking.
     */
    int activePointerId = ACTIVE_POINTER_ID_NONE;

    /**
     * Developer callback which controls the behavior of ItemTouchHelper.
     */
    Callback callback;

    /**
     * Current mode.
     */
    int actionState = ACTION_STATE_IDLE;

    /**
     * The direction flags obtained from unmasking
     * {@link Callback#getAbsMovementFlags(RecyclerView, RecyclerView.ViewHolder)} for the
     * current
     * action state.
     */
    int selectedFlags;

    /**
     * When a View is swiped and needs to return to an open or closed position, we create a Recover
     * Animation and animate it to its location using this custom Animator, instead of using
     * framework Animators.
     * Using framework animators has the side effect of clashing with ItemAnimator, creating
     * jumpy UIs.
     */
    private List<RecoverAnimation> recoverAnimations = new ArrayList<>();

    private int slop;

    private RecyclerView recyclerView;

    private boolean isRtl;

    /**
     * Flag for if a the SwipeOpenItemTouchHelper should prevent swipe-to-opens of Start or End Views
     * that
     * have a size of 0
     * DEFAULT: false
     */
    private boolean preventZeroSizeViewSwipes = false;

    /**
     * Flag for if any open SwipeOpenViewHolders should be close when the view is scrolled or if
     * a new view holder is swiped
     * DEFAULT: true
     */
    private boolean closeOnAction = true;

    private SwipeOpenViewHolder prevSelected;

    /**
     * Used for detecting fling swipe
     */
    private VelocityTracker velocityTracker;

    private SparseArray<SavedOpenState> openedPositions = new SparseArray<>();

    /**
     * Data Observer that allow us to remove any opened positions when something is removed from the
     * adapter
     */
    private final RecyclerView.AdapterDataObserver adapterDataObserver =
            new RecyclerView.AdapterDataObserver() {

                @Override
                public void onChanged() {
                    // if notifyDataSetChanged is used we cannot know if opened holders should stay open,
                    // so close all of them
                    openedPositions.clear();
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    // if an item is removed, we need to remove the opened position
                    for (int i = positionStart; i < positionStart + itemCount; i++) {
                        openedPositions.remove(i);
                    }
                }
            };

    private final RecyclerView.OnItemTouchListener mOnItemTouchListener =
            new RecyclerView.OnItemTouchListener() {
                @Override
                public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent event) {
                    if (DEBUG) {
                        Log.d(TAG, "intercept: x:" + event.getX() + ",y:" + event.getY() + ", " + event);
                    }
                    final int action = event.getAction();
                    if (action == MotionEvent.ACTION_DOWN) {
                        activePointerId = event.getPointerId(0);
                        initialTouchX = event.getX();
                        initialTouchY = event.getY();
                        obtainVelocityTracker();
                        if (selected == null) {
                            final RecoverAnimation animation = findAnimation(event);
                            if (animation != null) {
                                initialTouchX -= animation.x;
                                initialTouchY -= animation.y;
                                endRecoverAnimation(animation.viewHolder);
                                select(animation.viewHolder, animation.actionState);
                                updateDxDy(event, selectedFlags, 0);
                            }
                        }
                    } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                        activePointerId = ACTIVE_POINTER_ID_NONE;
                        select(null, ACTION_STATE_IDLE);
                    } else if (activePointerId != ACTIVE_POINTER_ID_NONE) {
                        // in a non scroll orientation, if distance change is above threshold, we
                        // can select the item
                        final int index = event.findPointerIndex(activePointerId);
                        if (DEBUG) {
                            Log.d(TAG, "pointer index " + index);
                        }
                        if (index >= 0) {
                            checkSelectForSwipe(action, event, index);
                        }
                    }
                    if (velocityTracker != null) {
                        velocityTracker.addMovement(event);
                    }
                    return selected != null;
                }

                @Override
                public void onTouchEvent(RecyclerView recyclerView, MotionEvent event) {
                    if (DEBUG) {
                        Log.d(TAG, "on touch: x:" + initialTouchX + ",y:" + initialTouchY + ", :" + event);
                    }
                    if (velocityTracker != null) {
                        velocityTracker.addMovement(event);
                    }
                    if (activePointerId == ACTIVE_POINTER_ID_NONE) {
                        return;
                    }
                    final int action = event.getActionMasked();
                    final int activePointerIndex = event.findPointerIndex(activePointerId);
                    if (activePointerIndex >= 0) {
                        checkSelectForSwipe(action, event, activePointerIndex);
                    }
                    if (selected == null) {
                        return;
                    }
                    switch (action) {
                        case MotionEvent.ACTION_MOVE: {
                            // Find the index of the active pointer and fetch its position
                            if (activePointerIndex >= 0) {
                                updateDxDy(event, selectedFlags, activePointerIndex);
                                SwipeOpenItemTouchHelper.this.recyclerView.invalidate();
                            }
                            break;
                        }
                        case MotionEvent.ACTION_CANCEL:
                            if (velocityTracker != null) {
                                velocityTracker.clear();
                            }
                            // fall through
                        case MotionEvent.ACTION_UP:
                            select(null, ACTION_STATE_IDLE);
                            activePointerId = ACTIVE_POINTER_ID_NONE;
                            break;
                        case MotionEvent.ACTION_POINTER_UP: {
                            final int pointerIndex = event.getActionIndex();
                            final int pointerId = event.getPointerId(pointerIndex);
                            if (pointerId == activePointerId) {
                                // This was our active pointer going up. Choose a new
                                // active pointer and adjust accordingly.
                                final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                                activePointerId = event.getPointerId(newPointerIndex);
                                updateDxDy(event, selectedFlags, pointerIndex);
                            }
                            break;
                        }
                    }
                }

                @Override
                public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
                    if (!disallowIntercept) {
                        return;
                    }
                    select(null, ACTION_STATE_IDLE);
                }
            };

    private final RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (closeOnAction && (dx != 0 || dy != 0)) {
                if (prevSelected != null && (Math.abs(prevSelected.getSwipeView().getTranslationX()) > 0
                        || Math.abs(prevSelected.getSwipeView().getTranslationY()) > 0)) {
                    closeOpenHolder(prevSelected);
                    prevSelected = null;
                }
                // if we've got any open positions saved from a rotation, close those
                if (openedPositions.size() > 0) {
                    for (int i = 0; i < openedPositions.size(); i++) {
                        RecyclerView.ViewHolder holder =
                                recyclerView.findViewHolderForAdapterPosition(openedPositions.keyAt(i));
                        if (holder instanceof SwipeOpenViewHolder) {
                            closeOpenHolder((SwipeOpenViewHolder) holder);
                        }
                        openedPositions.removeAt(i);
                    }
                }
            }
        }
    };

    /**
     * Creates an SwipeOpenItemTouchHelper that will work with the given Callback.
     * <p>
     * You can attach SwipeOpenItemTouchHelper to a RecyclerView via
     * {@link #attachToRecyclerView(RecyclerView)}. Upon attaching, it will add an item decoration,
     * an onItemTouchListener and a Child attach / detach listener to the RecyclerView.
     *
     * @param callback The Callback which controls the behavior of this touch helper.
     */
    public SwipeOpenItemTouchHelper(Callback callback) {
        this.callback = callback;
    }

    private static boolean hitTest(View child, float x, float y, float left, float top) {
        return x >= left && x <= left + child.getWidth() && y >= top && y <= top + child.getHeight();
    }

    /**
     * Attaches the SwipeOpenItemTouchHelper to the provided RecyclerView. If the helper is already
     * attached to a RecyclerView, it will first detach from the previous one. You can call this
     * method with {@code null} to detach it from the current RecyclerView.
     * NOTE: RecyclerView must have an adapter set in order to allow adapter data observing to
     * correctly save opened positions state.
     *
     * @param recyclerView The RecyclerView instance to which you want to add this helper or
     *                     {@code null} if you want to remove SwipeOpenItemTouchHelper from the current
     *                     RecyclerView.
     */
    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) {
        if (this.recyclerView == recyclerView) {
            return; // nothing to do
        }
        if (this.recyclerView != null) {
            destroyCallbacks();
        }
        this.recyclerView = recyclerView;
        if (this.recyclerView != null) {
            setupCallbacks();
        }
    }

    /**
     * Flag to determine if any open SwipeViewHolders are closed when the RecyclerView is scrolled,
     * or when a new view holder is swiped.
     * Default value is true.
     *
     * @param closeOnAction true to close on an action, false to keep them open
     */
    public void setCloseOnAction(boolean closeOnAction) {
        this.closeOnAction = closeOnAction;
    }

    /**
     * Flag to prevent SwipeOpenItemTouchHelper from swiping open zero-sized Start or End views.
     *
     * @param preventZeroSizeViewSwipes true to prevent swiping open zero sized views, false to allow
     */
    public void setPreventZeroSizeViewSwipes(boolean preventZeroSizeViewSwipes) {
        this.preventZeroSizeViewSwipes = preventZeroSizeViewSwipes;
    }

    private void setupCallbacks() {
        ViewConfiguration vc = ViewConfiguration.get(recyclerView.getContext());
        slop = vc.getScaledTouchSlop();
        recyclerView.addItemDecoration(this);
        recyclerView.addOnItemTouchListener(mOnItemTouchListener);
        recyclerView.addOnChildAttachStateChangeListener(this);
        recyclerView.addOnScrollListener(scrollListener);
        Resources resources = recyclerView.getContext().getResources();
        isRtl = false;
        if (recyclerView.getAdapter() == null) {
            throw new IllegalStateException(
                    "SwipeOpenItemTouchHelper.attachToRecyclerView must be called after "
                            + "the RecyclerView's adapter has been set.");
        } else {
            recyclerView.getAdapter().registerAdapterDataObserver(adapterDataObserver);
        }
    }

    private void destroyCallbacks() {
        recyclerView.removeItemDecoration(this);
        recyclerView.removeOnItemTouchListener(mOnItemTouchListener);
        recyclerView.removeOnChildAttachStateChangeListener(this);
        if (recyclerView.getAdapter() != null) {
            recyclerView.getAdapter().unregisterAdapterDataObserver(adapterDataObserver);
        }

        // clean all attached
        final int recoverAnimSize = recoverAnimations.size();
        for (int i = recoverAnimSize - 1; i >= 0; i--) {
            final RecoverAnimation recoverAnimation = recoverAnimations.get(0);
            callback.clearView(recyclerView, recoverAnimation.viewHolder);
        }
        recoverAnimations.clear();
        releaseVelocityTracker();
        isRtl = false;
    }

    private void getSelectedDxDy(float[] outPosition) {
        if ((selectedFlags & (LEFT | RIGHT)) != 0) {
            outPosition[0] = selectedStartX + dX - selected.getSwipeView().getLeft();
        } else {
            outPosition[0] = selected.getSwipeView().getTranslationX();
        }
        if ((selectedFlags & (UP | DOWN)) != 0) {
            outPosition[1] = selectedStartY + dY - selected.getSwipeView().getTop();
        } else {
            outPosition[1] = selected.getSwipeView().getTranslationY();
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        float dx = 0, dy = 0;
        if (selected != null) {
            getSelectedDxDy(tmpPosition);
            dx = tmpPosition[0];
            dy = tmpPosition[1];
        }
        callback.onDrawOver(c, parent, selected, recoverAnimations, actionState, dx, dy);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

        float dx = 0, dy = 0;
        if (selected != null) {
            getSelectedDxDy(tmpPosition);
            dx = tmpPosition[0];
            dy = tmpPosition[1];
        }

        // checks if we need to prevent zero-size swipe-to-opens
        if (selected != null && preventZeroSizeViewSwipes) {
            if (preventHorizontalAction(selected, dx)) {
                dx = 0;
            } else if (preventVerticalAction(selected, dy)) {
                dy = 0;
            }
        }
        callback.onDraw(c, parent, selected, recoverAnimations, actionState, dx, dy, isRtl);
    }

    /**
     * Checks if we need to prevent a horizontal swipe action for a view holder -- this is used when
     * we have preventZeroSizeViewSwipes set to true and we need to check if we're preventing a
     * zero-size swipe
     *
     * @param holder       the view holder
     * @param translationX the new translation x of the holder
     * @return true if we need to prevent the action, false if not
     */
    private boolean preventHorizontalAction(final SwipeOpenViewHolder holder,
                                            final float translationX) {
        if (translationX > 0f && ((!isRtl && holder.getStartHiddenViewSize() == 0f) ^ (isRtl
                && holder.getEndHiddenViewSize() == 0f))) {
            return true;
        } else if (translationX < 0f && ((!isRtl && holder.getEndHiddenViewSize() == 0f) ^ (isRtl
                && holder.getStartHiddenViewSize() == 0f))) {
            return true;
        }
        return false;
    }

    private boolean preventVerticalAction(final SwipeOpenViewHolder holder, final float dy) {
        if (dy > 0f && holder.getStartHiddenViewSize() == 0f) {
            return true;
        } else if (dy < 0f && holder.getEndHiddenViewSize() == 0f) {
            return true;
        }
        return false;
    }

    /**
     * Starts dragging or swiping the given View. Call with null if you want to clear it.
     *
     * @param selected    The ViewHolder to swipe. Can be null if you want to cancel the
     *                    current action
     * @param actionState The type of action
     */
    private void select(SwipeOpenViewHolder selected, int actionState) {
        if (selected == this.selected && actionState == this.actionState) {
            return;
        }
        final int prevActionState = this.actionState;
        // prevent duplicate animations
        endRecoverAnimation(selected);
        this.actionState = actionState;
        int actionStateMask = (1 << (DIRECTION_FLAG_COUNT + DIRECTION_FLAG_COUNT * actionState)) - 1;
        boolean preventLayout = false;

        // close the previously selected view holder if we're swiping a new one and the flag is true
        if (closeOnAction && selected != null && prevSelected != null && selected != prevSelected) {
            closeOpenHolder(prevSelected);
            prevSelected = null;
            preventLayout = true;
        }

        // if we've got any opened positions, and closeOnAction is true, close them
        // NOTE: only real way for this to happen is to have a view opened during configuration change
        // that then has its' state saved
        if (closeOnAction && openedPositions.size() > 0) {
            for (int i = 0; i < openedPositions.size(); i++) {
                RecyclerView.ViewHolder holder =
                        recyclerView.findViewHolderForAdapterPosition(openedPositions.keyAt(i));
                // if our selected isn't the opened position, close it
                if (holder instanceof SwipeOpenViewHolder && (selected == null
                        || holder.getAdapterPosition() != selected.getViewHolder().getAdapterPosition())) {
                    closeOpenHolder((SwipeOpenViewHolder) holder);
                }
                openedPositions.removeAt(i);
            }
        }

        if (this.selected != null) {
            prevSelected = this.selected;
            // we've changed selection, we need to animate it back
            if (prevSelected.getViewHolder().itemView.getParent() != null) {
                final int swipeDir = checkPreviousSwipeDirection(prevSelected.getViewHolder());
                releaseVelocityTracker();
                // find where we should animate to
                final float targetTranslateX, targetTranslateY;
                getSelectedDxDy(tmpPosition);

                final float currentTranslateX = tmpPosition[0];
                final float currentTranslateY = tmpPosition[1];
                // only need to check if we need a recover animation for non-zero translation views
                if (prevSelected.getSwipeView().getTranslationX() != 0
                        || prevSelected.getSwipeView().getTranslationY() != 0) {
                    final float absTranslateX = Math.abs(currentTranslateX);
                    final float absTranslateY = Math.abs(currentTranslateY);
                    final SavedOpenState state;
                    switch (swipeDir) {
                        case LEFT:
                        case START:
                            targetTranslateY = 0;
                            // check if we need to close or go to the open position
                            if (absTranslateX > prevSelected.getEndHiddenViewSize() / 2) {
                                targetTranslateX = prevSelected.getEndHiddenViewSize() * Math.signum(dX);
                                state = SavedOpenState.END_OPEN;
                            } else {
                                targetTranslateX = 0;
                                state = null;
                            }
                            break;
                        case RIGHT:
                        case END:
                            targetTranslateY = 0;
                            if (absTranslateX > prevSelected.getStartHiddenViewSize() / 2) {
                                targetTranslateX = prevSelected.getStartHiddenViewSize() * Math.signum(dX);
                                state = SavedOpenState.START_OPEN;
                            } else {
                                targetTranslateX = 0;
                                state = null;
                            }
                            break;
                        case UP:
                            targetTranslateX = 0;
                            if (absTranslateY > prevSelected.getEndHiddenViewSize() / 2) {
                                targetTranslateY = prevSelected.getEndHiddenViewSize() * Math.signum(dY);
                                state = SavedOpenState.END_OPEN;
                            } else {
                                targetTranslateY = 0;
                                state = null;
                            }
                            break;
                        case DOWN:
                            targetTranslateX = 0;
                            if (absTranslateY > prevSelected.getStartHiddenViewSize() / 2) {
                                targetTranslateY = prevSelected.getStartHiddenViewSize() * Math.signum(dY);
                                state = SavedOpenState.START_OPEN;
                            } else {
                                targetTranslateY = 0;
                                state = null;
                            }
                            break;
                        default:
                            state = null;
                            targetTranslateX = 0;
                            targetTranslateY = 0;
                    }
                    // if state == null, we're closing it
                    if (state == null) {
                        openedPositions.remove(prevSelected.getViewHolder().getAdapterPosition());
                    } else {
                        openedPositions.put(prevSelected.getViewHolder().getAdapterPosition(), state);
                    }

                    final RecoverAnimation rv =
                            new RecoverAnimation(prevSelected, prevActionState, currentTranslateX,
                                    currentTranslateY, targetTranslateX, targetTranslateY);
                    final long duration = callback.getAnimationDuration(recyclerView, ANIMATION_TYPE_SWIPE,
                            targetTranslateX - currentTranslateX, targetTranslateY - currentTranslateY);
                    rv.setDuration(duration);
                    recoverAnimations.add(rv);
                    rv.start();
                    preventLayout = true;
                } else {
                    // if both translations are 0, it's closed
                    openedPositions.remove(prevSelected.getViewHolder().getAdapterPosition());
                }
            } else {
                callback.clearView(recyclerView, prevSelected);
            }
            this.selected = null;
        }
        if (selected != null) {
            selectedFlags =
                    (callback.getAbsMovementFlags(recyclerView, selected.getViewHolder()) & actionStateMask)
                            >> (this.actionState * DIRECTION_FLAG_COUNT);
            selectedStartX =
                    selected.getViewHolder().itemView.getLeft() + selected.getSwipeView().getTranslationX();
            selectedStartY =
                    selected.getViewHolder().itemView.getTop() + selected.getSwipeView().getTranslationY();
            this.selected = selected;
        }
        final ViewParent rvParent = recyclerView.getParent();
        if (rvParent != null) {
            rvParent.requestDisallowInterceptTouchEvent(this.selected != null);
        }
        if (!preventLayout) {
            recyclerView.getLayoutManager().requestSimpleAnimationsInNextLayout();
        }
        callback.onSelectedChanged(this.selected, this.actionState);
        recyclerView.invalidate();
    }

    @Override
    public void onChildViewAttachedToWindow(View view) {
        final RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(view);
        if (holder == null || !(holder instanceof SwipeOpenViewHolder)) {
            return;
        }
        // check if the view we are about to attach had previously saved open state,
        // and then open it based off that
        if (openedPositions.get(holder.getAdapterPosition(), null) != null) {
            final SwipeOpenViewHolder swipeHolder = (SwipeOpenViewHolder) holder;
            final SavedOpenState state = openedPositions.get(holder.getAdapterPosition());

            if (recyclerView.getLayoutManager().canScrollVertically()) {
                int rtlFlipStart = isRtl ? -1 : 1;
                int rtlFlipEnd = isRtl ? 1 : -1;

                // if we're in an opened state and both view sizes are 0, then we're attempting
                // to restore the opened position before the view has measured, so we need to measure it
                if (swipeHolder.getStartHiddenViewSize() == 0 && swipeHolder.getEndHiddenViewSize() == 0) {
                    final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    swipeHolder.getViewHolder().itemView.measure(widthSpec, heightSpec);
                }

                swipeHolder.getSwipeView().setTranslationX(
                        state == SavedOpenState.START_OPEN ? swipeHolder.getStartHiddenViewSize() * rtlFlipStart
                                : swipeHolder.getEndHiddenViewSize() * rtlFlipEnd);
            } else {
                swipeHolder.getSwipeView().setTranslationY(
                        state == SavedOpenState.START_OPEN ? swipeHolder.getStartHiddenViewSize()
                                : swipeHolder.getEndHiddenViewSize() * -1);
            }
        }
    }

    /**
     * When a View is detached from the RecyclerView it is either because the item has been deleted,
     * or the View is being detached/recycled because it is no longer visible (e.g. RecyclerView has
     * been scrolled)
     *
     * @param view the view being detached
     */
    @Override
    public void onChildViewDetachedFromWindow(View view) {
        final RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(view);
        if (holder == null || !(holder instanceof SwipeOpenViewHolder)) {
            return;
        }
        final SwipeOpenViewHolder swipeHolder = (SwipeOpenViewHolder) holder;

        if (prevSelected == swipeHolder) {
            prevSelected = null;
        }
        if (selected != null && swipeHolder == selected) {
            select(null, ACTION_STATE_IDLE);
        } else {
            callback.clearView(recyclerView, swipeHolder);
            endRecoverAnimation(swipeHolder);
        }
    }

    private void endRecoverAnimation(SwipeOpenViewHolder viewHolder) {
        final int recoverAnimSize = recoverAnimations.size();
        for (int i = recoverAnimSize - 1; i >= 0; i--) {
            final RecoverAnimation anim = recoverAnimations.get(i);
            if (anim.viewHolder == viewHolder) {
                if (!anim.ended) {
                    anim.cancel();
                }
                recoverAnimations.remove(i);
            }
        }
    }

    /**
     * Opens the position of the START hidden view for a given position
     *
     * @param position the position
     */
    public void openPositionStart(final int position) {
        openPosition(position, SavedOpenState.START_OPEN);
    }

    /**
     * Opens the position of the END hidden view for a given position
     *
     * @param position the position
     */
    public void openPositionEnd(final int position) {
        openPosition(position, SavedOpenState.END_OPEN);
    }

    private void openPosition(final int position, final SavedOpenState direction) {
        if (recyclerView == null) {
            return;
        }
        // attempt to close any open positions
        if (closeOnAction) {
            closeAllOpenPositions();
        }

        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
        if (holder instanceof SwipeOpenViewHolder) {
            // check that the view holder is attached to a parent
            if (((SwipeOpenViewHolder) holder).getViewHolder().itemView.getParent() != null) {
                // end any current animations for the view holder
                endRecoverAnimation((SwipeOpenViewHolder) holder);
                openHolder((SwipeOpenViewHolder) holder, direction);
                recyclerView.invalidate();
            }
        }
        // add open position to our saved positions
        openedPositions.put(position, direction);
    }

    /**
     * Closes the given SwipeOpenViewHolder at the given position if there is one.
     * If the position is not currently attached to the RecyclerView (e.g. off-screen), then
     * the opened position will just be removed and the holder will appear in a closed position
     * when it is next created/bound.
     *
     * @param position the position to close
     */
    public void closeOpenPosition(final int position) {
        if (recyclerView == null) {
            return;
        }
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
        if (holder instanceof SwipeOpenViewHolder) {
            // check that the view holder is attached to a parent
            if (((SwipeOpenViewHolder) holder).getViewHolder().itemView.getParent() != null) {
                // end any current animations for the view holder
                endRecoverAnimation((SwipeOpenViewHolder) holder);
                closeOpenHolder((SwipeOpenViewHolder) holder);
                recyclerView.invalidate();
            }
        }
        // remove the position if we have not already
        openedPositions.remove(position);
    }

    /**
     * Closes all currently opened SwipeOpenViewHolders for the currently attached RecyclerView
     */
    public void closeAllOpenPositions() {
        if (recyclerView == null) {
            return;
        }
        for (int i = openedPositions.size() - 1; i >= 0; i--) {
            closeOpenPosition(openedPositions.keyAt(i));
        }
        // remove all positions in case one was not removed
        openedPositions.clear();
    }

    /**
     * Closes a SwipeOpenHolder that has been previously opened
     *
     * @param holder the holder
     */
    private void closeOpenHolder(SwipeOpenViewHolder holder) {
        final View swipeView = holder.getSwipeView();
        final float translationX = swipeView.getTranslationX();
        final float translationY = swipeView.getTranslationY();
        final RecoverAnimation rv = new RecoverAnimation(holder, 0, translationX, translationY, 0, 0);
        final long duration =
                callback.getAnimationDuration(recyclerView, ANIMATION_TYPE_SWIPE, translationX,
                        translationY);
        rv.setDuration(duration);
        recoverAnimations.add(rv);
        rv.start();
        // remove it from our open positions if we've got it
        openedPositions.remove(holder.getViewHolder().getAdapterPosition());
    }

    /**
     * Opens a SwipeOpenHolder in a given direction
     *
     * @param holder    the holder
     * @param direction the direction
     * @return true if the view was opened, false if not
     */
    private void openHolder(SwipeOpenViewHolder holder, SavedOpenState direction) {
        final View swipeView = holder.getSwipeView();
        final float translationX = swipeView.getTranslationX();
        final float translationY = swipeView.getTranslationY();
        final float openSize = direction == SavedOpenState.START_OPEN ? holder.getStartHiddenViewSize()
                : holder.getEndHiddenViewSize();

        final RecoverAnimation rv;
        if (recyclerView.getLayoutManager().canScrollVertically()) {
            int rtlFlipStart = isRtl ? -1 : 1;
            int rtlFlipEnd = isRtl ? 1 : -1;

            float targetDx =
                    direction == SavedOpenState.START_OPEN ? openSize * rtlFlipStart : openSize * rtlFlipEnd;
            rv = new RecoverAnimation(holder, 0, translationX, translationY, targetDx, 0);
        } else {
            float targetDx = direction == SavedOpenState.START_OPEN ? openSize * -1 : openSize;
            rv = new RecoverAnimation(holder, 0, translationX, translationY, 0, targetDx);
        }
        final long duration =
                callback.getAnimationDuration(recyclerView, ANIMATION_TYPE_SWIPE, translationX,
                        translationY);
        rv.setDuration(duration);
        recoverAnimations.add(rv);
        rv.start();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        outRect.setEmpty();
    }

    private void obtainVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
        }
        velocityTracker = VelocityTracker.obtain();
    }

    private void releaseVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private RecyclerView.ViewHolder findSwipedView(MotionEvent motionEvent) {
        final RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
        if (activePointerId == ACTIVE_POINTER_ID_NONE) {
            return null;
        }
        final int pointerIndex = motionEvent.findPointerIndex(activePointerId);
        final float dx = motionEvent.getX(pointerIndex) - initialTouchX;
        final float dy = motionEvent.getY(pointerIndex) - initialTouchY;
        final float absDx = Math.abs(dx);
        final float absDy = Math.abs(dy);

        if (absDx < slop && absDy < slop) {
            return null;
        }
        if (absDx > absDy && lm.canScrollHorizontally()) {
            return null;
        } else if (absDy > absDx && lm.canScrollVertically()) {
            return null;
        }
        View child = findChildView(motionEvent);
        if (child == null) {
            return null;
        }
        RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(child);
        if (holder instanceof SwipeOpenViewHolder) {
            return holder;
        }
        return null;
    }

    /**
     * Checks whether we should select a View for swiping.
     */
    private boolean checkSelectForSwipe(int action, MotionEvent motionEvent, int pointerIndex) {
        if (selected != null || action != MotionEvent.ACTION_MOVE) {
            return false;
        }
        if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING) {
            return false;
        }
        final RecyclerView.ViewHolder vh = findSwipedView(motionEvent);
        if (vh == null) {
            return false;
        }

        final int movementFlags = callback.getAbsMovementFlags(recyclerView, vh);

        final int swipeFlags =
                (movementFlags & ACTION_MODE_SWIPE_MASK) >> (DIRECTION_FLAG_COUNT * ACTION_STATE_SWIPE);

        if (swipeFlags == 0) {
            return false;
        }

        // dX and dY are only set in allowed directions. We use custom x/y here instead of
        // updateDxDy to avoid swiping if user moves more in the other direction
        final float x = motionEvent.getX(pointerIndex);
        final float y = motionEvent.getY(pointerIndex);

        // Calculate the distance moved
        final float dx = x - initialTouchX;
        final float dy = y - initialTouchY;
        // swipe target is chose w/o applying flags so it does not really check if swiping in that
        // direction is allowed. This why here, we use dX dY to check slope value again.
        final float absDx = Math.abs(dx);
        final float absDy = Math.abs(dy);

        if (absDx < slop && absDy < slop) {
            return false;
        }
        if (absDx > absDy) {
            if (dx < 0 && (swipeFlags & LEFT) == 0) {
                return false;
            }
            if (dx > 0 && (swipeFlags & RIGHT) == 0) {
                return false;
            }
        } else {
            if (dy < 0 && (swipeFlags & UP) == 0) {
                return false;
            }
            if (dy > 0 && (swipeFlags & DOWN) == 0) {
                return false;
            }
        }
        dX = dY = 0f;
        activePointerId = motionEvent.getPointerId(0);
        select((SwipeOpenViewHolder) vh, ACTION_STATE_SWIPE);
        return true;
    }

    private View findChildView(MotionEvent event) {
        // first check elevated views, if none, then call RV
        final float x = event.getX();
        final float y = event.getY();
        if (selected != null) {
            final View selectedView = selected.getViewHolder().itemView;
            if (hitTest(selectedView, x, y, selectedStartX + dX, selectedStartY + dY)) {
                return selectedView;
            }
        }
        for (int i = recoverAnimations.size() - 1; i >= 0; i--) {
            final RecoverAnimation anim = recoverAnimations.get(i);
            final View view = anim.viewHolder.getViewHolder().itemView;
            if (hitTest(view, x, y, anim.x, anim.y)) {
                return view;
            }
        }
        return recyclerView.findChildViewUnder(x, y);
    }

    /**
     * Starts swiping the provided ViewHolder.
     * See {@link android.support.v7.widget.helper.ItemTouchHelper#startSwipe(RecyclerView.ViewHolder)}
     *
     * @param viewHolder The ViewHolder to start swiping. It must be a direct child of
     *                   RecyclerView.
     */
    public void startSwipe(SwipeOpenViewHolder viewHolder) {
        if (viewHolder.getViewHolder().itemView.getParent() != recyclerView) {
            Log.e(TAG, "Start swipe has been called with a view holder which is not a child of "
                    + "the RecyclerView controlled by this SwipeOpenItemTouchHelper.");
            return;
        }
        obtainVelocityTracker();
        dX = dY = 0f;
        select(viewHolder, ACTION_STATE_SWIPE);
    }

    private RecoverAnimation findAnimation(MotionEvent event) {
        if (recoverAnimations.isEmpty()) {
            return null;
        }
        View target = findChildView(event);
        for (int i = recoverAnimations.size() - 1; i >= 0; i--) {
            final RecoverAnimation anim = recoverAnimations.get(i);
            if (anim.viewHolder.getViewHolder().itemView == target) {
                return anim;
            }
        }
        return null;
    }

    private void updateDxDy(MotionEvent ev, int directionFlags, int pointerIndex) {
        final float x = ev.getX(pointerIndex);
        final float y = ev.getY(pointerIndex);
        // Calculate the distance moved
        dX = x - initialTouchX;
        dY = y - initialTouchY;
        if ((directionFlags & LEFT) == 0) {
            dX = Math.max(0, dX);
        }
        if ((directionFlags & RIGHT) == 0) {
            dX = Math.min(0, dX);
        }
        if ((directionFlags & UP) == 0) {
            dY = Math.max(0, dY);
        }
        if ((directionFlags & DOWN) == 0) {
            dY = Math.min(0, dY);
        }
    }

    private int checkPreviousSwipeDirection(RecyclerView.ViewHolder viewHolder) {
        final int originalMovementFlags = callback.getMovementFlags(recyclerView, viewHolder);
        final int absoluteMovementFlags = callback.convertToAbsoluteDirection(originalMovementFlags,
                ViewCompat.getLayoutDirection(recyclerView));
        final int flags = (absoluteMovementFlags & ACTION_MODE_SWIPE_MASK) >> (ACTION_STATE_SWIPE
                * DIRECTION_FLAG_COUNT);
        if (flags == 0) {
            return 0;
        }
        final int originalFlags = (originalMovementFlags & ACTION_MODE_SWIPE_MASK) >> (
                ACTION_STATE_SWIPE
                        * DIRECTION_FLAG_COUNT);
        int swipeDir;
        if (Math.abs(dX) > Math.abs(dY)) {
            if ((swipeDir = checkHorizontalSwipe(flags)) > 0) {
                // if swipe dir is not in original flags, it should be the relative direction
                if ((originalFlags & swipeDir) == 0) {
                    // convert to relative
                    return Callback.convertToRelativeDirection(swipeDir,
                            ViewCompat.getLayoutDirection(recyclerView));
                }
                return swipeDir;
            }
            if ((swipeDir = checkVerticalSwipe(flags)) > 0) {
                return swipeDir;
            }
        } else {
            if ((swipeDir = checkVerticalSwipe(flags)) > 0) {
                return swipeDir;
            }
            if ((swipeDir = checkHorizontalSwipe(flags)) > 0) {
                // if swipe dir is not in original flags, it should be the relative direction
                if ((originalFlags & swipeDir) == 0) {
                    // convert to relative
                    return Callback.convertToRelativeDirection(swipeDir,
                            ViewCompat.getLayoutDirection(recyclerView));
                }
                return swipeDir;
            }
        }
        return 0;
    }

    private int checkHorizontalSwipe(int flags) {
        if ((flags & (LEFT | RIGHT)) != 0) {
            return dX > 0 ? RIGHT : LEFT;
        }
        return 0;
    }

    private int checkVerticalSwipe(int flags) {
        if ((flags & (UP | DOWN)) != 0) {
            return dY > 0 ? DOWN : UP;
        }
        return 0;
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putSparseParcelableArray(OPENED_STATES, openedPositions);
    }

    public void restoreInstanceState(Bundle savedInstanceState) {
        openedPositions = savedInstanceState.getSparseParcelableArray(OPENED_STATES);
        if (openedPositions == null) {
            openedPositions = new SparseArray<>();
        }
    }

    /**
     * Base Callback class that extends off of {@link ItemTouchHelper.Callback}
     */
    @SuppressWarnings("UnusedParameters")
    public abstract static class Callback
            extends ItemTouchHelper.Callback {

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            // do not use
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            // do not use
        }

        /**
         * Convenience method to create movement flags.
         * <p>
         * For instance, if you want to let your items be drag & dropped vertically and swiped
         * left to be dismissed, you can call this method with:
         * <code>makeMovementFlags(UP | DOWN, LEFT);</code>
         *
         * @param swipeFlags The directions in which the item can be swiped.
         * @return Returns an integer composed of the given drag and swipe flags.
         */
        public static int makeMovementFlags(int swipeFlags) {
            return makeFlag(ACTION_STATE_IDLE, swipeFlags) | makeFlag(ACTION_STATE_SWIPE, swipeFlags);
        }

        final int getAbsMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            final int flags = getMovementFlags(recyclerView, viewHolder);
            return convertToAbsoluteDirection(flags, ViewCompat.getLayoutDirection(recyclerView));
        }

        /**
         * Called when the ViewHolder is changed.
         * <p/>
         * If you override this method, you should call super.
         *
         * @param viewHolder  The new ViewHolder that is being swiped. Might be null if
         *                    it is cleared.
         * @param actionState One of {@link SwipeOpenItemTouchHelper#ACTION_STATE_IDLE},
         *                    {@link SwipeOpenItemTouchHelper#ACTION_STATE_SWIPE}
         * @see #clearView(RecyclerView, SwipeOpenViewHolder)
         */
        public void onSelectedChanged(SwipeOpenViewHolder viewHolder, int actionState) {
            if (viewHolder != null) {
                getDefaultUIUtil().onSelected(viewHolder.getSwipeView());
            }
        }

        private void onDraw(Canvas c, RecyclerView parent, SwipeOpenViewHolder selected,
                            List<RecoverAnimation> recoverAnimationList, int actionState, float dX, float dY,
                            boolean isRtl) {
            final int recoverAnimSize = recoverAnimationList.size();
            for (int i = 0; i < recoverAnimSize; i++) {
                final RecoverAnimation anim = recoverAnimationList.get(i);
                anim.update();
                final int count = c.save();
                onChildDraw(c, parent, anim.viewHolder, anim.x, anim.y, false);
                c.restoreToCount(count);
            }
            if (selected != null) {
                final int count = c.save();
                notifySwipeDirections(selected, isRtl, dX, dY);
                onChildDraw(c, parent, selected, dX, dY, true);
                c.restoreToCount(count);
            }
        }

        /**
         * Notifies the SwipeOpenHolder when one of its hidden views has become visible.
         *
         * @param holder the holder
         * @param isRtl  if the layout is RTL or not
         * @param dX     the new dX of the swiped view
         * @param dY     the new dY of the swiped view
         */
        private void notifySwipeDirections(SwipeOpenViewHolder holder, boolean isRtl, float dX,
                                           float dY) {
            // check if we are about to start a swipe to open start or open end positions
            View swipeView = holder.getSwipeView();
            // 0 or negative translationX, heading to positive translationX
            if (swipeView.getTranslationX() <= 0 && dX > 0) {
                if (isRtl) {
                    holder.notifyEndOpen();
                } else {
                    holder.notifyStartOpen();
                }
                // 0 or positive translationX, heading to negative translationX
            } else if (swipeView.getTranslationX() >= 0 && dX < 0) {
                if (isRtl) {
                    holder.notifyStartOpen();
                } else {
                    holder.notifyEndOpen();
                }
                // 0 or positive translationY, heading to negative translationY
            } else if (swipeView.getTranslationY() >= 0 && dY < 0) {
                holder.notifyEndOpen();
            } else if (swipeView.getTranslationY() <= 0 && dY > 0) {
                holder.notifyStartOpen();
            }
        }

        private void onDrawOver(Canvas c, RecyclerView parent, SwipeOpenViewHolder selected,
                                List<RecoverAnimation> recoverAnimationList, int actionState, float dX, float dY) {
            final int recoverAnimSize = recoverAnimationList.size();
            boolean hasRunningAnimation = false;
            for (int i = recoverAnimSize - 1; i >= 0; i--) {
                final RecoverAnimation anim = recoverAnimationList.get(i);
                if (anim.ended) {
                    recoverAnimationList.remove(i);
                } else {
                    hasRunningAnimation = true;
                }
            }
            if (hasRunningAnimation) {
                parent.invalidate();
            }
        }

        public void clearView(RecyclerView recyclerView, SwipeOpenViewHolder viewHolder) {
            getDefaultUIUtil().clearView(viewHolder.getSwipeView());
        }

        public void onChildDraw(Canvas c, RecyclerView recyclerView, SwipeOpenViewHolder viewHolder,
                                float dX, float dY, boolean isCurrentlyActive) {
            // handle the draw
            getDefaultUIUtil().onDraw(c, recyclerView, viewHolder.getSwipeView(), dX, dY,
                    ACTION_STATE_SWIPE, isCurrentlyActive);
        }

        public long getAnimationDuration(RecyclerView recyclerView, int animationType, float animateDx,
                                         float animateDy) {
            final RecyclerView.ItemAnimator itemAnimator = recyclerView.getItemAnimator();
            if (itemAnimator == null) {
                return DEFAULT_SWIPE_ANIMATION_DURATION;
            } else {
                return itemAnimator.getMoveDuration();
            }
        }
    }

    /**
     * Simple callback class that defines the swipe directions allowed and delegates everything else
     * to the base class
     */
    @SuppressWarnings("UnusedParameters")
    public static class SimpleCallback extends Callback {

        private int mDefaultSwipeDirs;

        public SimpleCallback(int swipeDirs) {
            mDefaultSwipeDirs = swipeDirs;
        }

        public void setDefaultSwipeDirs(int defaultSwipeDirs) {
            mDefaultSwipeDirs = defaultSwipeDirs;
        }

        public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return mDefaultSwipeDirs;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(getSwipeDirs(recyclerView, viewHolder));
        }
    }

    private static class RecoverAnimation implements Animator.AnimatorListener {

        final float startDx;

        final float startDy;

        final float targetX;

        final float targetY;

        final SwipeOpenViewHolder viewHolder;

        final int actionState;

        private final ValueAnimator valueAnimator;

        float x;

        float y;

        private boolean ended = false;

        private float fraction;

        public RecoverAnimation(SwipeOpenViewHolder viewHolder, int actionState, float startDx,
                                float startDy, float targetX, float targetY) {
            this.actionState = actionState;
            this.viewHolder = viewHolder;
            this.startDx = startDx;
            this.startDy = startDy;
            this.targetX = targetX;
            this.targetY = targetY;
            valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setFraction(animation.getAnimatedFraction());
                }
            });
            valueAnimator.setTarget(viewHolder.getViewHolder().itemView);
            valueAnimator.addListener(this);
            setFraction(0f);
        }

        public void setDuration(long duration) {
            valueAnimator.setDuration(duration);
        }

        public void start() {
            viewHolder.getViewHolder().setIsRecyclable(false);
            valueAnimator.start();
        }

        public void cancel() {
            valueAnimator.cancel();
        }

        public void setFraction(float fraction) {
            this.fraction = fraction;
        }

        /**
         * We run updates on onDraw method but use the fraction from animator callback.
         * This way, we can sync translate x/y values w/ the animators to avoid one-off frames.
         */
        public void update() {
            if (startDx == targetX) {
                x = viewHolder.getSwipeView().getTranslationX();
            } else {
                x = startDx + fraction * (targetX - startDx);
            }
            if (startDy == targetY) {
                y = viewHolder.getSwipeView().getTranslationY();
            } else {
                y = startDy + fraction * (targetY - startDy);
            }
        }

        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            if (!ended) {
                viewHolder.getViewHolder().setIsRecyclable(true);
            }
            ended = true;
        }

        @Override
        public void onAnimationCancel(Animator animator) {
            setFraction(1f); //make sure we recover the view's state.
        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }

    /**
     * Enum for saving the opened state of the view holders
     */
    private enum SavedOpenState implements Parcelable {
        START_OPEN, END_OPEN;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(ordinal());
        }

        public static final Creator<SavedOpenState> CREATOR =
                new Creator<SavedOpenState>() {
                    @Override
                    public SavedOpenState createFromParcel(Parcel source) {
                        return SavedOpenState.values()[source.readInt()];
                    }

                    @Override
                    public SavedOpenState[] newArray(int size) {
                        return new SavedOpenState[size];
                    }
                };

    }
}