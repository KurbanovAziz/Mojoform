package org.dev_alex.mojo_qa.mojo.custom_views.camerax;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import org.dev_alex.mojo_qa.mojo.R;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private static final String FILENAME_FORMAT = "yyyy_MM_dd_HH_mm_ss_SSS";

    private PreviewView previewView;
    private FrameLayout takePhotoLayout;

    private ImageCapture imageCapture;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestRequiredPermissions();
    }

    private void initViews() {
        previewView = findViewById(R.id.camerax_preview);
        takePhotoLayout = findViewById(R.id.camerax_takePhoto);
    }

    private void setViewsListeners() {
        if (takePhotoLayout != null) takePhotoLayout.setOnClickListener(v -> takePhoto());
    }

    private void setupView() {
        setContentView(R.layout.camerax_activity);
        initViews();
        setViewsListeners();
    }

    private void requestRequiredPermissions() {
        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        showToastMessage("Требуется дать разрешение на доступ к камере");
                        setResult(RESULT_CANCELED);
                        finish();
                    } else if (grantResults[1] == PackageManager.PERMISSION_DENIED) {
                        showToastMessage("Требуется разрешение на запись данных");
                        setResult(RESULT_CANCELED);
                        finish();
                    } else {
                        setupView();
                        startCamera();
                    }
                }
        }
    }

    private void showToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    Preview preview = new Preview.Builder().build();
                    preview.setSurfaceProvider(previewView.getSurfaceProvider());

                    imageCapture = new ImageCapture.Builder().build();

                    CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                    cameraProvider.unbindAll();
                    cameraProvider.bindToLifecycle(CameraActivity.this, cameraSelector, preview, imageCapture);
                } catch (ExecutionException e) {
                    Log.e("MojoApp", "Camera. Error when get future result  " + e);
                } catch (InterruptedException e) {
                    Log.e("MojoApp", "Camera. Thread interrupted when get future result  " + e);
                } catch (Exception e) {
                    Log.e("MojoApp", "Camera. Error when binding camera provider  " + e);
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;
        String fileName = new SimpleDateFormat(FILENAME_FORMAT, Locale.ENGLISH).format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MojoImages");
        }
        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                showToastMessage("Фото сохранено");
                closeAndSaveData(outputFileResults.getSavedUri());
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e("MojoApp", "Error while save photo  " + exception);
                showToastMessage("Ошибка при сохранении фото");
            }
        });
    }

    private void closeAndSaveData(Uri data) {
        Intent intent = new Intent();
        intent.setData(data);
        setResult(RESULT_OK, intent);
        finish();
    }
}
