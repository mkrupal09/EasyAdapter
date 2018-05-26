package easyadapter.dc.com.library;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

/**
 * Created by HB on 25/5/18.
 */
public class EasySpinner extends AppCompatTextView {

    private PopupWindow popupWindow;
    private RecyclerView recyclerView;

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
        popupWindow = buildPopupWindow();
        post(new Runnable() {
            @Override
            public void run() {
                popupWindow.setWidth(getWidth());
            }
        });
        setOnClickListener(onClickListener);
    }

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
    }

    public void hide() {
        popupWindow.dismiss();
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }
}
