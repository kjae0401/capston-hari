package com.capston.hari;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class ClientSocketService extends Service {
    SocketData socketData;
    private SharedPreferences LoginInformation;
    public static Intent service = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void initializeNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.bigText("설정을 보려면 누르세요.");
        style.setBigContentTitle(null);
        style.setSummaryText("서비스 동작중");
        builder.setContentText(null);
        builder.setContentTitle(null);
        builder.setOngoing(true);
        builder.setStyle(style);
        builder.setWhen(0);
        builder.setShowWhen(false);
        Intent notificationIntent = new Intent(this, Main.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(new NotificationChannel("1", "undead_service", NotificationManager.IMPORTANCE_NONE));
        }
        Notification notification = builder.build(); startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        service = intent;
        initializeNotification();
        LoginInformation = getSharedPreferences("settings", Activity.MODE_PRIVATE);
        UserInfo userInfo = UserInfo.getInstance(LoginInformation.getString("id", ""));
        notifyHandler handler = new notifyHandler();
        socketData = SocketData.getInstance(handler);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        socketData = null;
        super.onDestroy();

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 3);
        Intent intent = new Intent(this, SMSReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0,intent,0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        socketData = null;
        super.onTaskRemoved(rootIntent);

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 3);
        Intent intent = new Intent(this, SMSReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0,intent,0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    class notifyHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 3) {
                Intent delete = new Intent("com.exaple.delete");
                delete.putExtra("room", NotifyData.room);
                delete.putExtra("money", NotifyData.money);
                delete.putExtra("date", NotifyData.date);
                sendBroadcast(delete);
            }if (msg.what == 2) {
                Intent change = new Intent("com.exaple.change");
                change.putExtra("room", NotifyData.room);
                change.putExtra("money", NotifyData.money);
                change.putExtra("use", NotifyData.use);
                change.putExtra("date", NotifyData.date);
                sendBroadcast(change);
            } else if (msg.what == 1) {
                Intent create = new Intent("com.example.create");
                create.putExtra("room", NotifyData.room);
                sendBroadcast(create);
                //LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(create);
            } else if(msg.what == 0) {
                Intent nintent = new Intent("com.example.broadcastreceiver");
                nintent.putExtra("title", NotifyData.Title);
                nintent.putExtra("content", NotifyData.contents);
                nintent.putExtra("date", NotifyData.date);
                sendBroadcast(nintent);
                //LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(nintent);

                if (Foreground.get().isBackground()) {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
                        /* 누가버전 이하 노티처리
                         *  Toast.makeText(getApplicationContext(),"누가버전이하",Toast.LENGTH_SHORT).show(); */
                        Intent intent = new Intent();

                        BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.mipmap.ic_launcher);
                        Bitmap bitmap = bitmapDrawable.getBitmap();

                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext()).
                                setLargeIcon(bitmap)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setWhen(System.currentTimeMillis()).
                                        setShowWhen(true).
                                        setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_MAX)
                                .setContentTitle(NotifyData.Title).setContentText(NotifyData.User + " : " + NotifyData.contents) // 방이름 나오도록  .setContentText("123")
                                .setDefaults(Notification.DEFAULT_VIBRATE)
                                .setFullScreenIntent(pendingIntent, true)
                                .setContentIntent(pendingIntent);

                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(0, builder.build());
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        /**   Toast.makeText(getApplicationContext(),"오레오이상",Toast.LENGTH_SHORT).show();
                         * 오레오 이상 노티처리
                         *    BitmapDrawable bitmapDrawable = (BitmapDrawable)getResources().getDrawable(R.mipmap.ic_launcher);
                         *    Bitmap bitmap = bitmapDrawable.getBitmap();
                         * 오레오 버전부터 노티를 처리하려면 채널이 존재해야합니다.
                         */

                        int importance = NotificationManager.IMPORTANCE_HIGH;
                        String Noti_Channel_ID = "Noti";
                        String Noti_Channel_Group_ID = "Noti_Group";

                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        NotificationChannel notificationChannel = new NotificationChannel(Noti_Channel_ID, Noti_Channel_Group_ID, importance);

//                    notificationManager.deleteNotificationChannel("testid"); 채널삭제

                        // 채널이 있는지 체크해서 없을경우 만들고 있으면 채널을 재사용합니다.
                        if (notificationManager.getNotificationChannel(Noti_Channel_ID) != null) {
                            //    Toast.makeText(getApplicationContext(),"채널이 이미 존재합니다.",Toast.LENGTH_SHORT).show();
                        } else {
                            //    Toast.makeText(getApplicationContext(),"채널이 없어서 만듭니다.",Toast.LENGTH_SHORT).show();
                            notificationManager.createNotificationChannel(notificationChannel);
                        }

                        notificationManager.createNotificationChannel(notificationChannel);
//                    Log.e("로그확인","===="+notificationManager.getNotificationChannel("testid1"));
//                    notificationManager.getNotificationChannel("testid");

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Noti_Channel_ID)
                                .setLargeIcon(null).setSmallIcon(R.mipmap.ic_launcher)
                                .setWhen(System.currentTimeMillis()).setShowWhen(true).
                                        setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_MAX)
                                .setContentTitle(NotifyData.Title).setContentText(NotifyData.User + " : " + NotifyData.contents); // 방이름 나오도록  .setContentText("123")
//                            .setContentIntent(pendingIntent);
//                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(0, builder.build());
                    }
                }
            }
        }
    }
}