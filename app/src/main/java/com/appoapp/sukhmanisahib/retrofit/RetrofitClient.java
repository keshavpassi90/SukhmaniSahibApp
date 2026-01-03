package com.appoapp.sukhmanisahib.retrofit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://api.pexels.com/";
    private static ApiService api;

    public static ApiService getApi() {

        if (api == null) {

            OkHttpClient okHttp = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request authorised = original.newBuilder()
                                .addHeader("Authorization", "rhFThUXYqnmkVGlwoJC8SpgiVv7IQBDDj9gG8jCpg1QXuSUmyx9msFHY")
                                .build();
                        return chain.proceed(authorised);
                    })
                    // (optional) logcat the HTTP traffic
                    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttp)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            api = retrofit.create(ApiService.class);
        }
        return api;
    }
}
