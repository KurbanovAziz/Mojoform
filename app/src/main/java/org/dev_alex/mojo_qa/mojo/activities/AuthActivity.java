package org.dev_alex.mojo_qa.mojo.activities;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.fragments.LoginFragment;
import org.dev_alex.mojo_qa.mojo.fragments.LoginHistoryFragment;
import org.dev_alex.mojo_qa.mojo.fragments.LogoFragment;
import org.dev_alex.mojo_qa.mojo.models.User;
import org.dev_alex.mojo_qa.mojo.services.LoginHistoryService;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.dev_alex.mojo_qa.mojo.services.TokenService;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Response;

public class AuthActivity extends AppCompatActivity {
    private ProgressDialog loopDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        initDialog();
       // startActivity(new Intent(AuthActivity.this, MainActivity.class));

        if (!OnboardingActivity.isOnboardingFinished())
            startActivity(new Intent(this, OnboardingActivity.class));

        getSupportFragmentManager().beginTransaction().replace(R.id.container, LogoFragment.newInstance()).commit();
        if (!TokenService.isTokenExists())
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    replaceWithLoginFragment();
                }
            }, 2500);
        else
            new LoginTask().execute();
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

    private void initDialog() {
        loopDialog = new ProgressDialog(this, R.style.ProgressDialogStyle);
        loopDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loopDialog.setMessage(getString(R.string.loading_please_wait));
        loopDialog.setIndeterminate(true);
        loopDialog.setCanceledOnTouchOutside(false);
        loopDialog.setCancelable(false);
    }

    private void replaceWithLoginFragment() {
        try {
            if (LoginHistoryService.lastLoginUsersExists())
                getSupportFragmentManager().beginTransaction().replace(R.id.container, LoginHistoryFragment.newInstance()).commitAllowingStateLoss();
            else
                getSupportFragmentManager().beginTransaction().replace(R.id.container, LoginFragment.newInstance()).commitAllowingStateLoss();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public class LoginTask extends AsyncTask<Void, Void, Integer> {
        private String username, password;
        private boolean loginWithinToken;
        private User user;

        LoginTask() {
            loginWithinToken = true;
            //TokenService.deleteToken();
        }

        public LoginTask(String username, String password) {
            this.username = username;
            this.password = password;
            loginWithinToken = false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!loginWithinToken)
                loopDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                JSONObject requestJson = new JSONObject();
                requestJson.put("app", "android");

                if (loginWithinToken) {
                    User user = LoginHistoryService.getCurrentUser();
                    requestJson.put("username", user.username);
                    requestJson.put("refresh_token", user.refresh_token);
                    requestJson.put("device_id", TokenService.getFirebaseToken());
                } else {
                    requestJson.put("username", username);
                    requestJson.put("password", password);
                    requestJson.put("device_id", TokenService.getFirebaseToken());
                }

                Response response = RequestService.createPostRequest("/api/user/login/app", requestJson.toString());

                if (response.code() == 202 || response.code() == 200) {
                    String userJson = response.body().string();
                    user = new ObjectMapper().readValue(userJson, User.class);
                }

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
                if (loopDialog != null && loopDialog.isShowing())
                    loopDialog.dismiss();

                if (loginWithinToken) {
                    if (responseCode == 202 || responseCode == 200) {
                        if (user == null) {
                            Toast.makeText(AuthActivity.this, getString(R.string.unknown_error), Toast.LENGTH_LONG).show();
                            return;
                        }
                        LoginHistoryService.setCurrentUser(user);
                        TokenService.updateToken(user.token, user.username);
                        Intent intentData = getIntent();
                        String data = intentData.getDataString();
                        if(data != null){
                            Toast.makeText(AuthActivity.this, "выпAuth" + data, Toast.LENGTH_LONG).show();

                            if(data.contains("task")){

                                String taskId = data.substring(data.lastIndexOf("/") + 1);
                                Intent intentP = new Intent(AuthActivity.this, MainActivity.class);
                                intentP.putExtra("task", taskId);
                                startActivity(intentP);
                                finish();
                            }
                            else if(data.contains("reports")){
                                String taskId = data.substring(data.lastIndexOf("/") + 1);
                                Intent intentP = new Intent(AuthActivity.this, MainActivity.class);
                                intentP.putExtra("task", "e26906f0fb101f7d8eb1b6269f5ab3ef32152f69");
                                startActivity(intentP);
                                finish();
                            }
                            else{
                            Intent intentP = new Intent(AuthActivity.this, PlayerActivity.class);
                            intentP.putExtra("video", data);
                            startActivity(intentP);
                            finish();}
                        }
                        else {
                        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                        intent.putExtras(getIntent());
                        intent.setData(getIntent().getData());
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();}
                    } else {
                        TokenService.deleteToken();
                        replaceWithLoginFragment();
                    }
                } else {
                    if (responseCode == null)
                        Toast.makeText(AuthActivity.this, R.string.network_error, Toast.LENGTH_LONG).show();
                    else if (responseCode == 401)
                        Toast.makeText(AuthActivity.this, R.string.invalid_username_or_password, Toast.LENGTH_LONG).show();
                    else if (responseCode == 202 || responseCode == 200) {
                        if (user == null) {
                            Toast.makeText(AuthActivity.this, getString(R.string.unknown_error), Toast.LENGTH_LONG).show();
                            return;
                        }
                        LoginHistoryService.setCurrentUser(user);
                        LoginHistoryService.addUser(user);
                        TokenService.updateToken(user.token, user.username);
                        Intent intentData = getIntent();
                        String data = intentData.getDataString();
                        if(data != null){
                            Intent intentP = new Intent(AuthActivity.this, PlayerActivity.class);
                            intentP.putExtra("video", data);
                            startActivity(intentP);
                            finish();
                        }
                        else {
                        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                        intent.putExtras(getIntent());
                        intent.setData(getIntent().getData());
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();}
                    } else
                        Toast.makeText(AuthActivity.this, getString(R.string.unknown_error) + "  code: " + responseCode, Toast.LENGTH_LONG).show();
                }
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }
}
