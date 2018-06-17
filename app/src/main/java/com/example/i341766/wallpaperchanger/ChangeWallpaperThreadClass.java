package com.example.i341766.wallpaperchanger;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * Thread Class to set wallpaper
 */
class ChangeWallpaperThreadClass extends AsyncTask<Bitmap, Void, String> {

    Context context = null;

    public ChangeWallpaperThreadClass() {
    }

    public ChangeWallpaperThreadClass(Context mContext) {
        context = mContext;
    }


    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param bitmaps The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected String doInBackground(Bitmap... bitmaps) {


        try {
            WallpaperManager myWallpaperManager
                    = WallpaperManager.getInstance(context);
            // to set image of bit map
            myWallpaperManager.setBitmap(bitmaps[0]);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Executed";
    }


    /**
     * <p>Runs on the UI thread after {@link #doInBackground}. The
     * specified result is the value returned by {@link #doInBackground}.</p>
     * <p>
     * <p>This method won't be invoked if the task was cancelled.</p>
     *
     * @param result The result of the operation computed by {@link #doInBackground}.
     * @see #onPreExecute
     * @see #doInBackground
     * @see #onCancelled(Object)
     */
    @Override
    protected void onPostExecute(String result) {
        try {
            Toast.makeText(context, "Wallpaper successfully changed", Toast.LENGTH_SHORT).show();
            String filename = "AlarmTime.txt";
            String filepath = "MyFileStorage";
            File myExternalFile;
            myExternalFile = new File(context.getExternalFilesDir(filepath), filename);

            FileOutputStream fos = new FileOutputStream(myExternalFile, true);
            Date currentTime = Calendar.getInstance().getTime();
            fos.write(("\nWallpaper changed @ = " + currentTime.toString()).getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
