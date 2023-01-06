package org.dev_alex.mojo_qa.mojo.audio;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;

public class AudioRecordController {

    private static MediaRecorder mediaRecorder;
    private static MediaPlayer mediaPlayer;
    private static String filePath;

    private static void initMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(filePath);
    }

    private static void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
    }

    private static void startRecording() {
        if (mediaRecorder == null) {
            initMediaRecorder();
        }
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            Log.e("MojoApp", "Error when start audio recording " + e);
        }
    }

    public static void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    public static void recordToFile(String filePath) {
        AudioRecordController.filePath = filePath;
        startRecording();
    }

    public static void startPlaying() {
        if (mediaPlayer == null) initMediaPlayer();
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Log.e("MojoApp", "Error when trying to play recorded audio " + e);
        }
    }

    public static void stopPlaying() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
