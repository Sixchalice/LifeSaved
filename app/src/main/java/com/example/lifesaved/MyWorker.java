package com.example.lifesaved;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.lifesaved.UI.Login.MainActivity;
import com.example.lifesaved.UI.Settings.SettingsActivity;
import com.example.lifesaved.UI.Viewing.ViewingActivity;

import java.util.concurrent.TimeUnit;

public class MyWorker extends Worker {

    private static final String CHANNEL_ID = "pushid1";
    private static final int NOTIFICATION_ID = 12;

    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Do the work here--in this case, push notification
        Log.e("MyWorker", "doWork: ");

        //NotificationChannel
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "name ruti", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("description ruti");
            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.
            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("LifeSaved")
                    .setContentText("You have a new message!")
                    .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0,
                            new Intent(getApplicationContext(), MainActivity.class), FLAG_CANCEL_CURRENT))
                    .build();

            Log.e("sent", "sent");
            NotificationManagerCompat notificationManagerCompat =
                    NotificationManagerCompat.from(getApplicationContext());

            notificationManagerCompat.notify(NOTIFICATION_ID, notification);
        }
        return Result.success();
    }
    @Override
    public void onStopped() {
        super.onStopped();
    }

}
