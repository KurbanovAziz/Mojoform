package org.dev_alex.mojo_qa.mojo.custom_views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.speech.SpeechRecognizer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class CustomImageView extends androidx.appcompat.widget.AppCompatImageView {
public EditText editText;
public CustomImageView microBTN;
public Intent mSpeechRecognizerIntent;
public SpeechRecognizer mSpeechRecognizer;
    public CustomImageView(Context context) {
        super(context);
    }

    public CustomImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
/*
    public boolean onTouchEvent1(MotionEvent event, EditText editText, SpeechRecognizer speechRecognizer, CustomImageView customImageView, Intent intent) {
        super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                customImageView.setColorFilter(Color.parseColor("#CECECE"));
                speechRecognizer.stopListening();
                return true;
            case MotionEvent.ACTION_DOWN:
                customImageView.setColorFilter(Color.parseColor("#ff2400"));
                Toast.makeText(getContext(), "Говорите", Toast.LENGTH_SHORT).show();
                speechRecognizer.startListening(intent);
                return true;
        }
        return false;}
        */

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                microBTN.setColorFilter(Color.parseColor("#ff2400"));
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                return true;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_BUTTON_RELEASE:
                microBTN.setColorFilter(Color.parseColor("#CECECE"));
                mSpeechRecognizer.stopListening();
                return true;


        }
        return false;}
}
