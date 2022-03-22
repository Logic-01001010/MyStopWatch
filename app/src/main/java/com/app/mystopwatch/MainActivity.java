package com.app.mystopwatch;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button startButton;
    Button resetButton;

    TextView timeTextView;

    private Handler mHandler;

    int count = 0;

    boolean isStart = false;

    Thread thread;


    public void createNotification(String time, Intent intent) {

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");


        builder.setSmallIcon(R.mipmap.ic_main );
        builder.setContentTitle("StopWatch");
        builder.setContentText(time);
        builder.setContentIntent(pendingIntent);
        builder.setVibrate(null);



        // 사용자가 탭을 클릭하면 자동 제거
        builder.setAutoCancel(true);

        // 알림 표시
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("default", "Default Channel", NotificationManager.IMPORTANCE_LOW));

        }

        // id값은
        // 정의해야하는 각 알림의 고유한 int값
        notificationManager.notify(1, builder.build());
    }

    public void removeNotification() {

        // Notification 제거
        NotificationManagerCompat.from(this).cancel(1);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        startButton = (Button) findViewById(R.id.startButton);
        resetButton = (Button) findViewById(R.id.resetButton);
        timeTextView = (TextView) findViewById(R.id.timeTextView);

        startButton.setOnClickListener(new View.OnClickListener() { // 시작/일시정지
            @Override
            public void onClick(View view) {

                if( ! isStart ){ // 시작

                    resetButton.setEnabled(true);

                    startButton.setText("Pause");
                    isStart = !isStart;

                    thread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            while(true){
                                mHandler.sendEmptyMessage(0);

                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
                                    break;
                                }

                                count++;

                            }
                        }
                    });
                    thread.start();

                    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                    if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT){
                        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(100); // 0.1초간 진동
                    }

                } else { // 일시정지
                    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                    if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT){
                        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(100); // 0.1초간 진동
                    }

                    startButton.setText("Continue");
                    isStart = !isStart;
                    thread.interrupt();
                }



            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() { // 리셋

            @Override
            public void onClick(View view) {
                NotificationManagerCompat.from(getApplicationContext()).cancel(1);

                AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT){
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(100); // 0.1초간 진동
                }

                count = 0;
                timeTextView.setText("00:00:00");
                isStart = false;

                thread.interrupt();

                startButton.setText("Start");
                resetButton.setEnabled(false);

            }
        });


        mHandler = new Handler() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(Message msg) {

                if(msg.what == 0) {

                    

                    int sec = count;

                    int hour = sec / 3600;
                    sec %= 3600;
                    int min = sec/60;
                    sec %= 60;

                    String shour = ( hour < 10 ) ? "0" + String.valueOf( hour ) : String.valueOf( hour );
                    String smin = ( min < 10 ) ? "0" + String.valueOf( min ) : String.valueOf( min );
                    String ssec = ( sec < 10 ) ? "0" + String.valueOf( sec ) : String.valueOf( sec );

                    String time = shour + ":" + smin + ":" + ssec;

                    Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                    notificationIntent.setAction(Intent.ACTION_MAIN);
                    notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    createNotification(time, notificationIntent);

                    timeTextView.setText( time );
                }
            }
        };

    }







}