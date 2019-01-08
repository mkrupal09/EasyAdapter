package easyadapter.dc.com.easyadapter;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;

import com.devbrackets.android.exomedia.core.video.scale.ScaleType;
import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.devbrackets.android.exomedia.ui.widget.VideoView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ClippingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaPeriod;
import com.google.android.exoplayer2.source.MediaSource;

/**
 * Created by HB on 7/12/18.
 */
public class VideoActivity extends AppCompatActivity implements OnPreparedListener {

    VideoView videoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);  // or add <item name="android:windowTranslucentStatus">true</item> in the theme
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        setContentView(R.layout.activity_video);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams attrib = getWindow().getAttributes();
            attrib.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        videoView = (VideoView) findViewById(R.id.video_view);
        videoView.setOnPreparedListener(this);



        videoView.setScaleType(ScaleType.CENTER_CROP);

        //For now we just picked an arbitrary item to play
        videoView.setVideoURI(Uri.parse("http://pcock.com/public/upload/twilio_media/VIDEO_20181205_140947-20181205084109450389.mp4"));
    }

    @Override
    public void onPrepared() {
        videoView.start();
    }
}
