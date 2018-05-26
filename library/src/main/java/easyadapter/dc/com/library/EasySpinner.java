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
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

/**
 * Created by HB on 25/5/18.
 */
public class EasySpinner extends AppCompatEditText {

    private PopupWindow popupWindow;
    private RecyclerView recyclerView;
    private KeyListener keyListener;
    private OnTextChange onTextChange;
    private OnDropDownVisibilityListener onDropDownVisibilityListener;



    public  interface OnDropDownVisibilityListener {
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
        popupWindow = buildPopupWindow();
        post(new Runnable() {
            @Override
            public void run() {
                popupWindow.setWidth(getWidth());
            }
        });
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

    private PopupWindow buildPopupWindow() {
        PopupWindow popupWindow = new PopupWindow(getContext());
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), android.R.drawable.dialog_holo_light_frame));
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        recyclerView = new RecyclerView(getContext());
        popupWindow.setContentView(recyclerView);
        return popupWindow;
    }


    public <M, B extends ViewDataBinding> void setAdapter(EasyAdapter<M, B> adapter) {
        setAdapter(new LinearLayoutManager(getContext()), adapter);
    }

    public <M, B extends ViewDataBinding> void setAdapter(RecyclerView.LayoutManager layoutManager, EasyAdapter<M, B> adapter) {
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    public void show() {
        popupWindow.showAsDropDown(this, (int) getX(), 0);
        if(onDropDownVisibilityListener!=null)
            onDropDownVisibilityListener.onDropDownVisibilityChange(true);
    }

    public void hide() {
        popupWindow.dismiss();
        if(onDropDownVisibilityListener!=null)
            onDropDownVisibilityListener.onDropDownVisibilityChange(false);
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }


    public void enableAutoCompleteMode(OnTextChange onTextChange) {
        this.onTextChange = onTextChange;
        setKeyListener(keyListener);
        removeTextChangedListener(textWatcher);
        addTextChangedListener(textWatcher);

    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {


        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            onTextChange.onTextChange(EasySpinner.this, getText().toString());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };


    public void setOnDropDownVisibilityListener(OnDropDownVisibilityListener onDropDownVisibilityListener) {
        this.onDropDownVisibilityListener = onDropDownVisibilityListener;
    }
}
