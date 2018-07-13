package easyadapter.dc.com.library;

import android.content.Context;
import android.graphics.Rect;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;

/**
 * Created by HB on 9/7/18.
 */
public class EasyAutoComplete extends android.support.v7.widget.AppCompatAutoCompleteTextView {

    private OnItemCallback onItemCallback;
    private boolean enableAutoComplete;
    private KeyListener keyListener;


    public interface OnItemCallback {
        public void onItemCallback(int position, View view);
    }


    public EasyAutoComplete(Context context) {
        super(context);
        init();
    }

    public EasyAutoComplete(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EasyAutoComplete(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    private void init() {
        setThreshold(0);
        keyListener = getKeyListener();
        enableAutoComplete(true);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getAdapter() != null) {
                    showDropDown();
                }
            }
        });
        setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onItemCallback.onItemCallback(position, EasyAutoComplete.this);
            }
        });
    }


    @Override
    public boolean enoughToFilter() {
        return true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction,
                                  Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused && getAdapter() != null) {
            performFiltering(getText(), 0);
            post(new Runnable() {
                @Override
                public void run() {
                    showDropDown();
                }
            });
        }
    }

    public void setItemSelectionCallback(OnItemCallback onItemCallback) {
        this.onItemCallback = onItemCallback;
    }


    public void enableAutoComplete(boolean enableAutoComplete) {
        this.enableAutoComplete = enableAutoComplete;
        makeEditable(enableAutoComplete);
    }

    private void makeEditable(boolean editable) {
        if (editable) {
            setCursorVisible(true);
            setKeyListener(keyListener);
            setSelectAllOnFocus(true);
        } else {
            setKeyListener(null);
            setCursorVisible(false);
            setInputType(0);
            setSelectAllOnFocus(false);
        }
    }

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        super.performFiltering(!enableAutoComplete ? "" : text, keyCode);
    }
}
