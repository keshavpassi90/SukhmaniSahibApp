package com.appoapp.sukhmanisahib;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.appoapp.sukhmanisahib.databinding.FragmentDetailBinding;
import com.appoapp.sukhmanisahib.model.NitnemModel;
import com.appoapp.sukhmanisahib.utlis.LanguagePref;
import com.appoapp.sukhmanisahib.utlis.MusicPlayerService;
import com.appoapp.sukhmanisahib.utlis.NetworkUtil;
import com.appoapp.sukhmanisahib.utlis.NoInternetDialog;

import java.util.Locale;

public class DetailFragment extends Fragment {
    private String selectedLanguage;

    private FragmentDetailBinding binding;
    private NitnemModel model;
    private MusicPlayerService musicService;
    private boolean isBound = false;
    private Handler handler = new Handler();
    private Runnable updateSeekBar;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.LocalBinder binder = (MusicPlayerService.LocalBinder) service;
            musicService = binder.getService();
            isBound = true;

            // Register listener to update UI on notification actions
            MusicPlayerService.setMusicUpdateListener(new MusicPlayerService.MusicUpdateListener() {
                @Override
                public void onPlay() {
                    binding.btnPlay.setImageResource(R.drawable.pause_icon);
                    startSeekBarUpdate();
                }

                @Override
                public void onPause() {
                    binding.btnPlay.setImageResource(R.drawable.ic_play);
                    handler.removeCallbacks(updateSeekBar);
                }

                @Override
                public void onStop() {
                    resetUI();
                }

                @Override
                public void onProgress(int current, int duration) {
                    binding.seekBar.setMax(duration);
                    binding.seekBar.setProgress(current);
                    binding.startTimeTV.setText(formatTime(current));
                    binding.endTimeTV.setText(formatTime(duration));
                }
            });

            syncUIWithService();
            startSeekBarUpdate();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String lang = LanguagePref.getLanguage(requireActivity());
        if (lang != null && !lang.isEmpty()) {
            // value available
            Log.d("LANG", "Saved language = " + lang);
            selectedLanguage =lang;
        } else {
            // value not saved yet → default language
            selectedLanguage = "pa";
        }
        if (getArguments() != null) {
            model = getArguments().getParcelable("nitnemModel");
        }

        binding.headingTV.setText(model.getTitle(selectedLanguage));
        binding.mantarTV.setText(model.getPath(selectedLanguage));

        // Back button
        binding.backRL.setOnClickListener(v -> stopAndGoBack());

        binding.settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicService != null) musicService.stopMusic();
                else sendActionToService(MusicPlayerService.ACTION_STOP);
                resetUI();
                NavHostFragment.findNavController(DetailFragment.this)
                        .navigate(R.id.settingFragment);

            }
        });
        // Play/Pause
        binding.btnPlay.setOnClickListener(v -> {
            if (!NetworkUtil.isInternetAvailable(requireActivity())) {
                NoInternetDialog.show(requireActivity());
            }else{
                if (musicService != null) {
                    if (musicService.isPlaying()) {
                        musicService.pause();
                    } else {
                        musicService.play();   // ✅ ONLY HERE
                    }
                } else {
                    sendActionToService(MusicPlayerService.ACTION_PLAY);
                }
            }

        });


        // Stop
        binding.btnStop.setOnClickListener(v -> {
            if (!NetworkUtil.isInternetAvailable(requireActivity())) {
                NoInternetDialog.show(requireActivity());
            }else{
                if (musicService != null) musicService.stopMusic();
                else sendActionToService(MusicPlayerService.ACTION_STOP);
                resetUI();
            }

        });


        // Progress color
        binding. seekBar.getProgressDrawable()
                .setColorFilter(
                        ContextCompat.getColor(requireActivity(), R.color.white),
                        PorterDuff.Mode.SRC_IN
                );

// Thumb color
        binding. seekBar.getThumb()
                .setColorFilter(
                        ContextCompat.getColor(requireActivity(), R.color.white),
                        PorterDuff.Mode.SRC_IN
                );
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (musicService != null && fromUser) {
                    musicService.seekTo(progress);

//                    // If the player was paused, start it immediately
//                    if (!musicService.isPlaying()) {
//                        musicService.play();
//                    }
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() { stopAndGoBack(); }
                });

        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (musicService != null && musicService.isPlaying()) {
                    int pos = musicService.getCurrentPosition();
                    int dur = musicService.getDuration();
                    binding.seekBar.setMax(dur);
                    binding.seekBar.setProgress(pos);
                    binding.startTimeTV.setText(formatTime(pos));
                    binding.endTimeTV.setText(formatTime(dur));
                }
                handler.postDelayed(this, 500);
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        startAndBindMusicService();
    }

    private void startAndBindMusicService() {
        Context context = requireContext();

        Intent startIntent = new Intent(context, MusicPlayerService.class);
        startIntent.putExtra("AUDIO_URL", model.getPathAudioLink());
        startIntent.putExtra("NOTIFY_TITLE", model.getTitle(selectedLanguage));
        context.startService(startIntent);   // ❌ play nahi karega ab

        Intent bindIntent = new Intent(context, MusicPlayerService.class);
        context.bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }


    private void sendActionToService(String action) {
        Intent intent = new Intent(requireContext(), MusicPlayerService.class);
        intent.putExtra("ACTION", action);
        requireContext().startService(intent);
    }

    private void syncUIWithService() {
        if (musicService != null && musicService.isPlaying())
            binding.btnPlay.setImageResource(R.drawable.pause_icon);
        else binding.btnPlay.setImageResource(R.drawable.ic_play);
    }

    private void startSeekBarUpdate() {
        handler.removeCallbacks(updateSeekBar);
        handler.post(updateSeekBar);
    }

    private void stopAndGoBack() {
        if (musicService != null) musicService.stopMusic();
        else sendActionToService(MusicPlayerService.ACTION_STOP);
        resetUI();
        NavHostFragment.findNavController(DetailFragment.this).popBackStack();
    }

    private void resetUI() {
        binding.seekBar.setProgress(0);
        binding.startTimeTV.setText("00:00");
        binding.btnPlay.setImageResource(R.drawable.ic_play);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isBound) {
            requireContext().unbindService(serviceConnection);
            isBound = false;
        }
        handler.removeCallbacks(updateSeekBar);
    }

    private String formatTime(int millis) {
        int totalSeconds = millis / 1000;

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        return String.format(
                Locale.getDefault(),
                "%02d:%02d:%02d",
                hours,
                minutes,
                seconds
        );
    }

}
