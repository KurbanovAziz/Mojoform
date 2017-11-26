package org.dev_alex.mojo_qa.mojo.services;

import android.util.Log;

import org.dev_alex.mojo_qa.mojo.App;
import org.dev_alex.mojo_qa.mojo.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestService {
    private final static MediaType JSON = MediaType.parse("application/json");
    private final static MediaType MEDIA_TYPE = MediaType.parse("application/octet-stream");


    public static Response createPostRequest(String path, String jsonStr) throws Exception {
        String url = App.getHost() + path;
        return createPostRequestWithCustomUrl(url, jsonStr);
    }

    public static Response createPostRequestWithCustomUrl(String url, String jsonStr) throws Exception {
        OkHttpClient client = createOkHttpClient();
        RequestBody body = RequestBody.create(JSON, jsonStr);
        Request.Builder requestBuilder = new Request.Builder().url(url).post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", Credentials.basic(Data.getTaskAuthLogin(), Data.taskAuthPass));

        Request request = requestBuilder.build();
        Log.d("mojo-log", "send file to server. request: " + request.toString());
        return client.newCall(request).execute();
    }

    public static Response createSendFileRequest(String path, MediaType mimeType, File file) throws Exception {
        Log.d("mojo-log", "send file to server. file exists: " + String.valueOf(file.exists()));
        Log.d("mojo-log", "send file to server. file about " + file.toString());

        OkHttpClient client = createOkHttpClient();
        String url = App.getHost() + path;
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(mimeType, file))
                .build();
        Request.Builder requestBuilder = new Request.Builder().url(url).post(requestBody);
        Request request = requestBuilder.build();
        Log.d("mojo-log", "send file to server. request: " + request.toString());
        return client.newCall(request).execute();
    }

    public static Response createCustomTypeRequest(String path, String method, String jsonStr) throws Exception {
        OkHttpClient client = createOkHttpClient();
        String url = App.getHost() + path;
        RequestBody body = RequestBody.create(JSON, jsonStr);
        Request.Builder requestBuilder = new Request.Builder().url(url).method(method, body);
        return client.newCall(requestBuilder.build()).execute();
    }

    public static Response createGetRequest(String path) throws Exception {
        OkHttpClient client = createOkHttpClient();
        String url = App.getHost() + path;
        Request.Builder requestBuilder = new Request.Builder().url(url);
        return client.newCall(requestBuilder.build()).execute();
    }


    private static OkHttpClient createOkHttpClient() {
        if (TokenService.isTokenExists())
            return new OkHttpClient().newBuilder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .cookieJar(new CookieJar() {
                        @Override
                        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        }

                        @Override
                        public List<Cookie> loadForRequest(HttpUrl url) {
                            final ArrayList<Cookie> oneCookie = new ArrayList<>(1);
                            oneCookie.add(createNonPersistentCookie());
                            return oneCookie;
                        }
                    })
                    .build();
        else
            return new OkHttpClient();
    }

    private static Cookie createNonPersistentCookie() {
        String domain = App.getHost();
        domain = domain.replace("https://", "");
        domain = domain.replace("http://", "");
        return new Cookie.Builder()
                .domain(domain)
                .path("/")
                .name("auth_token")
                .value(TokenService.getToken() + "")
                .secure()
                .build();
    }
}
