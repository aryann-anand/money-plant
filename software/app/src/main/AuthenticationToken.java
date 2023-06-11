package com.christo.moneyplant.services;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

public class AuthenticationToken {
    public static final String  USER_INFO = "UserInfo";
    public static final String AUTHENTICATION_TOKEN_KEY = "AuthenticationToken";

    public static void saveToken (@NonNull Context context, String Token) {
        SharedPreferences.Editor editor = context.getSharedPreferences(USER_INFO, Context.MODE_PRIVATE).edit();
        editor.putString(AUTHENTICATION_TOKEN_KEY, Token);
        editor.apply();
    }

    public static void clearToken (@NonNull Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(USER_INFO, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
    }

    public static boolean isKeyAvailable (@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_INFO, Context.MODE_PRIVATE);
        Log.i("isKeyAvailable: ", sharedPreferences.getString(AUTHENTICATION_TOKEN_KEY, "null"));
        return sharedPreferences.getString(AUTHENTICATION_TOKEN_KEY, null) != null;
    }

    public static String getAuthenticationToken (@NonNull Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(USER_INFO, Context.MODE_PRIVATE);
        return sharedPreferences.getString(AUTHENTICATION_TOKEN_KEY, null);
    }
}
