package com.example.i341766.wallpaperchanger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

import static android.app.AlarmManager.ELAPSED_REALTIME;
import static android.content.Context.MODE_PRIVATE;
import static com.example.i341766.wallpaperchanger.MainActivity.MY_PREFS_NAME;


/**
 * The request is received after booting the device,
 * it then set the alarm.
 */
public class SampleBootReceiver extends BroadcastReceiver {

    static AlarmManager alarmMgr;
    static Intent intent1;
    static PendingIntent alarmIntent;
    private long triggerAtMillis;
    private long intervalMillis;

    public void setAlarm() {
        try {
            alarmMgr.setInexactRepeating(ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + triggerAtMillis,
                    intervalMillis, alarmIntent);
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
    @Override
    public void onReceive(Context context, Intent intent) {
        try {

            SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            triggerAtMillis = prefs.getLong("TRIGGER_AT_MILLIS", AlarmManager.INTERVAL_FIFTEEN_MINUTES);
            intervalMillis = prefs.getLong("INTERVAL_MILLIS", AlarmManager.INTERVAL_DAY);

            //     if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            //getting the alarm manager
            alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            //creating a new intent specifying the broadcast receiver
            intent1 = new Intent(context, Alarm.class);

            //creating a pending intent using the intent
            alarmIntent = PendingIntent.getBroadcast(context, 0, intent1, 0);
            setAlarm();
            //   }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}