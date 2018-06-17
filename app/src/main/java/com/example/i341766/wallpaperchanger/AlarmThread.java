package com.example.i341766.wallpaperchanger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.widget.Toast;

import static android.app.AlarmManager.ELAPSED_REALTIME;

/**
 * This is a thread class to call Alarm Class at every intervalMillis
 */
public class AlarmThread extends AsyncTask<Context, Void, String> {

    static AlarmManager alarmMgr;
    static Intent intent;
    static PendingIntent alarmIntent;
    private long triggerAtMillis;
    Context context;
    int option;
    static final int SET_NEW_ALARM = 1;
    static final int CANCEL_ALARM = 2;
    static final int CANCEL_ALARM_AND_SET_NEW_ALARM = 3;


    public AlarmThread(long triggerAtMillis,int option) {
        this.triggerAtMillis = triggerAtMillis;
        this.option = option;
    }

    public void setAlarm() {
        try {
            //setting the repeating alarm that will be fired after triggerAtMillis
            alarmMgr.set(ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + triggerAtMillis,
                    alarmIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void cancelAlarm()
    {
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }
    }

    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param contexts The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected String doInBackground(Context... contexts) {

        try {
            context = contexts[0];

            ComponentName receiver = new ComponentName(context, Alarm.class);
            PackageManager pm = context.getPackageManager();

            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

            //getting the alarm manager
            alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            //creating a new intent specifying the broadcast receiver
            intent = new Intent(context, SampleBootReceiver.class);
            intent.putExtra("triggerAtMillis", triggerAtMillis);

            //creating a pending intent using the intent
            alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

            if(option == SET_NEW_ALARM){
                setAlarm();
            }
            else if(option == CANCEL_ALARM)
            {
                cancelAlarm();
            }
            else if(option == CANCEL_ALARM_AND_SET_NEW_ALARM)
            {
                cancelAlarm();
                setAlarm();
            }

        } catch (Exception e) {
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
        Toast.makeText(context, "Alarm Requested", Toast.LENGTH_SHORT).show();
    }

}
