package com.christo.moneyplant.models.ws.response;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Response<T>  extends  ResponseBase{
    private T message = null;

    Response(int status_code, String event, T message) {
        super(status_code, event);
        this.message = message;

    }
    public T getMessage() {
        return message;
    }

}
