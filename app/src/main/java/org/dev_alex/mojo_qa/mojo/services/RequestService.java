package org.dev_alex.mojo_qa.mojo.services;

import org.dev_alex.mojo_qa.mojo.App;
import org.dev_alex.mojo_qa.mojo.Data;
import org.dev_alex.mojo_qa.mojo.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    private final static MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final static MediaType MEDIA_TYPE = MediaType.parse("image/jpg");


    public static Response createPostRequest(String path, String jsonStr) throws Exception {
        OkHttpClient client = createOkHttpClient();
        String url = App.getContext().getString(R.string.host) + path;
        RequestBody body = RequestBody.create(JSON, jsonStr);
        Request.Builder requestBuilder = new Request.Builder().url(url).post(body);
        return client.newCall(requestBuilder.build()).execute();
    }

    public static Response createSendFileRequest(String path, File file) throws Exception {
        OkHttpClient client = createOkHttpClient();
        String url = App.getContext().getString(R.string.host) + path;
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(MEDIA_TYPE, file))
                .build();
        Request.Builder requestBuilder = new Request.Builder().url(url).post(requestBody);
        return client.newCall(requestBuilder.build()).execute();
    }

    public static Response createCustomTypeRequest(String path, String method, String jsonStr) throws Exception {
        OkHttpClient client = createOkHttpClient();
        String url = App.getContext().getString(R.string.host) + path;
        RequestBody body = RequestBody.create(JSON, jsonStr);
        Request.Builder requestBuilder = new Request.Builder().url(url).method(method, body);
        return client.newCall(requestBuilder.build()).execute();
    }

    public static Response createGetRequest(String path) throws Exception {
        OkHttpClient client = createOkHttpClient();
        String url = App.getContext().getString(R.string.host) + path;
        Request.Builder requestBuilder = new Request.Builder().url(url);
        return client.newCall(requestBuilder.build()).execute();
    }


    private static OkHttpClient createOkHttpClient() {
        if (Data.currentUser != null)
            return new OkHttpClient().newBuilder()
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
        String domain = App.getContext().getString(R.string.host);
        domain = domain.replace("https://", "");
        domain = domain.replace("http://", "");
        return new Cookie.Builder()
                .domain(domain)
                .path("/")
                .name("auth_token")
                .value(Data.currentUser.token)
                .secure()
                .build();
    }
}
