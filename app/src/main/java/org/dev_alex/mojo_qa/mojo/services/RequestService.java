package org.dev_alex.mojo_qa.mojo.services;

import org.dev_alex.mojo_qa.mojo.App;
import org.dev_alex.mojo_qa.mojo.R;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestService {
    private final static MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static Response createPostRequest(String path, String jsonStr) throws Exception {
        OkHttpClient client = createOkHttpClient();
        String url = App.getContext().getString(R.string.host) + path;
        RequestBody body = RequestBody.create(JSON, jsonStr);
        Request.Builder requestBuilder = new Request.Builder().url(url).post(body);

        if (TokenService.isTokenExists())
            requestBuilder.addHeader("auth_token", TokenService.getToken());

        return client.newCall(requestBuilder.build()).execute();
    }

    public static Response createGetRequest(String path) throws Exception {
        OkHttpClient client = createOkHttpClient();
        String url = App.getContext().getString(R.string.host) + path;
        Request.Builder requestBuilder = new Request.Builder().url(url);

        if (TokenService.isTokenExists())
            requestBuilder.addHeader("auth_token", TokenService.getToken());

        return client.newCall(requestBuilder.build()).execute();
    }


    private static OkHttpClient createOkHttpClient() {
        if (TokenService.isTokenExists())
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
                .value(TokenService.getToken())
                .secure()
                .build();
    }
}
