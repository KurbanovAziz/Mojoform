package org.dev_alex.mojo_qa.mojo.dialogs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.dev_alex.mojo_qa.mojo.R;

public class AudioRecordDialog extends BottomSheetDialogFragment {

    private static final int REQUEST_CODE_PERMISSIONS = 1;

    private TextView cancelTv;
    private TextView recordTv;
    private TextView saveTv;
    private Chronometer chronometer;
    private ImageView recordIv;
    private ImageView recordControlIv;

    @Override
    public int getTheme() {
        return R.style.AppTheme_BottomSheetDialog_Dark;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestRequiredPermissions();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_record_audio, container, false);
        setDialogParams();
        initViews(view);
        setViewsListeners();
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

    private void setViewsListeners() {
        if (cancelTv != null) cancelTv.setOnClickListener(v -> dismiss());
    }

    private void showToastMessage(String message) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
    }
}
