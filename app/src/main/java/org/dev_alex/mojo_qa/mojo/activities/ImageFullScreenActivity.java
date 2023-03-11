package org.dev_alex.mojo_qa.mojo.activities;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.dev_alex.mojo_qa.mojo.R;

public class ImageFullScreenActivity extends AppCompatActivity {

    public static final String INTENT_KEY_EXTRAS = "image_uri";

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
            Uri uri = extras.getParcelable(INTENT_KEY_EXTRAS);
            if (uri != null) {
                Glide.with(this).load(uri).into(contentIv);
            }
        }
    }
}