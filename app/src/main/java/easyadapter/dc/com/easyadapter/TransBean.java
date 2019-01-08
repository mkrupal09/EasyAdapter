package easyadapter.dc.com.easyadapter;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by HB on 3/1/19.
 */
public class TransBean implements Parcelable {
    protected TransBean(Parcel in) {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TransBean> CREATOR = new Creator<TransBean>() {
        @Override
        public TransBean createFromParcel(Parcel in) {
            return new TransBean(in);
        }

        @Override
        public TransBean[] newArray(int size) {
            return new TransBean[size];
        }
    };
}
