package org.dev_alex.mojo_qa.mojo.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import org.dev_alex.mojo_qa.mojo.R;

public class ImageFullScreenActivity extends AppCompatActivity {

    public static final String INTENT_KEY_EXTRAS = "image_view";

    private ImageView closeIv = null;
    private ImageView contentIv = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        setContentView(R.layout.activity_image_full_screen);
        closeIv = findViewById(R.id.activity_image_full_screen_close_iv);
        contentIv = findViewById(R.id.activity_image_full_screen_content_iv);

        if (closeIv != null) closeIv.setOnClickListener(v -> finish());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            byte[] arr = extras.getByteArray(INTENT_KEY_EXTRAS);
            if (arr != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(arr, 0, arr.length);
                if (bitmap != null) contentIv.setImageBitmap(bitmap);
            }
        }
    }
}