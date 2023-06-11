package com.christo.moneyplant.helpers;

import android.util.Log;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class DateDeserializer implements JsonDeserializer<Date> {

    private String TAG = "GSON Date Serializer";

    @Override
    public Date deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
        String date = element.getAsString();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Log.d(TAG, "deserialize" + formatter.parse(date));
            return formatter.parse(date);
        } catch (ParseException e) {
            Log.e(TAG, "deserialize: GSON couldn't parse the string");
            return null;
        }
    }
}