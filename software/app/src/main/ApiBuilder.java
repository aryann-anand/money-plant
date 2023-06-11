package com.christo.moneyplant.services;

import android.text.TextUtils;
import android.util.Log;

import com.christo.moneyplant.helpers.DateDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiBuilder {
    public static final String BASE_URL = "http://christojobyantony.zapto.org:5000/moneyplant/";
    private static Interceptor errorInterceptor = new ErrorInterceptor();

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
            .addInterceptor(errorInterceptor);
    private static Gson custom_gson = new GsonBuilder().registerTypeAdapter(Date.class, new DateDeserializer()).create();

    private static final Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(custom_gson));
    private static Retrofit retrofit = builder.build();

    public static ApiService getService () {
        return getService(null);
    }

    public static ApiService getService (final String authToken) {
        if (!TextUtils.isEmpty(authToken)) {
            AuthenticationInterceptor interceptor = new AuthenticationInterceptor(authToken);
            if (!httpClient.interceptors().contains(interceptor)) {
                httpClient.addInterceptor(interceptor);
                Log.i("Authentication Token", "getService: Interceptor added !");
            }
        }
        builder.client(httpClient.build());
        retrofit = builder.build();
        ApiService api =  retrofit.create(ApiService.class);
        return api;

    }
}
