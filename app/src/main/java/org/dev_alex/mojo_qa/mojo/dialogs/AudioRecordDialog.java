package org.dev_alex.mojo_qa.mojo.dialogs;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.dev_alex.mojo_qa.mojo.R;

public class AudioRecordDialog extends BottomSheetDialogFragment {

    private Chronometer chronometer;

    @Override
    public int getTheme() {
        return R.style.AppTheme_BottomSheetDialog_Dark;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_record_audio, container, false);
        setDialogParams();
        initViews(view);
        setViewsParams();
        return view;
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
        chronometer = view.findViewById(R.id.dialog_record_audio_chronometer);
    }

    private void setViewsParams() {
        chronometer.setFormat("HH:mm:ss");
    }
}
