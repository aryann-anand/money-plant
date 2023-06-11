package com.christo.moneyplant.services;

import android.text.TextUtils;
import android.util.Log;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BinConnectBuilder {
        public static final String BASE_URL = "http://192.168.10.1:4000/";
        private static Interceptor errorInterceptor = new ErrorInterceptor();

        private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .addInterceptor(errorInterceptor);

        private static final Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());
        private static Retrofit retrofit = builder.build();

        public static ApiService getService () {
            builder.client(httpClient.build());
            retrofit = builder.build();
            ApiService api =  retrofit.create(ApiService.class);
            return api;
        }
}
