package org.dev_alex.mojo_qa.mojo.custom_views.camera;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import org.dev_alex.mojo_qa.mojo.R;

import java.io.File;
import java.io.IOException;


public class CustomCamera2Activity extends AppCompatActivity {

    private static final String TAG = CustomCamera2Activity.class.getName();

    private CameraController mRxCameraController21;
    private View mFocusIndicator;

    public static void startForResult(Fragment fragment, int requestCode) {
        Intent starter = new Intent(fragment.getContext(), CustomCamera2Activity.class);
        fragment.startActivityForResult(starter, requestCode);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_camera_activity);
        File outputDir = getCacheDir(); // context being the Activity pointer
        File outputFile = null;
        try {
            outputFile = File.createTempFile("prefix", ".jpg", outputDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        findViewById(R.id.customCameraActivity_takePhoto).setOnClickListener(view -> mRxCameraController21.takePhoto());
        findViewById(R.id.customCameraActivity_switchCamera).setOnClickListener(view -> mRxCameraController21.switchCamera());
        mFocusIndicator = findViewById(R.id.customCameraActivity_focusIndicator);

        mRxCameraController21 = new CameraController(
                this,
                mRxCamerController21Callback,
                outputFile.getAbsolutePath(),
                findViewById(R.id.customCameraActivity_textureView),
                Configuration.ORIENTATION_PORTRAIT);
        mRxCameraController21.getLifecycle().onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRxCameraController21.getLifecycle().onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRxCameraController21.getLifecycle().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRxCameraController21.getLifecycle().onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRxCameraController21.getLifecycle().onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mRxCameraController21.getLifecycle().onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRxCameraController21.getLifecycle().onDestroy();
    }

    private final CameraController.Callback mRxCamerController21Callback = new CameraController.Callback() {
        @Override
        public void onFocusStarted() {
            mFocusIndicator.setVisibility(View.VISIBLE);
            mFocusIndicator.setScaleX(1f);
            mFocusIndicator.setScaleY(1f);
            mFocusIndicator.animate()
                    .scaleX(2f)
                    .scaleY(2f)
                    .setDuration(500)
                    .start();
        }

        @Override
        public void onFocusFinished() {
            mFocusIndicator.setVisibility(View.GONE);
        }

        @Override
        public void onPhotoTaken(@NonNull String photoUrl, @NonNull Integer photoSourceType) {
            Log.e("check_camera", photoUrl);
            Intent intent = new Intent();
            intent.putExtra("photo_path", photoUrl);
            setResult(RESULT_OK, intent);
            finish();
        }

        @Override
        public void onCameraAccessException() {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }

        @Override
        public void onCameraOpenException(@Nullable OpenCameraException.Reason reason) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }

        @Override
        public void onException(Throwable throwable) {
            throwable.printStackTrace();
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    };

}
