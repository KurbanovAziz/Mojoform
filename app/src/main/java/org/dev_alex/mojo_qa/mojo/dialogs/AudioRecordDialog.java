package org.dev_alex.mojo_qa.mojo.dialogs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.dev_alex.mojo_qa.mojo.BuildConfig;
import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.audio.AudioRecordController;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AudioRecordDialog extends BottomSheetDialogFragment implements MediaPlayer.OnCompletionListener {

    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static final String FILENAME_FORMAT = "yyyy_MM_dd_HH_mm_ss_SSS";
    private static final String FILE_EXT = ".3gp";

    private TextView cancelTv;
    private TextView recordTv;
    private TextView saveTv;
    private Chronometer chronometer;
    private ImageView recordIv;
    private ImageView recordControlIv;

    AlphaAnimation alphaAnimation = new AlphaAnimation(.5f, 1f);

    private FileAttachDialog.OnResultListener onResultListener;

    public AudioRecordDialog(FileAttachDialog.OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }


    @Override
    public int getTheme() {
        return R.style.AppTheme_BottomSheetDialog_Dark;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        requestRequiredPermissions();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_record_audio, container, false);
        setDialogParams();
        initViews(view);
        setViewsParams();
        setViewsListeners();
        setAnimParams();
        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    showToastMessage("Требуется дать разрешение на доступ к микрофону");
                    dismiss();
                } else if (grantResults[1] == PackageManager.PERMISSION_DENIED) {
                    showToastMessage("Требуется разрешение на запись данных");
                    dismiss();
                }
            }
        }
    }

    private void requestRequiredPermissions() {
        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSIONS);
    }

    @SuppressLint({"RestrictedApi", "VisibleForTests"})
    private void setDialogParams() {
        if (getDialog() != null) {
            BottomSheetDialog bottomSheetDialog = ((BottomSheetDialog) getDialog());
            BottomSheetBehavior<FrameLayout> behavior = bottomSheetDialog.getBehavior();
            behavior.disableShapeAnimations();
        }
    }

    private void initViews(View view) {
        cancelTv = view.findViewById(R.id.dialog_record_audio_tv_cancel);
        recordTv = view.findViewById(R.id.dialog_record_audio_tv_record);
        saveTv = view.findViewById(R.id.dialog_record_audio_tv_save);
        chronometer = view.findViewById(R.id.dialog_record_audio_chronometer);
        recordIv = view.findViewById(R.id.dialog_record_audio_iv_record);
        recordControlIv = view.findViewById(R.id.dialog_record_audio_iv_control);
    }

    private void setViewsParams() {
        recordControlIv.setEnabled(false);
        chronometer.setText(getString(R.string.dialog_record_audio_chronometer, "00", "00", "00"));
    }

    private void setViewsListeners() {
        if (cancelTv != null) cancelTv.setOnClickListener(v -> {
            releaseMedia();
            deleteRecordedAudio();
            dismiss();
        });
        if (recordIv != null) recordIv.setOnClickListener(v -> {
            setViewsStateRecordStarted();
            startRecordAudio();
        });
        if (saveTv != null) saveTv.setOnClickListener(v -> {
            Toast.makeText(requireActivity(), "Запись успешно сохранена", Toast.LENGTH_SHORT).show();
            releaseMedia();
            saveAudioFileUri();
            dismiss();
        });
        if (chronometer != null) chronometer.setOnChronometerTickListener(chronometer -> {

            long time = SystemClock.elapsedRealtime() - chronometer.getBase();
            int h = (int) (time / 3600000);
            int m = (int) (time - h * 3600000) / 60000;
            int s = (int) (time - h * 3600000 - m * 60000) / 1000;
            String hh = h < 10 ? "0" + h : h + "";
            String mm = m < 10 ? "0" + m : m + "";
            String ss = s < 10 ? "0" + s : s + "";
            chronometer.setText(getString(R.string.dialog_record_audio_chronometer, hh, mm, ss));
        });

    }

    private void setAnimParams() {
        alphaAnimation.setDuration(700);
        alphaAnimation.setFillAfter(true);
        alphaAnimation.setRepeatCount(Animation.INFINITE);
        alphaAnimation.setRepeatMode(Animation.REVERSE);
    }

    private void showToastMessage(String message) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private void setViewsStateRecordStarted() {
        recordIv.startAnimation(alphaAnimation);
        recordIv.setEnabled(false);

        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();

        recordTv.setEnabled(true);
        saveTv.setEnabled(false);

        recordControlIv.setImageResource(R.drawable.ic_record_stop_ripple);
        recordControlIv.setEnabled(true);
        recordControlIv.setOnClickListener(v -> {
            setViewsStateRecordStopped();
            stopRecordAudio();
        });
    }

    private void setViewsStateRecordStopped() {
        chronometer.stop();
        recordIv.clearAnimation();
        recordIv.setEnabled(true);

        recordTv.setEnabled(false);
        saveTv.setEnabled(true);

        recordControlIv.setImageResource(R.drawable.ic_record_play_ripple);
        recordControlIv.setEnabled(true);
        recordControlIv.setOnClickListener(v -> {
            setViewsStateRecordPlaying();
            playRecordedAudio();
        });

        recordIv.setOnClickListener(v -> {
            setViewsStateRecordStarted();
            resetRecordAudio();
            startRecordAudio();
        });
    }

    private void setViewsStateRecordPlaying() {
        recordIv.setEnabled(false);
        recordTv.setEnabled(false);
        saveTv.setEnabled(false);

        recordControlIv.setImageResource(R.drawable.ic_record_stop_ripple);
        recordControlIv.setEnabled(true);
        recordControlIv.setOnClickListener(v -> {
            stopPlayingRecordedAudio();
            setViewsStateRecordPlayingStopped();
        });
    }

    private void setViewsStateRecordPlayingStopped() {
        recordIv.setEnabled(true);
        recordTv.setEnabled(false);
        saveTv.setEnabled(true);

        recordControlIv.setImageResource(R.drawable.ic_record_play_ripple);
        recordControlIv.setEnabled(true);
        recordControlIv.setOnClickListener(v -> {
            playRecordedAudio();
            setViewsStateRecordPlaying();
        });

        recordIv.setOnClickListener(v -> {
            setViewsStateRecordStarted();
            resetRecordAudio();
            startRecordAudio();
        });
    }

    private void startRecordAudio() {
        String fileName = new SimpleDateFormat(FILENAME_FORMAT, Locale.ENGLISH).format(System.currentTimeMillis());
        for (File mediaDir : requireActivity().getExternalMediaDirs()) {
            if (mediaDir != null) {
                String path = mediaDir + "/" + fileName + FILE_EXT;
                AudioRecordController.startRecordingToFile(path);
                break;
            }
        }
    }

    private void resumeRecordAudio() {
        AudioRecordController.resumeRecording();
    }

    private void pauseRecordAudio() {
        AudioRecordController.pauseRecording();
    }

    private void stopRecordAudio() {
        AudioRecordController.stopRecording();
    }

    private void resetRecordAudio() {
        AudioRecordController.resetRecording();
    }

    private void playRecordedAudio() {
        AudioRecordController.startPlaying(this);
    }

    private void stopPlayingRecordedAudio() {
        AudioRecordController.stopPlaying();
    }

    private void releaseMedia() {
        AudioRecordController.releaseMediaResources();
    }

    private void deleteRecordedAudio() {
        if (AudioRecordController.getAudioRecordPath() != null) {
            File file = new File(AudioRecordController.getAudioRecordPath());
            try {
                file.delete();
            } catch (Exception e) {
                Log.e("MojoApp", "Error when trying delete recorded audio file " + e);
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopPlayingRecordedAudio();
        setViewsStateRecordPlayingStopped();
    }

    private void saveAudioFileUri() {
        if (AudioRecordController.getAudioRecordPath() != null) {
            File file = new File(AudioRecordController.getAudioRecordPath());
            Uri uri = FileProvider.getUriForFile(requireActivity(), BuildConfig.APPLICATION_ID + ".provider", file);
            onResultListener.onFileSaved(uri);
        }
    }
}
