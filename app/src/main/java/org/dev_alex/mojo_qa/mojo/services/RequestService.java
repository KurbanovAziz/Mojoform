package org.dev_alex.mojo_qa.mojo.services;

import android.util.Log;


import com.chuckerteam.chucker.api.ChuckerInterceptor;

import org.dev_alex.mojo_qa.mojo.App;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Cookie;
import okhttp3.CookieJar;
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

    public static Response createPutRequest(String path, String jsonStr) throws Exception {
        String url = App.getHost() + path;
        return createPutRequestWithCustomUrl(url, jsonStr);
    }

    public static Response createPostRequestWithCustomUrl(String url, String jsonStr) throws Exception {
        OkHttpClient client = createOkHttpClient();
        RequestBody body = RequestBody.create(jsonStr, JSON);
        Request.Builder requestBuilder = new Request.Builder().url(url).post(body)
                .addHeader("Content-Type", "application/json");
        Request request = requestBuilder.build();
        Log.d("mojo-log", "send file to server. request: " + request.toString());
        Call call = client.newCall(request);
        return call.execute();
    }

    public static Response createPutRequestWithCustomUrl(String url, String jsonStr) throws Exception {
        OkHttpClient client = createOkHttpClient();
        RequestBody body = RequestBody.create(JSON, jsonStr);
        Request.Builder requestBuilder = new Request.Builder().url(url).put(body)
                .addHeader("Content-Type", "application/json");
        //.addHeader("Authorization", Credentials.basic(Data.getTaskAuthLogin(), Data.taskAuthPass));

        Request request = requestBuilder.build();
        Log.d("mojo-log", "send file to server. request: " + request.toString());
        return client.newCall(request).execute();
    }

    public static Response createSendFilePostRequest(String path, MediaType mimeType, File file) throws Exception {
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

    public static Response createSendFilePutRequest(String path, MediaType mimeType, File file) throws Exception {
        Log.d("mojo-log", "send file to server. file exists: " + String.valueOf(file.exists()));
        Log.d("mojo-log", "send file to server. file about " + file.toString());

        OkHttpClient client = createOkHttpClient();

        String url = App.getHost() + path;
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(mimeType, file))
                .build();
        Request.Builder requestBuilder = new Request.Builder().url(url).put(requestBody);
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

    //ЛЮТАЯ ГРЯЗЬ, НЕ ЗАБЫТЬ ПЕРЕДЕЛАТЬ
    public static Response createGetRequestWithQuery(String path, long from, long to) throws Exception {
        OkHttpClient client = createOkHttpClient();
        String url = App.getHost() + path;
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        urlBuilder.addQueryParameter("time_length", String.valueOf(to - from));
        urlBuilder.addQueryParameter("last", String.valueOf(to));
        Request.Builder requestBuilder = new Request.Builder().url(urlBuilder.build());
        return client.newCall(requestBuilder.build()).execute();
    }



    private static OkHttpClient createOkHttpClient() {
        if (TokenService.isTokenExists())
            return new OkHttpClient.Builder()
                    .addInterceptor(new ChuckerInterceptor(App.getContext()))                            .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(35, TimeUnit.SECONDS)
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
        else {
            return new OkHttpClient.Builder()
                    .addInterceptor(new ChuckerInterceptor(App.getContext()))
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(35, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

        }
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
