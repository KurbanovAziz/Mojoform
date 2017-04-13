package org.dev_alex.mojo_qa.mojo.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.dev_alex.mojo_qa.mojo.Data;
import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.fragments.LoginFragment;
import org.dev_alex.mojo_qa.mojo.fragments.LoginHistoryFragment;
import org.dev_alex.mojo_qa.mojo.fragments.LogoFragment;
import org.dev_alex.mojo_qa.mojo.models.User;
import org.dev_alex.mojo_qa.mojo.services.LoginHistoryService;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.json.JSONObject;

import okhttp3.Response;

public class AuthActivity extends AppCompatActivity {
    private ProgressDialog loopDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        initDialog();

        getSupportFragmentManager().beginTransaction().replace(R.id.container, LogoFragment.newInstance()).commit();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (LoginHistoryService.lastLoginUsersExists())
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, LoginHistoryFragment.newInstance()).commit();
                else
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, LoginFragment.newInstance()).commit();
            }
        }, 3000);
    }

    private void initDialog() {
        loopDialog = new ProgressDialog(this, R.style.ProgressDialogStyle);
        loopDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loopDialog.setMessage(getString(R.string.loading_please_wait));
        loopDialog.setIndeterminate(true);
        loopDialog.setCanceledOnTouchOutside(false);
        loopDialog.setCancelable(false);
    }

    public class LoginTask extends AsyncTask<Void, Void, Integer> {
        private String username, password;
        private User user;

        public LoginTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                JSONObject requestJson = new JSONObject();
                requestJson.put("username", username);
                requestJson.put("password", password);
                Response response = RequestService.createPostRequest("/api/user/info", requestJson.toString());

                if (response.code() == 202)
                    user = new ObjectMapper().readValue(response.body().string(), User.class);

                return response.code();
            } catch (Exception exc) {
                exc.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            super.onPostExecute(responseCode);
            if (loopDialog != null && loopDialog.isShowing())
                loopDialog.dismiss();

            if (responseCode == null)
                Toast.makeText(AuthActivity.this, R.string.network_error, Toast.LENGTH_LONG).show();
            else if (responseCode == 401)
                Toast.makeText(AuthActivity.this, R.string.invalid_username_or_password, Toast.LENGTH_LONG).show();
            else {
                Data.currentUser = user;
                LoginHistoryService.addUser(user);
                Log.d("mojo-log", user.token);
                startActivity(new Intent(AuthActivity.this, MainActivity.class));
                finish();
            }
        }
    }
}
