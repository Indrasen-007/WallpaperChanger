package com.example.i341766.wallpaperchanger;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.util.Random;

import static android.text.TextUtils.isEmpty;
import static com.example.i341766.wallpaperchanger.AlarmThread.CANCEL_ALARM_AND_SET_NEW_ALARM;
import static com.example.i341766.wallpaperchanger.AlarmThread.SET_NEW_ALARM;

public class MainActivity extends AppCompatActivity
        implements  AdapterView.OnItemSelectedListener, View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    static final int REQUIRED_HEIGHT = 500;
    static final int REQUIRED_WIDTH = 500;
    static final String MY_PREFS_NAME = "MyFileStorage";
    static final String PATH = "path";
    private static final long SEC = 1000;
    private static final long MIN = 60 * SEC;
    private static final long HOUR = 60 * MIN;
    private static final long DAY = 24 * HOUR;
    private static final int REQUEST_CODE_TO_CHOOSE_DIRECTORY = 1;
    private static final int REQUEST_CODE_TO_NOT_CHOOSE_DIRECTORY = 0;
    private static long TRIGGER_AT_MILLIS = 1000 * 10;//AlarmManager.INTERVAL_HALF_HOUR;
    private static long INTERVAL_MILLIS = AlarmManager.INTERVAL_HOUR;
    long unit;
    private Bitmap bitmap;
    private TextView textView;
    private ImageView imagePreview;
    private Boolean isAlarmSet;
    private EditText time;
    private String path ;
    /**
     * Check if external Storage Reading is allowed.
     *
     * @return boolean
     */
    static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        return !Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState);
    }

    /**
     * Check if external storage is available
     *
     * @return boolean
     */
    static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(extStorageState);
    }

    /**
     * Calculate the optimal size for the image.
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Resize the image based on width and height provided.
     *
     * @param res
     * @param resId
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                  int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * check if the permission to read is granted
     *
     * @param context
     * @return
     */
    static boolean isStoragePermissionGranted(Context context) {
        return Build.VERSION.SDK_INT < 23 || (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button buttonSetWallpaper = findViewById(R.id.changeWallpaper);
        buttonSetWallpaper.setOnClickListener(this);

        Button nextImage = findViewById(R.id.nextImage);
        nextImage.setOnClickListener(this);

        Button chooseDir = findViewById(R.id.chooseDir);
        chooseDir.setOnClickListener(this);

        textView = findViewById(R.id.textView);

        imagePreview = findViewById(R.id.imageView);

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        path = prefs.getString(PATH, null);
        Boolean firstTime = prefs.getBoolean("firstTime", true);
        isAlarmSet = prefs.getBoolean("AlarmSet", false);
        if (firstTime) {
            isStoragePermissionGrantedIfNotRequestAccess(REQUEST_CODE_TO_NOT_CHOOSE_DIRECTORY);
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
            editor.putBoolean("firstTime", false);
            editor.putLong("INTERVAL_MILLIS", INTERVAL_MILLIS);
            editor.putLong("TRIGGER_AT_MILLIS",TRIGGER_AT_MILLIS);
            editor.apply();
        }

        if (isExternalStorageAvailable() && isExternalStorageReadOnly() && isStoragePermissionGranted()) {
            if (path == null) {
                selectDirectory();
            }
        }

        mainMethod(path);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * calls isStoragePermissionGranted() with context
     *
     * @return
     */
    private boolean isStoragePermissionGranted() {
        return isStoragePermissionGranted(this);
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_TO_CHOOSE_DIRECTORY || requestCode == REQUEST_CODE_TO_NOT_CHOOSE_DIRECTORY) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        if (requestCode == REQUEST_CODE_TO_CHOOSE_DIRECTORY) {
                            selectDirectory();
                        }
                        // Toast.makeText(this, "Permission granted to write", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }


    /**
     * <p>Callback method to be invoked when an item in this view has been
     * selected. This callback is invoked only when the newly selected
     * position is different from the previously selected position or if
     * there was no selected item.</p>
     * <p>
     * Impelmenters can call getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param parent   The AdapterView where the selection happened
     * @param view     The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id       The row id of the item that is selected
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (position) {
            case 0:
                unit = DAY;
                break;
            case 1:
                unit = HOUR;
                break;
            case 2:
                unit = MIN;
                break;
            case 3:
                unit = SEC;
                break;
            default:
                unit = SEC;
        }
    }

    /**
     * Callback method to be invoked when the selection disappears from this
     * view. The selection can disappear for instance when touch is activated
     * or when the adapter becomes empty.
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    void changeAlarm() {
        LayoutInflater inflater = getLayoutInflater();
        View myLayout = inflater.inflate(R.layout.change_alarm_time, null, false);
        time = (EditText) myLayout.findViewById(R.id.time);

        //spinner
        Spinner spinner = (Spinner) myLayout.findViewById(R.id.unit);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.time_unit, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

//spiner ends

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setView(myLayout).setTitle("Enter the time to set the Interval ")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String str = time.getText().toString();

                        if (str.equals("") || (Double.parseDouble(str)) < 0) {
                            Toast.makeText(MainActivity.this, "Enter valid Time", Toast.LENGTH_SHORT).show();
                        } else {
                            double timeEntered = (Double.parseDouble(str));
                            INTERVAL_MILLIS = (long) (timeEntered * unit);
                            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                            editor.putLong("INTERVAL_MILLIS", INTERVAL_MILLIS);
                            editor.apply();
                            //todo Cancel the previous alarm, getting error in cancel method need to look
                            new AlarmThread(TRIGGER_AT_MILLIS,CANCEL_ALARM_AND_SET_NEW_ALARM).execute(MainActivity.this);
                            Toast.makeText(MainActivity.this, time.getText() + "Inteval" + Long.toString(INTERVAL_MILLIS), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_change_time_interval) {
            changeAlarm();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Check if the permission is provide to read the external storage, if not provided than request.
     *
     * @param requestCode
     * @return
     */
    private boolean isStoragePermissionGrantedIfNotRequestAccess(int requestCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }


    /**
     * set alarm
     * read the image from the path provide and set on the  imagePreview
     *
     * @param path
     */
    private void mainMethod(String path) {

        textView.setText(path);

        try {

            //read from external storage
            if (isExternalStorageAvailable() && isExternalStorageReadOnly() && isStoragePermissionGranted()) {
                //File sdCard = Environment.getExternalStorageDirectory();
                //path = sdCard.getAbsolutePath() + "/WallpaperChanger";
                if (isEmpty(path)) {
                    return;
                }

                if (!isAlarmSet) {
                    setAlarm();
                    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putBoolean("AlarmSet", true);
                    editor.apply();
                    isAlarmSet = true;
                }

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
            }

            if (bitmap == null)
                bitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.default_img, REQUIRED_WIDTH, REQUIRED_HEIGHT);

        } catch (Exception e) {

            bitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.default_img, REQUIRED_WIDTH, REQUIRED_HEIGHT);

            e.printStackTrace();
        } finally {

            imagePreview.setImageBitmap(bitmap);
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.changeWallpaper: {
                /*
                    Call the thread class to change wallpaper.
                 */
                new ChangeWallpaperThreadClass(getApplicationContext()).execute(bitmap);
                break;
            }

            case R.id.chooseDir: {
                /*
                Check if Storage permission is granted and if not request user to grant
                And  if granted ,then select directory.
                 */
                if (isStoragePermissionGrantedIfNotRequestAccess(REQUEST_CODE_TO_CHOOSE_DIRECTORY)) {
                    selectDirectory();
                }
                break;
            }
            case R.id.nextImage:
            {
                mainMethod(path);
                break;
            }
        }
    }

    /**
     * provide a dialog box to select the directory.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void selectDirectory() {
        try {
            // Create DirectoryChooserDialog and register a callback
            DirectoryChooserDialog directoryChooserDialog;
            directoryChooserDialog = new DirectoryChooserDialog(MainActivity.this,
                    new DirectoryChooserDialog.ChosenDirectoryListener() {
                        @Override
                        public void onChosenDir(String chosenDir) {

                            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                            editor.putString(PATH, chosenDir);
                            editor.apply();

                            mainMethod(chosenDir);
                        }
                    });
            // Toggle new folder button enabling
            directoryChooserDialog.setNewFolderEnabled(false);
            // Load directory chooser dialog for initial 'm_chosenDir' directory.
            // The registered callback will be called upon final directory selection.
            directoryChooserDialog.chooseDirectory("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Used to set alarm, that will be trigger at TRIGGER_AT_MILLIS and at an interval of INTERVAL_MILLIS
     */
    private void setAlarm() {
        try {
            new AlarmThread(TRIGGER_AT_MILLIS,SET_NEW_ALARM).execute(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
