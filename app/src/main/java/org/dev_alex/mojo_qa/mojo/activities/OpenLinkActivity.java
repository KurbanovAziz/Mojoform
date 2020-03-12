package org.dev_alex.mojo_qa.mojo.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.fragments.TemplateFragment;

public class OpenLinkActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String stringUUID = getIntent().getStringExtra("uuid_arg");

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, TemplateFragment.newInstance(stringUUID)).addToBackStack(null).commit();
    }

    public static Intent getActivityIntent(Context context, String uuid) {
        Intent intent = new Intent(context, OpenLinkActivity.class);
        intent.putExtra("uuid_arg", uuid);

        return intent;
    }
}
