package com.christo.moneyplant.models.ws.request;

import com.christo.moneyplant.services.WebSocket.WebSocketClient;

public class InitiateRequest extends RequestBase {
    public String email;

    public InitiateRequest(String email) {
        super(WebSocketClient.EVENT_INITIATE);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

}
