package com.christo.moneyplant.services;

import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ErrorInterceptor implements Interceptor {

    private static final String TAG = "Error message interceptor";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        if (response.code() != 200 && response.body() != null) {
            Log.d(TAG, "url : " + request.url() + "intercept:  " + response.body().string());
            try {
                JsonObject errorBody = JsonParser.parseString(response.body().string()).getAsJsonObject();
                String error = errorBody.get("detail").getAsString();
                if (error != null){
                    response =  response.newBuilder()
                            .message(error)
                            .body(ResponseBody.create(response.body().contentType(), error))
                            .build();
                }
            }
            catch (Exception e){
                return  response;
            }
        }

        return  response;
    }
}
