package easyadapter.dc.com.easyadapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Kiran-pc on 23/06/2018.
 */
public class SplashActivity extends AppCompatActivity {

  public static final String TAG = "EasyAdapter";

  @SuppressLint("StaticFieldLeak") @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);

    new AsyncTask<Void, Void, File>() {
      @Override protected void onPreExecute() {
        super.onPreExecute();
      }

      @Override protected File doInBackground(Void... voids) {
        File obbDir = getObbDir();
        File fileToExtract = new File(obbDir, getExpansionFileName());
        if (!fileToExtract.exists()) {
          Log.e(TAG, "Obb File Not found");
          //Download File code here
        }
        File extractTo = getExtractedDirectory();
        try {
          unzip(fileToExtract, extractTo);
        } catch (IOException e) {
          e.printStackTrace();
        }
        return extractTo;
      }

      @Override protected void onPostExecute(File extractedTo) {
        super.onPostExecute(extractedTo);
        if (extractedTo.exists() && extractedTo.length() > 0) {
          startActivity(new Intent(SplashActivity.this, MainActivity.class));
        }
      }
    }.execute();
  }

  public static void unzip(File zipFile, File targetDirectory) throws IOException {
    ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
    try {
      ZipEntry ze;
      int count;
      byte[] buffer = new byte[8192];
      while ((ze = zis.getNextEntry()) != null) {
        File file = new File(targetDirectory, ze.getName());
        File dir = ze.isDirectory() ? file : file.getParentFile();
        if (!dir.isDirectory() && !dir.mkdirs()) {
          throw new FileNotFoundException("Failed to ensure directory: " + dir.getAbsolutePath());
        }
        if (ze.isDirectory()) continue;
        FileOutputStream fout = new FileOutputStream(file);
        try {
          while ((count = zis.read(buffer)) != -1) fout.write(buffer, 0, count);
        } finally {
          fout.close();
        }
            /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
      }
    } finally {
      zis.close();
    }
  }

  public String getExpansionFileName() {
    return "main.1.easyadapter.dc.com.easyadapter.zip";
  }

  public File getExtractedDirectory() {
    File extractTo = new File(Environment.getExternalStorageDirectory(), "Extracted");
    if (!extractTo.exists()) extractTo.mkdir();
    return extractTo;
  }
}

