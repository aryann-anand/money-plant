package com.christo.moneyplant.models.ws.request;

import com.christo.moneyplant.services.WebSocket.WebSocketClient;

public class AnalyzeWasteRequest extends RequestBase {
    public String type;

    public AnalyzeWasteRequest(String type) {
        super(WebSocketClient.EVENT_ANALYZE_WASTE);
        this.type = type;
    }
}
