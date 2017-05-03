package org.dev_alex.mojo_qa.mojo.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.dev_alex.mojo_qa.mojo.Data;
import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.custom_views.CustomDrawerItem;
import org.dev_alex.mojo_qa.mojo.fragments.DocumentsFragment;
import org.dev_alex.mojo_qa.mojo.fragments.TasksFragment;
import org.dev_alex.mojo_qa.mojo.fragments.TemplateFragment;
import org.dev_alex.mojo_qa.mojo.services.AlarmService;
import org.dev_alex.mojo_qa.mojo.services.TokenService;

public class MainActivity extends AppCompatActivity {
    public Drawer drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AlarmService.scheduleAlarm(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        initDrawer();
        drawer.setSelection(2, false);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, TasksFragment.newInstance(), "tasks").commit();
        if (getIntent().hasExtra(AlarmService.TEMPLATE_ID)) {
            String templateId = getIntent().getStringExtra(AlarmService.TEMPLATE_ID);
            String taskId = getIntent().getStringExtra(AlarmService.TASK_ID);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, TemplateFragment.newInstance(templateId, taskId)).addToBackStack(null).commit();
        }
    }

    private void initDrawer() {
        View headerView = getLayoutInflater().inflate(R.layout.drawer_header, null);
        if (Data.currentUser != null) {
            ((TextView) headerView.findViewById(R.id.user_name)).setText(Data.currentUser.firstName + " " + Data.currentUser.lastName);
        }

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
    }
}
