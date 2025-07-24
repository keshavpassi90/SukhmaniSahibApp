package com.appoapp.sukhmanisahib;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        AudioManager.OnAudioFocusChangeListener {

    private MainActivity mainActivity;
    private Handler mHandler;
    private static MediaPlayer mediaPlayer;
    private MediaSessionCompat mediaSession;
    private static final String CHANNEL_ID = "MusicPlayerChannel";
    private final IBinder binder = new LocalBinder();
    private AudioManager audioManager;
    static boolean isPlaying = false;
    public static Boolean serviceRunning = false;
    String filename;
    String filepath;
    String title;

    public class LocalBinder extends Binder {
        public MusicService getServiceInstance() {
            return MusicService.this;
        }
    }

    public static boolean isServiceRunning() {
        return mediaPlayer != null;
    }

    public void registerClient(MainActivity activity) {
        mainActivity = activity;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mHandler = new Handler();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mediaSession = new MediaSessionCompat(this, "MusicService");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        createNotificationChannel();
        createMediaSessionCallback();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceRunning = true;
        filename = PlayerConstants.SONG_NAME;
        title = PlayerConstants.SONG_TITLE;

        if (intent != null) {
            String action = intent.getAction();
            if ("ACTION_PLAY".equals(action)) {
                playMusic();
            } else if ("ACTION_PAUSE".equals(action)) {
                pauseMusic();
            } else if ("ACTION_STOP".equals(action)) {
                stopMusic();
            } else {
                playSong(); // Default action
            }
        }
        return START_STICKY;
    }

    private void createMediaSessionCallback() {
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                playMusic();
            }

            @Override
            public void onPause() {
                pauseMusic();
            }

            @Override
            public void onStop() {
                stopMusic();
            }
        });
    }

    public void stop() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void createNotification() {
        Intent playIntent = new Intent(this, MusicService.class).setAction("ACTION_PLAY");
        Intent pauseIntent = new Intent(this, MusicService.class).setAction("ACTION_PAUSE");

        PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Intent to open MainActivity when the notification is clicked
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(this, 0, mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_file)
                .setContentTitle("You are listening to " + PlayerConstants.SONG_NAME)
                .setContentText(isPlaying ? "Playing Music" : "Paused Music")
                .setOngoing(true) // Makes notification non-dismissable
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true) // Prevents re-alerting on update
                .setAutoCancel(false) // Prevents auto-dismissal when tapped
                .setContentIntent(mainActivityPendingIntent) // Opens MainActivity when notification is clicked
                .addAction(new NotificationCompat.Action(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play, isPlaying ? "Pause" : "Play", isPlaying ? pausePendingIntent : playPendingIntent))
                .setStyle(new MediaStyle().setShowActionsInCompactView());

        startForeground(1, notificationBuilder.build());
    }

    private void playSong() {
        if (!requestAudioFocus()) return;

        String filepath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/" + PlayerConstants.SONG_NAME + ".mp3";
        File foundFile = new File(filepath);
        mediaPlayer.reset();

        try {
            if (foundFile.exists()) {
                mediaPlayer.setDataSource(filepath);
            } else {
                Uri url = Uri.parse(PlayerConstants.SONG_URL);
                mediaPlayer.setDataSource(getApplicationContext(), url);
            }
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        isPlaying = true;
        mediaSession.setActive(true);
        Timer timer = new Timer();

        TimerTask delayedThreadStartTask = new TimerTask() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                                    mainActivity.seekBarsetMax(mediaPlayer.getDuration());
                                    mainActivity.changeButton("Pause");
                                }
                            }
                        });
                    }
                }).start();
            }
        };

        timer.schedule(delayedThreadStartTask, 2000);
        createNotification();
    }

    private void playMusic() {
        if (!isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
            PlayerConstants.SONG_PAUSED = false;
            mainActivity.changeButton("Pause");
            createNotification();
        }
    }

    private void pauseMusic() {
        if (isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
            createNotification();
            PlayerConstants.SONG_PAUSED = true;
            mainActivity.changeButton("Play");
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopMusic();
    }

    private void stopMusic() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            isPlaying = false;
            mediaSession.setActive(false);
            stopForeground(true); // Remove notification
            PlayerConstants.SONG_PAUSED = true;
            mainActivity.changeButton("Play");
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceRunning = false;
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaSession.release();
        abandonAudioFocus();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Player",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private boolean requestAudioFocus() {
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void abandonAudioFocus() {
        audioManager.abandonAudioFocus(this);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // Handle errors as needed
        return true;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mediaPlayer == null) {
                    playSong();
                } else if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                stopMusic();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    isPlaying = false;
                    createNotification();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.setVolume(0.1f, 0.1f);
                }
                break;
        }
    }
}
