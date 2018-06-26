package easyadapter.dc.com.library;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupWindow;

import java.util.ArrayList;

/**
 * Created by HB on 25/5/18.
 */
public class EasySpinner extends AppCompatEditText {

    private PopupWindow popupWindow;
    private RecyclerView recyclerView;
    private KeyListener keyListener;
    private OnTextChange onTextChange;
    private OnDropDownVisibilityListener onDropDownVisibilityListener;
    private int listSize = 500;


    public interface OnDropDownVisibilityListener {
        public void onDropDownVisibilityChange(boolean show);
    }

    public interface OnTextChange {
        public void onTextChange(EasySpinner easySpinner, String text);
    }

    public EasySpinner(Context context) {
        super(context);
        init();
    }

    public EasySpinner(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EasySpinner(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        keyListener = getKeyListener();
        setKeyListener(null);
        setInputType(0);
        recyclerView = new RecyclerView(getContext());

        setOnClickListener(onClickListener);
        setOnFocusChangeListener(onFocusChangeListener);
    }


    private View.OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean b) {
            if (!b) {
                hide();
            } else {
                show();
            }
        }
    };
    private View.OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!popupWindow.isShowing()) {
                show();
            } else {
                hide();
            }
        }
    };

    private PopupWindow buildPopupWindow(int width, int height) {
        PopupWindow popupWindow = new PopupWindow(recyclerView, width, height);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), android.R.drawable.dialog_holo_light_frame));
        popupWindow.setContentView(recyclerView);
        return popupWindow;
    }


    public <M, B extends ViewDataBinding> void setAdapter(EasyAdapter<M, B> adapter) {
        setAdapter(new LinearLayoutManager(getContext()), adapter);
    }

    public <M, B extends ViewDataBinding> void setAdapter(RecyclerView.LayoutManager layoutManager, EasyAdapter<M, B> adapter) {
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        adapter.setOnDataUpdateListener(new EasyAdapter.OnDataUpdate<M>() {
            @Override
            public void onDataUpdate(ArrayList<M> data) {
                popupWindow.update(EasySpinner.this, getWidth(), getRecyclerViewHeight());
            }
        });
    }


    public void show() {
        if (getKeyListener() == null) {
            hideKeyboard();
        }
        popupWindow = buildPopupWindow(getWidth(), getRecyclerViewHeight());
        /*popupWindow.setAnimationStyle(R.style.Popwindow_Anim_Down);*/
        popupWindow.showAsDropDown(this, 0, 0);
        if (onDropDownVisibilityListener != null)
            onDropDownVisibilityListener.onDropDownVisibilityChange(true);

    }


   /* public void show() {
        if (getKeyListener() == null) {
            hideKeyboard();
        }
        popupWindow.showAsDropDown(this);
        if (onDropDownVisibilityListener != null)
            onDropDownVisibilityListener.onDropDownVisibilityChange(true);
    }*/

    public void hide() {
        popupWindow.dismiss();
        if (onDropDownVisibilityListener != null)
            onDropDownVisibilityListener.onDropDownVisibilityChange(false);
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }


    public void enableAutoCompleteMode(OnTextChange onTextChange) {
        this.onTextChange = onTextChange;
        setKeyListener(keyListener);
        stopAutoCompleteObserve();
        startAutoCompleteObserve();
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {


        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            onTextChange.onTextChange(EasySpinner.this, getText().toString());
            /*changeLocation();*/
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };


    /**
     * Callback when drop down is visible or hide
     *
     * @param onDropDownVisibilityListener
     */
    public void setOnDropDownVisibilityListener(OnDropDownVisibilityListener onDropDownVisibilityListener) {
        this.onDropDownVisibilityListener = onDropDownVisibilityListener;
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && popupWindow.isShowing()) {
            hide();
            return true;
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null)
            inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

    public void stopAutoCompleteObserve() {
        removeTextChangedListener(textWatcher);
    }

    public void startAutoCompleteObserve() {
        addTextChangedListener(textWatcher);
    }

    public int getRecyclerViewHeight() {
        return Math.min(recyclerView.getAdapter().getItemCount() * 200, listSize);
    }
}

