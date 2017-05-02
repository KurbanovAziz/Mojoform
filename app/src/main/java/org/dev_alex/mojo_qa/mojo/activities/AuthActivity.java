package org.dev_alex.mojo_qa.mojo.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
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
import org.dev_alex.mojo_qa.mojo.services.TokenService;
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
        if (!TokenService.isTokenExists())
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    replaceWithLoginFragment();
                }
            }, 3000);
        else
            new LoginTask(TokenService.getToken()).execute();
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
        private String token;
        private boolean loginWithinToken;
        private User user;

        LoginTask(String token) {
            this.token = token;
            loginWithinToken = true;
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
                if (!loginWithinToken) {
                    requestJson.put("username", username);
                    requestJson.put("password", password);
                }

                Response response;
                if (loginWithinToken)
                    response = RequestService.createGetRequest("/api/user/info");
                else
                    response = RequestService.createPostRequest("/api/user/login", requestJson.toString());

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

            if (loginWithinToken) {
                if (responseCode == 202) {
                    Data.currentUser = user;
                    LoginHistoryService.addUser(user);
                    TokenService.updateToken(user.token, user.userName);
                    startActivity(new Intent(AuthActivity.this, MainActivity.class));
                    finish();
                } else {
                    TokenService.deleteToken();
                    replaceWithLoginFragment();
                }
            } else {
                if (responseCode == null)
                    Toast.makeText(AuthActivity.this, R.string.network_error, Toast.LENGTH_LONG).show();
                else if (responseCode == 401)
                    Toast.makeText(AuthActivity.this, R.string.invalid_username_or_password, Toast.LENGTH_LONG).show();
                else if (responseCode == 202) {
                    Data.currentUser = user;
                    LoginHistoryService.addUser(user);
                    TokenService.updateToken(user.token, user.userName);
                    Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                    intent.putExtras(getIntent());
                    startActivity(intent);
                    finish();
                } else
                    Toast.makeText(AuthActivity.this, getString(R.string.unknown_error) + "  code: " + responseCode, Toast.LENGTH_LONG).show();
            }
        }
    }
}
