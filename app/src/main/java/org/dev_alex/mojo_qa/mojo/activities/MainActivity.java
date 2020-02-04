package org.dev_alex.mojo_qa.mojo.activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.woxthebox.draglistview.DragListView;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.adapters.DraggableItemAdapter;
import org.dev_alex.mojo_qa.mojo.custom_views.CustomDrawerItem;
import org.dev_alex.mojo_qa.mojo.fragments.DocumentsFragment;
import org.dev_alex.mojo_qa.mojo.fragments.NotificationsFragment;
import org.dev_alex.mojo_qa.mojo.fragments.PanelListFragment;
import org.dev_alex.mojo_qa.mojo.fragments.TasksFragment;
import org.dev_alex.mojo_qa.mojo.fragments.TemplateFragment;
import org.dev_alex.mojo_qa.mojo.gcm.MyFirebaseMessagingService;
import org.dev_alex.mojo_qa.mojo.models.User;
import org.dev_alex.mojo_qa.mojo.services.LoginHistoryService;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.dev_alex.mojo_qa.mojo.services.TokenService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

public class MainActivity extends AppCompatActivity {
    public Drawer drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        initDrawer();
        if (drawer == null)
            return;

        drawer.setSelectionAtPosition(1, true);
        //getSupportFragmentManager().beginTransaction().replace(R.id.container, TasksFragment.newInstance(), "tasks").commit();
        if (getIntent().hasExtra(MyFirebaseMessagingService.TASK_ID)) {
            long taskId = getIntent().getIntExtra(MyFirebaseMessagingService.TASK_ID, -1);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, TemplateFragment.newInstance(
                            taskId, false)).addToBackStack(null).commit();
        }
        checkData(getIntent());
    }

    void checkData(Intent intent) {
        Uri data = intent.getData();
        if (data != null) {
            if (data.getPath().contains("pdf/view/")) {
                long documentId = Long.parseLong(data.getPath().substring(data.getPath().lastIndexOf("/") + 1));
                String name = "document_pdf_" + documentId;
                new DownloadPdfTask(documentId, name).execute();
            }

            if (data.getPath().contains("task/view/")) {
                long taskId = Long.parseLong(data.getPath().substring(data.getPath().lastIndexOf("/") + 1));
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, TemplateFragment.newInstance(
                                taskId, true)).addToBackStack(null).commit();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkData(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, null);
    }

    private void initDrawer() {
        View headerView = getLayoutInflater().inflate(R.layout.drawer_header, null);
        User currentUser = LoginHistoryService.getCurrentUser();

        if (TextUtils.isEmpty(currentUser.firstName) && TextUtils.isEmpty(currentUser.lastName))
            ((TextView) headerView.findViewById(R.id.user_name)).setText(currentUser.username);
        else
            ((TextView) headerView.findViewById(R.id.user_name)).setText(String.format(Locale.getDefault(),
                    "%s %s", TextUtils.isEmpty(currentUser.lastName) ? "" : currentUser.lastName,
                    TextUtils.isEmpty(currentUser.firstName) ? "" : currentUser.firstName));


        ArrayList<SecondaryDrawerItem> mainDraggableItems = new ArrayList<>();
        for (String str : getDrawerMenuSequence()) {
            if (str.equals("tasks"))
                mainDraggableItems.add(new CustomDrawerItem(15, mainDraggableItems.isEmpty() ? -18 : 0).withIdentifier(1).withName(R.string.tasks).withIcon(R.drawable.tasks));
            if (str.equals("docs"))
                mainDraggableItems.add(new CustomDrawerItem(15, mainDraggableItems.isEmpty() ? -18 : 0).withIdentifier(2).withName(R.string.documents).withIcon(R.drawable.documents));
            if (str.equals("analytics"))
                mainDraggableItems.add(new CustomDrawerItem(15, mainDraggableItems.isEmpty() ? -18 : 0).withIdentifier(5).withName(R.string.analystics).withIcon(R.drawable.analystics_icon));
        }

        DrawerBuilder builder = new DrawerBuilder()
                .withActivity(this)
                .withDrawerWidthDp(305)
                .withHeader(headerView);

        builder.addDrawerItems(
                mainDraggableItems.get(0),
                new DividerDrawerItem()
        );

        if (mainDraggableItems.size() > 1) {
            builder.addDrawerItems(mainDraggableItems.get(1));
        }

        if (mainDraggableItems.size() > 2) {
            builder.addDrawerItems(mainDraggableItems.get(2));
        }

        builder.addDrawerItems(
                new CustomDrawerItem(15, 0).withIdentifier(11).withName(R.string.notifications).withIcon(R.drawable.bell),
                new CustomDrawerItem(15, 0).withIdentifier(3).withName(R.string.exit).withIcon(R.drawable.exit),
                new DividerDrawerItem(),
                new CustomDrawerItem(15, 0).withIdentifier(4).withName(R.string.about_app).withIcon(R.drawable.question),
                new DividerDrawerItem(),
                new CustomDrawerItem(15, 0).withIdentifier(6).withName(R.string.change_seq).withIcon(R.drawable.drag_icon)

        );

        drawer = builder.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                switch ((int) drawerItem.getIdentifier()) {
                    case 1:
                        getSupportFragmentManager().popBackStack(null, 0);
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, TasksFragment.newInstance(), "tasks").commit();
                        break;
                    case 2:
                        getSupportFragmentManager().popBackStack(null, 0);
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, DocumentsFragment.newInstance()).commit();
                        break;
                    case 3:
                        new LogoutTask().execute();
                        break;
                    case 4:
                        Intent intent = new Intent(MainActivity.this, OnboardingActivity.class);
                        intent.putExtra("from_page", 1);
                        startActivity(intent);
                        break;
                    case 5:
                        getSupportFragmentManager().popBackStack(null, 0);
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, PanelListFragment.newInstance()).commit();
                        break;
                    case 6:
                        showDragItemsDialog();
                        break;
                    case 11:
                        getSupportFragmentManager().popBackStack(null, 0);
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, NotificationsFragment.newInstance()).commit();
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

            String userInitials;
            if (TextUtils.isEmpty(currentUser.firstName) && TextUtils.isEmpty(currentUser.lastName))
                userInitials = currentUser.username;
            else
                userInitials = String.format(Locale.getDefault(),
                        "%s%s", TextUtils.isEmpty(currentUser.firstName) ? "" : currentUser.firstName.charAt(0),
                        TextUtils.isEmpty(currentUser.lastName) ? "" : currentUser.lastName.charAt(0));

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
                Response thumbResponse = RequestService.createGetRequest("/api/user/" + LoginHistoryService.getCurrentUser().username + "/avatar.png");
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

    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }

            dir = context.getExternalCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    private void setDrawerMenuSequence(List<String> sequenceList) {
        try {
            SharedPreferences pref = getSharedPreferences("drawer_settings", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("drawer_sequence", new ObjectMapper().writeValueAsString(sequenceList));
            editor.apply();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private List<String> getDrawerMenuSequence() {
        try {
            SharedPreferences pref = getSharedPreferences("drawer_settings", Context.MODE_PRIVATE);
            String jsonString = pref.getString("drawer_sequence", null);

            List<String> seqList;
            if (jsonString == null) {
                seqList = new ArrayList<>();
                seqList.add("tasks");
                seqList.add("docs");
                seqList.add("analytics");
            } else {
                seqList = new ObjectMapper().readValue(jsonString, new TypeReference<List<String>>() {
                });
            }

            User user = LoginHistoryService.getCurrentUser();
            if (user != null && (user.is_manager == null || !user.is_manager) && (user.is_orgowner == null || !user.is_orgowner)) {
                seqList.remove("docs");
                seqList.remove("analytics");
            }

            return seqList;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showDragItemsDialog() {
        View dialogRootView = getLayoutInflater().inflate(R.layout.drag_list_view, null);
        DragListView dragListView = (DragListView) dialogRootView.findViewById(R.id.drag_list_view);
        dragListView.setLayoutManager(new LinearLayoutManager(this));

        List<String> values = getDrawerMenuSequence();
        final DraggableItemAdapter draggableItemAdapter = new DraggableItemAdapter(this, values, R.layout.draggable_card, R.id.drag_icon, false);

        dragListView.setAdapter(draggableItemAdapter, true);
        dragListView.setCanDragHorizontally(false);
        dragListView.setCanNotDragAboveTopItem(false);
        dragListView.setCanNotDragBelowBottomItem(false);

        new MaterialDialog.Builder(this)
                .title(R.string.change_seq)
                .customView(dragListView, false)
                .positiveText(R.string.close)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        List<String> set = new ArrayList<>();
                        for (int i = 0; i < 3; i++) {
                            int itemId = (int) draggableItemAdapter.getUniqueItemId(i);
                            switch (itemId) {
                                case DraggableItemAdapter.TASKS:
                                    set.add("tasks");
                                    break;
                                case DraggableItemAdapter.DOCS:
                                    set.add("docs");
                                    break;
                                case DraggableItemAdapter.ANALYTICS:
                                    set.add("analytics");
                                    break;
                            }
                        }
                        setDrawerMenuSequence(set);
                        initDrawer();
                    }
                })
                .show();
    }

    private class DownloadPdfTask extends AsyncTask<Void, Void, Integer> {
        private java.io.File resultFile;
        private long documentId;
        private String name;

        DownloadPdfTask(long documentId, String name) {
            this.documentId = documentId;
            this.name = name;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            resultFile = new File(downloadsDir, name + ".pdf");
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                if (resultFile.exists())
                    return 200;

                String url = "/api/fs-mojo/document/id/" + documentId + "/pdf";
                Response response = RequestService.createGetRequest(url);

                if (response.code() == 200) {
                    BufferedSink sink = Okio.buffer(Okio.sink(resultFile));
                    sink.writeAll(response.body().source());
                    sink.close();
                }
                response.body().close();

                return response.code();
            } catch (Exception exc) {
                exc.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            super.onPostExecute(responseCode);
            try {
                if (responseCode == null)
                    Toast.makeText(MainActivity.this, R.string.network_error, Toast.LENGTH_LONG).show();
                else if (responseCode == 200) {
                    try {
                        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                        Uri fileUri = FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName() + ".provider", resultFile);
                        viewIntent.setDataAndType(fileUri, "application/pdf");
                        viewIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(viewIntent);

                        Toast.makeText(MainActivity.this, "Сохранено в загрузках", Toast.LENGTH_LONG).show();
                    } catch (Exception exc) {
                        exc.printStackTrace();
                        Toast.makeText(MainActivity.this, "Нет приложения, которое может открыть этот файл", Toast.LENGTH_LONG).show();
                        try {
                            resultFile.delete();
                        } catch (Exception exc1) {
                            exc1.printStackTrace();
                        }
                    }
                } else
                    Toast.makeText(MainActivity.this, R.string.unknown_error, Toast.LENGTH_LONG).show();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

    private class LogoutTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            try {
                String url = "/api/user/logout";
                Response response = RequestService.createPostRequest(url, "{}");
                response.body().close();

                return response.code();
            } catch (Exception exc) {
                exc.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            super.onPostExecute(responseCode);
            TokenService.deleteToken();
            startActivity(new Intent(MainActivity.this, AuthActivity.class));
            finish();
        }
    }

}
