package org.dev_alex.mojo_qa.mojo.activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.custom_views.CustomDrawerItem;
import org.dev_alex.mojo_qa.mojo.fragments.DocumentsFragment;
import org.dev_alex.mojo_qa.mojo.fragments.TasksFragment;
import org.dev_alex.mojo_qa.mojo.fragments.TemplateFragment;
import org.dev_alex.mojo_qa.mojo.models.User;
import org.dev_alex.mojo_qa.mojo.services.AlarmService;
import org.dev_alex.mojo_qa.mojo.services.LoginHistoryService;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.dev_alex.mojo_qa.mojo.services.TokenService;

import java.util.Date;
import java.util.Locale;

import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public Drawer drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_main);
        AlarmService.scheduleAlarm(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        initDrawer();
        if (drawer == null)
            return;

        drawer.setSelection(1, false);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, TasksFragment.newInstance(), "tasks").commit();
        if (getIntent().hasExtra(AlarmService.TEMPLATE_ID)) {
            String templateId = getIntent().getStringExtra(AlarmService.TEMPLATE_ID);
            String siteId = getIntent().getStringExtra(AlarmService.SITE_ID);
            String initiator = getIntent().getStringExtra(AlarmService.INITIATOR);
            String taskId = getIntent().getStringExtra(AlarmService.TASK_ID);
            String nodeForTasks = getIntent().getStringExtra(AlarmService.NODE_FOR_TASKS);
            long dueDate = getIntent().getLongExtra(AlarmService.DUE_DATE, new Date().getTime());

            if (templateId != null && !templateId.isEmpty() && taskId != null && !taskId.isEmpty())
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, TemplateFragment.newInstance(
                                templateId, taskId, nodeForTasks, dueDate, siteId, initiator)).addToBackStack(null).commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, null);
    }

    private void initDrawer() {
        View headerView = getLayoutInflater().inflate(R.layout.drawer_header, null);
        User currentUser = LoginHistoryService.getCurrentUser();
        ((TextView) headerView.findViewById(R.id.user_name)).setText(currentUser.firstName + " " + currentUser.lastName);


        drawer = new DrawerBuilder()
                .withActivity(this)
                .withDrawerWidthDp(305)
                .withHeader(headerView)
                .addDrawerItems(
                        new CustomDrawerItem(15, -10).withIdentifier(1).withName(R.string.tasks).withIcon(R.drawable.tasks),
                        new CustomDrawerItem(15, 0).withIdentifier(2).withName(R.string.documents).withIcon(R.drawable.documents),
                        new CustomDrawerItem(15, 0).withIdentifier(3).withName(R.string.exit).withIcon(R.drawable.exit)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch ((int) drawerItem.getIdentifier()) {
                            case 1:
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, TasksFragment.newInstance(), "tasks").commit();
                                break;
                            case 2:
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, DocumentsFragment.newInstance()).commit();
                                break;
                            case 3:
                                TokenService.deleteToken();
                                startActivity(new Intent(MainActivity.this, AuthActivity.class));
                                finish();
                                break;
                        }
                        return false;
                    }
                })
                .build();

        findViewById(R.id.sandwich_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen())
                    drawer.closeDrawer();
                else
                    drawer.openDrawer();
            }
        });

        if (currentUser.has_avatar)
            new DownloadUserAvatar((ImageView) headerView.findViewById(R.id.profile_image)).execute();
        else {
            headerView.findViewById(R.id.profile_image).setVisibility(View.GONE);

            String userInitials = String.format(Locale.getDefault(), "%s%s", currentUser.firstName.isEmpty() ?
                    "" : currentUser.firstName.charAt(0), currentUser.lastName.isEmpty() ? "" : currentUser.lastName.charAt(0));
            ((TextView) headerView.findViewById(R.id.user_initials)).setText(userInitials);
        }
    }

    private class DownloadUserAvatar extends AsyncTask<Void, Void, Void> {
        private ImageView avatarImageView;
        private Bitmap avatar;

        DownloadUserAvatar(ImageView avatarImageView) {
            this.avatarImageView = avatarImageView;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Response thumbResponse = RequestService.createGetRequest("/api/user/avatar.png");
                byte[] imageBytes = thumbResponse.body().bytes();
                avatar = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            } catch (Exception exc) {
                exc.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                if (avatar != null) {
                    avatarImageView.setImageBitmap(avatar);
                    LoginHistoryService.addAvatar(LoginHistoryService.getCurrentUser().username, avatar);
                }
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(123321);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}
