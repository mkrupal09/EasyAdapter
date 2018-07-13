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
public class InstantSpinner extends android.support.v7.widget.AppCompatAutoCompleteTextView {

    private KeyListener keyListener;
    private EasyAutoComplete.OnItemCallback onItemCallback;

    public InstantSpinner(Context context) {
        super(context);
        init();
    }

    public InstantSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InstantSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    private void init() {
        setThreshold(0);
        keyListener = getKeyListener();
        makeEditable(false);
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
                onItemCallback.onItemCallback(position, InstantSpinner.this);
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

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        super.performFiltering("", keyCode);
    }

    public void setItemSelectionCallback(EasyAutoComplete.OnItemCallback onItemCallback) {
        this.onItemCallback = onItemCallback;
    }
}
