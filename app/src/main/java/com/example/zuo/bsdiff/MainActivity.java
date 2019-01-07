package com.example.zuo.bsdiff;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView version = (TextView) findViewById(R.id.tv_version);
        version.setText(BuildConfig.VERSION_NAME);
    }

    /**
     *
     * @param oldapk 当前运行的apk
     * @param patch 差分包
     * @param output 合成胡的新的apk
     */
    native void bspatch(String oldapk,String patch,String output);

    public void onUpdate(View view) {
        new MyAsyncTask().execute();
    }

    private class MyAsyncTask extends AsyncTask<Void,Void,File>{

        @Override
        protected File doInBackground(Void... voids) {
            //1、合成apk
            String old = getApplication().getApplicationInfo().sourceDir;

            bspatch(old,"/sdcard/patch","/sdcard/new.apk");
            return new File("/sdcard/new.apk");
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            //2、安装
            Intent i = new Intent(Intent.ACTION_VIEW);
            if(Build.VERSION.SDK_INT<Build.VERSION_CODES.N){
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setDataAndType(Uri.fromFile(file),
                        "application/vnd.android.package-archive");
            }else {
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                String packageName = getApplication().getPackageName();
                Uri contentUri = FileProvider.getUriForFile(MainActivity.this, packageName+ ".fileProvider", file);
                i.setDataAndType(contentUri,"application/vnd.android.package-archive");
            }
            startActivity(i);
        }
    }
}
