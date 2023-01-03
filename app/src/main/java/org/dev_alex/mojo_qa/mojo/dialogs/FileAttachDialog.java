package org.dev_alex.mojo_qa.mojo.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.custom_views.camerax.CameraActivity;


public class FileAttachDialog extends DialogFragment {

    private static final int REQUEST_CODE = 1;

    private TextView menuTvCamera;
    private TextView menuTvGallery;
    private TextView menuTvDocuments;
    private TextView menuTvAudio;
    private TextView menuTvClose;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = requireActivity().getLayoutInflater().inflate(R.layout.dialog_attach_file, null);
        Dialog dialog = new AlertDialog.Builder(requireActivity())
                .setView(view)
                .create();
        setDialogParams(dialog);
        initViews(view);
        setViewsListeners();
        return dialog;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_CANCELED:
                    dismiss();
                    break;
                case Activity.RESULT_OK:
                    dismiss();
                    break;
            }
        }
    }

    private void setDialogParams(Dialog dialog) {
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void initViews(View view) {
            menuTvCamera    = view.findViewById(R.id.dialog_attach_file_tv_camera);
            menuTvGallery   = view.findViewById(R.id.dialog_attach_file_tv_gallery);
            menuTvDocuments = view.findViewById(R.id.dialog_attach_file_tv_documents);
            menuTvAudio     = view.findViewById(R.id.dialog_attach_file_tv_audio);
            menuTvClose     = view.findViewById(R.id.dialog_attach_file_tv_close);
    }

    private void setViewsListeners() {
        if (menuTvCamera != null) menuTvCamera.setOnClickListener(v -> openCamera());
    }

    private void openCamera() {
        startActivityForResult(new Intent(requireActivity(), CameraActivity.class), REQUEST_CODE);
    }
}
