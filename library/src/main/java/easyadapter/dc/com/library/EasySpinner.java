package easyadapter.dc.com.library;

import android.content.Context;
import android.content.res.Resources;
import android.databinding.ViewDataBinding;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import java.util.ArrayList;

/**
 * Created by HB on 25/5/18.
 */
public class EasySpinner extends AppCompatEditText {

    public static final int POPUP_WIDTH_VIEW = -1;
    public static final int POPUP_TYPE_DROP_DOWN = 1;
    public static final int POPUP_TYPE_DIALOG = 2;
    private PopupWindow popupWindow;
    private RecyclerView recyclerView;
    private KeyListener keyListener;
    private OnTextChange onTextChange;
    private OnDropDownVisibilityListener onDropDownVisibilityListener;
    private int MAX_SIZE = 0;
    private Drawable background;
    private int animation;
    private int popupWidth = POPUP_WIDTH_VIEW;
    private int popupType = POPUP_TYPE_DROP_DOWN;

    private LinearLayout linearLayout;

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
        //Make Edit text as TextView First
        keyListener = getKeyListener();
        makeEditable(false);

        recyclerView = new RecyclerView(getContext());
        linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        final EditText editText = new EditText(getContext());
        editText.setHint(getHint());
        editText.setId(R.id.edtHint);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                onTextChange(editText);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        linearLayout.addView(editText);
        linearLayout.addView(recyclerView);

        setPopupBackground(ContextCompat.getDrawable(getContext(), android.R.drawable.dialog_holo_light_frame));
        setOnClickListener(onClickListener);
        setOnFocusChangeListener(onFocusChangeListener);
    }

    public void setPopupBackground(Drawable drawable) {
        background = drawable;
    }

    public void setPopupType(int popupType) {
        this.popupType = popupType;
    }

    public void setPopupWidth(int width) {
        popupWidth = width;
    }

    public void setAnimation(@StyleRes int animationStyle) {
        this.animation = animationStyle;
    }


    public void setOnDropDownVisibilityListener(OnDropDownVisibilityListener onDropDownVisibilityListener) {
        this.onDropDownVisibilityListener = onDropDownVisibilityListener;
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null)
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void stopAutoCompleteObserve() {
        removeTextChangedListener(textWatcher);
    }

    public void startAutoCompleteObserve() {
        addTextChangedListener(textWatcher);
    }


    public <M, B extends ViewDataBinding> void setAdapter(EasyAdapter<M, B> adapter) {
        setAdapter(new LinearLayoutManager(getContext()), adapter);
    }

    public <M, B extends ViewDataBinding> void setAdapter(RecyclerView.LayoutManager layoutManager, EasyAdapter<M, B> adapter) {
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        adapter.addOnDataUpdateListener(new EasyAdapter.OnDataUpdate<M>() {
            @Override
            public void onDataUpdate(ArrayList<M> data) {
                if (popupWindow != null) {
                    if (popupType == POPUP_TYPE_DROP_DOWN) {
                        popupWindow.update(EasySpinner.this, getPopupWidth(), getListHeight());
                    }
                }
            }
        });
    }

    private View.OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (!hasFocus) {
                hide();
            } else {
                show();
            }
        }
    };

    private View.OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!isShowing()) {
                show();
            } else {
                hide();
            }
        }
    };


    private boolean isShowing() {
        if (popupWindow != null) {
            return popupWindow.isShowing();
        }
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        MAX_SIZE = getScreenHeight() / 3;
    }

    private int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    private PopupWindow buildPopupWindow(int width, int height) {
        PopupWindow popupWindow = new PopupWindow(recyclerView, width, height);
        popupWindow.setAnimationStyle(animation);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        popupWindow.setBackgroundDrawable(background);
        popupWindow.setContentView(linearLayout);
        return popupWindow;
    }


    public void show() {
        if (getKeyListener() == null) {
            hideKeyboard(this);
        }

        //Testing purpose
        /*if (System.currentTimeMillis() % 2 == 0) {
            popupType = POPUP_TYPE_DROP_DOWN;
        } else {
            popupType = POPUP_TYPE_DIALOG;
        }*/

        popupWindow = buildPopupWindow(getPopupWidth(), getListHeight());

        EditText edtHint = popupWindow.getContentView().findViewById(R.id.edtHint);
        edtHint.setVisibility(onTextChange != null &&
                popupType == POPUP_TYPE_DIALOG ? View.VISIBLE : View.GONE);
        edtHint.setText("");

        if (popupType == POPUP_TYPE_DROP_DOWN) {
            //show as dropdown
            popupWindow.showAsDropDown(this, 0, 0);
            popupWindow.update(this, getPopupWidth(), getListHeight());
        } else {
            //Show as dialog
            popupWindow.showAtLocation(this, Gravity.CENTER, 0, 0);
            dimPopupWindow(popupWindow);
        }

        if (onDropDownVisibilityListener != null)
            onDropDownVisibilityListener.onDropDownVisibilityChange(true);
    }

    public static void dimPopupWindow(PopupWindow popupWindow) {
        View container;
        if (popupWindow.getBackground() == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                container = (View) popupWindow.getContentView().getParent();
            } else {
                container = popupWindow.getContentView();
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                container = (View) popupWindow.getContentView().getParent().getParent();
            } else {
                container = (View) popupWindow.getContentView().getParent();
            }
        }
        Context context = popupWindow.getContentView().getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);
    }

    public void hide() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
        if (onDropDownVisibilityListener != null)
            onDropDownVisibilityListener.onDropDownVisibilityChange(false);

        hideKeyboard(this);
        hideKeyboard(popupWindow.getContentView().findViewById(R.id.edtHint));
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void enableAutoCompleteMode(OnTextChange onTextChange) {
        this.onTextChange = onTextChange;
        makeEditable(true);
        stopAutoCompleteObserve();
        startAutoCompleteObserve();
    }


    private void makeEditable(boolean editable) {
        if (editable) {
            setCursorVisible(true);
            setKeyListener(keyListener);
        } else {
            setKeyListener(null);
            setCursorVisible(false);
            setInputType(0);
        }
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {


        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            onTextChange(EasySpinner.this);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };


    private int getListHeight() {
        recyclerView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

        int recyclerViewHeight = recyclerView.getMeasuredHeight();
        if (recyclerViewHeight > 0)
            recyclerViewHeight += background.getIntrinsicHeight() / 2;
        return Math.min(recyclerViewHeight, MAX_SIZE);
    }

    private int getPopupWidth() {
        return popupWidth == -1 ? getWidth() : popupWidth;
    }

  /*  private Dialog createDialog(View contentView) {
        Dialog dialog = new Dialog(getContext());
        dialog.setCancelable(true);

        dialog.getWindow().getAttributes().windowAnimations = animation;
        dialog.getWindow().setBackgroundDrawable(background);

        dialog.setContentView(contentView);
        return dialog;
    }*/

    private void onTextChange(EditText editText) {
        if (onTextChange != null) {
            onTextChange.onTextChange(EasySpinner.this, editText.getText().toString());
        }
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && isShowing()) {
            hide();
            return true;
        }
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        hide();
    }

}


