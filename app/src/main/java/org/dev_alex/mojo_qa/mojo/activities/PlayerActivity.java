package org.dev_alex.mojo_qa.mojo.activities;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;


import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.util.Util;


import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.dev_alex.mojo_qa.mojo.services.TokenService;
import org.jsoup.Connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import okhttp3.Response;


/**
 * A fullscreen activity to play audio or video streams.
 */
public class PlayerActivity extends AppCompatActivity {

    private PlaybackStateListener playbackStateListener;
    private static final String TAG = PlayerActivity.class.getName();
    private final ScheduledExecutorService scheduler
            = Executors.newScheduledThreadPool(1);
    private PlayerView playerView;
    private static SimpleExoPlayer player;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    AudioManager audioManager;
    boolean isAudio = true;
    int volumeLevel;
    CardView cardBack;
    ImageView progress_bar;
    String videoName;
    public static PlayerActivity playerActivity;
    public static Context context;
    public boolean isDelete;

    private int season = 0;
    private int episode = 0;
    private boolean isSerialResume = false;
    //Minimum Video you want to buffer while Playing
    private final int MIN_BUFFER_DURATION = 5000;
    //Max Video you want to buffer during PlayBack
    private final int MAX_BUFFER_DURATION = 5000;
    //Min Video you want to buffer before start Playing it
    private final int MIN_PLAYBACK_START_BUFFER = 1500;
    //Min video You want to buffer when user resumes video
    private final int MIN_PLAYBACK_RESUME_BUFFER = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        init();
        Log.e("myTag", "This is my message");
        Log.e("myTag", "хрень");

        progress_bar = findViewById(R.id.image_progress);

        ObjectAnimator animation = ObjectAnimator.ofFloat(progress_bar, View.ROTATION_Y, 0.0f, 360f);
        animation.setDuration(2400);
        animation.setRepeatCount(ObjectAnimator.INFINITE);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.start();



        cardBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Log.e("myTag", "хрень2");
    }
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(PlayerActivity.this, MainActivity.class);
        intent.putExtras(getIntent());
        intent.setData(getIntent().getData());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void init() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        cardBack = (CardView) findViewById(R.id.back_card);
        playerView = findViewById(R.id.video_view);
        playbackStateListener = new PlaybackStateListener();
        context = getApplicationContext();
        playerActivity = this;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            try {
                initializePlayer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();

    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

    }





    private void initializePlayer() throws IOException {

            if (player == null) {
                LoadControl loadControl = new DefaultLoadControl.Builder()
                        .setAllocator(new DefaultAllocator(true, 16))
                        .setBufferDurationsMs(MIN_BUFFER_DURATION,
                                MAX_BUFFER_DURATION,
                                MIN_PLAYBACK_START_BUFFER,
                                MIN_PLAYBACK_RESUME_BUFFER)
                        .setTargetBufferBytes(-1)
                        .setPrioritizeTimeOverSizeThresholds(true).createDefaultLoadControl();
                DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
                trackSelector.setParameters(
                        trackSelector.buildUponParameters().setMaxVideoSizeSd());
                player = new SimpleExoPlayer.Builder(this)
                        .setTrackSelector(trackSelector)
                        .setLoadControl(loadControl)
                        .build();
            }
            playerView.setPlayer(player);
            String videoName = String.valueOf(getIntent().getData());
        videoName = videoName.replace ("https://system.mojoform.com/attachment/", "");
        videoName =  "https://system.mojoform.com/api/file/get/" + videoName;

        videoName = "https://downloader.disk.yandex.ru/disk/6874ede5cbd7fb1ea3803d408693bdb75f5640dbe57de3baa153196e83a12fe9/615268ce/6r_Sv6yTOctaBfG7sL4kKnNxxWvifgMiA26ahA24dM321JmoCqPjmy6HwipGNJIShNKVMx2oh84FdmUdnEewpA%3D%3D?uid=0&filename=71d99fbf-7471-45d5-8ab4-913c408f6906%20%2810%29.mp4&disposition=attachment&hash=9OtirFALoU0jn9gTnx%2BtXmd5kCa75i2LjpdWLZKOEhc9eup4gEEZhSmzbOtnAaL0q/J6bpmRyOJonT3VoXnDag%3D%3D%3A&limit=0&content_type=video%2Fmp4&owner_uid=597126353&fsize=6922340&hid=c9913ce025471f96d717faeff97903e8&media_type=video&tknv=v2";
       // String url = "https://mojo-qa.dev-alex.org/" + TokenService.getToken() + "/api/file/download/4081";
        //TokenService.updateToken();
        String ur2l = "https://mojo-qa.dev-alex.org/api/file/download/4081?auth_token=" + TokenService.getToken();
        Intent intent = getIntent();
        String url = intent.getStringExtra("video");


        if (url != null && url.startsWith("https://system.mojoform.com/attachment/")){
            url = url.replace("https://system.mojoform.com/attachment/", "");
            url = "https://system.mojoform.com/api/file/download/" + url + "?auth_token=" + TokenService.getToken();
            Log.e("b", url);
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(url)
                    .build();
            player.setMediaItem(mediaItem);
            player.setPlayWhenReady(true);
            player.seekTo(currentWindow, playbackPosition);
            player.addListener(playbackStateListener);
            player.prepare();
        }
        if (url != null && url.startsWith("https://mojo-qa.dev-alex.org/attachment/")){
            url = url.replace("https://mojo-qa.dev-alex.org/attachment/", "");
            url = "https://mojo-qa.dev-alex.org/api/file/get/" + url + "?auth_token=" + TokenService.getToken();
            Log.e("с", url);

            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(url)
                    .build();
            player.setMediaItem(mediaItem);
            player.setPlayWhenReady(true);
            player.seekTo(currentWindow, playbackPosition);
            player.addListener(playbackStateListener);
            player.prepare();
        }




            //"https://drive.google.com/uc?export=download&id=1Ql63EqziGH8480Fl7JhLnd1LfPWHe7vd"
            //"https://drive.google.com/uc?export=download&id=10mMXKzuOQp3Ae1EIxnZc0yG_V_mYExrN"
            ////"drive.google.com/uc?export=download&id=1J5s24cNymTyF8escKCvqtkboVvlznINh"
            // //"https://drive.google.com/uc?export=download&id=1Cea8Yelq9OKbk4BsghE6UodA_kRT6dEm"

    }



    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.removeListener(playbackStateListener);
            player.release();
            player = null;
        }
    }

    private static void printToast() {

    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private class PlaybackStateListener implements Player.EventListener {

        @Override
        public void onPlaybackStateChanged(int playbackState) {
            String stateString;
            switch (playbackState) {
                case ExoPlayer.STATE_IDLE:
                    stateString = "ExoPlayer.STATE_IDLE      -";
                    progress_visible(false);


                    break;
                case ExoPlayer.STATE_BUFFERING:
                    stateString = "ExoPlayer.STATE_BUFFERING -";
                    //progress_visible(false);

                    break;
                case ExoPlayer.STATE_READY:
                    stateString = "ExoPlayer.STATE_READY     -";
                    progress_visible(false );
                    break;
                case ExoPlayer.STATE_ENDED:
                    stateString = "ExoPlayer.STATE_ENDED     -";
                    break;
                default:
                    stateString = "UNKNOWN_STATE             -";
                    break;
            }
            Log.d(TAG, "changed state to " + stateString);
        }
    }

    private void progress_visible(boolean visible) {
        if(visible){
            progress_bar.setVisibility(View.VISIBLE);}
        else{
            progress_bar.setVisibility(View.GONE);
        }

    }


    }
