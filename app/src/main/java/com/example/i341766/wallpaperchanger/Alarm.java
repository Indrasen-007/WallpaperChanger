package com.example.i341766.wallpaperchanger;

import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;
import static com.example.i341766.wallpaperchanger.MainActivity.MY_PREFS_NAME;
import static com.example.i341766.wallpaperchanger.MainActivity.PATH;
import static com.example.i341766.wallpaperchanger.MainActivity.REQUIRED_HEIGHT;
import static com.example.i341766.wallpaperchanger.MainActivity.REQUIRED_WIDTH;
import static com.example.i341766.wallpaperchanger.MainActivity.calculateInSampleSize;
import static com.example.i341766.wallpaperchanger.MainActivity.decodeSampledBitmapFromResource;
import static com.example.i341766.wallpaperchanger.MainActivity.isExternalStorageAvailable;
import static com.example.i341766.wallpaperchanger.MainActivity.isExternalStorageReadOnly;
import static com.example.i341766.wallpaperchanger.MainActivity.isStoragePermissionGranted;

/**
 * Call the request to set the wallpaper
 */
public class Alarm extends BroadcastReceiver {

    /**
     * Read the image from the path provided,
     * and passes the image to ChangeWallpaperThreadClass, which in turn sets the wallpaper.
     *
     * @param context
     */
    private void mainMethod(Context context) {
        try {
            if (isExternalStorageAvailable() && isExternalStorageReadOnly() && isStoragePermissionGranted(context)) {

                SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                String path = prefs.getString(PATH, null);
                if (path != null) {

                    Bitmap bitmap = null;
                    try {
                        File directory = new File(path);
                        File[] files = directory.listFiles();

                        int randomNumber = new Random().nextInt(files.length);

                        File file = new File(directory, files[randomNumber].getName()); //or any other format supported
                        FileInputStream streamIn = new FileInputStream(file);

                        final BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(streamIn, null, options);
                        // Calculate inSampleSize
                        options.inSampleSize = calculateInSampleSize(options, REQUIRED_WIDTH, REQUIRED_HEIGHT);
                        streamIn.close();
                        streamIn = new FileInputStream(file);
                        // Decode bitmap with inSampleSize set
                        options.inJustDecodeBounds = false;

                        bitmap = BitmapFactory.decodeStream(streamIn, null, options);

                        streamIn.close();

                        if (bitmap == null)
                            bitmap = decodeSampledBitmapFromResource(context.getResources(), R.drawable.default_img, REQUIRED_WIDTH, REQUIRED_HEIGHT);

                    } catch (Exception e) {

                        bitmap = decodeSampledBitmapFromResource(context.getResources(), R.drawable.default_img, REQUIRED_WIDTH, REQUIRED_HEIGHT);

                        e.printStackTrace();
                    } finally {
                        try {
                            String filename = "AlarmTime.txt";
                            String filepath = "MyFileStorage";
                            File myExternalFile;
                            myExternalFile = new File(context.getExternalFilesDir(filepath), filename);

                            FileOutputStream fos = new FileOutputStream(myExternalFile, true);
                            Date currentTime = Calendar.getInstance().getTime();
                            fos.write(("\nWallpaper change Requested @ = " + currentTime.toString()).getBytes());
                            fos.close();
                            new ChangeWallpaperThreadClass(context).execute(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is called when the BroadcastReceiver is receiving an Intent
     * broadcast.  During this time you can use the other methods on
     * BroadcastReceiver to view/modify the current result values.  This method
     * is always called within the main thread of its process, unless you
     * explicitly asked for it to be scheduled on a different thread using
     * {@link Context#registerReceiver(BroadcastReceiver, * IntentFilter, String, Handler)}. When it runs on the main
     * thread you should
     * never perform long-running operations in it (there is a timeout of
     * 10 seconds that the system allows before considering the receiver to
     * be blocked and a candidate to be killed). You cannot launch a popup dialog
     * in your implementation of onReceive().
     * <p>
     * <p><b>If this BroadcastReceiver was launched through a &lt;receiver&gt; tag,
     * then the object is no longer alive after returning from this
     * function.</b> This means you should not perform any operations that
     * return a result to you asynchronously. If you need to perform any follow up
     * background work, schedule a {@link JobService} with
     * {@link JobScheduler}.
     * <p>The Intent filters used in {@link Context#registerReceiver}
     * and in application manifests are <em>not</em> guaranteed to be exclusive. They
     * are hints to the operating system about how to find suitable recipients. It is
     * possible for senders to force delivery to specific recipients, bypassing filter
     * resolution.  For this reason, {@link #onReceive(Context, Intent) onReceive()}
     * implementations should respond only to known actions, ignoring any unexpected
     * Intents that they may receive.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        //    if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
        mainMethod(context);
        // }
    }
}
