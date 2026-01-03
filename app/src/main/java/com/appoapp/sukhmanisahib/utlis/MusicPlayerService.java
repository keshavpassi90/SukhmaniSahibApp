package com.appoapp.sukhmanisahib.utlis;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.appoapp.sukhmanisahib.R;

import java.io.IOException;

public class MusicPlayerService extends Service {

    public static final String CHANNEL_ID = "MusicChannel";
    public static final int NOTIFICATION_ID = 1;

    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_STOP = "ACTION_STOP";

    private MediaPlayer mediaPlayer;
    private final IBinder binder = new LocalBinder();
    private boolean isPaused = false;
    private String title = "Music Player";
    private String audioUrl;

    // ======= Listeners =======
    private static OnMusicCompletionListener completionListener;
    private static MusicUpdateListener updateListener;

    public static void setOnMusicCompletionListener(OnMusicCompletionListener listener) {
        completionListener = listener;
    }

    public static void setMusicUpdateListener(MusicUpdateListener listener) {
        updateListener = listener;
    }

    public interface OnMusicCompletionListener {
        void onMusicCompleted();
    }

    public interface MusicUpdateListener {
        void onPlay();
        void onPause();
        void onStop();
        void onProgress(int current, int duration);
    }

    // ======= Binder =======
    public class LocalBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null) return START_STICKY;

        if (intent.hasExtra("AUDIO_URL")) {
            audioUrl = intent.getStringExtra("AUDIO_URL");
            title = intent.getStringExtra("NOTIFY_TITLE");
        }

        String action = intent.getStringExtra("ACTION");

        if (action != null) {
            switch (action) {
                case ACTION_PLAY:
                    play();
                    break;
                case ACTION_PAUSE:
                    pause();
                    break;
                case ACTION_STOP:
                    stopMusic();
                    break;
            }
        }

        return START_STICKY;
    }

    private void initMediaPlayer() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                isPaused = false;
                notifyPlay();
                showNotification();
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                if (completionListener != null) completionListener.onMusicCompleted();
                notifyStop();
                stopMusic();
            });
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ======= Playback Control =======
    public void play() {
        if (mediaPlayer == null) {
            if (audioUrl != null) initMediaPlayer();
        } else if (isPaused) {
            mediaPlayer.start();
            isPaused = false;
            notifyPlay();
            updateNotification();
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPaused = true;
            notifyPause();
            updateNotification();
        }
    }

    public void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPaused = false;
        notifyStop();
        stopForeground(true);
        stopSelf();
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) mediaPlayer.seekTo(position);
    }

    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    // ======= Notification =======
    private void showNotification() {
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    private void updateNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, buildNotification());
    }

    private Notification buildNotification() {

        PendingIntent playIntent = PendingIntent.getBroadcast(
                this, 0,
                new Intent(this, NotificationActionReceiver.class).setAction(ACTION_PLAY),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        PendingIntent pauseIntent = PendingIntent.getBroadcast(
                this, 1,
                new Intent(this, NotificationActionReceiver.class).setAction(ACTION_PAUSE),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        PendingIntent stopIntent = PendingIntent.getBroadcast(
                this, 2,
                new Intent(this, NotificationActionReceiver.class).setAction(ACTION_STOP),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(isPaused ? "Paused" : "Playing")
                .setSmallIcon(R.drawable.splash_logo)
                .addAction(R.drawable.ic_notify_play, "Play", playIntent)
                .addAction(R.drawable.ic_notify_pause, "Pause", pauseIntent)
                .addAction(R.drawable.ic_notify_stop, "Stop", stopIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle())
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(!isPaused)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Music Playback", NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    // ======= Notify Fragment =======
    private void notifyPlay() {
        if (updateListener != null) updateListener.onPlay();
    }

    private void notifyPause() {
        if (updateListener != null) updateListener.onPause();
    }

    private void notifyStop() {
        if (updateListener != null) updateListener.onStop();
    }

    @Override
    public void onDestroy() {
        stopMusic();
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopMusic();
    }
}
