package com.christo.moneyplant.services.WebSocket;

import com.christo.moneyplant.models.transaction.TransactionInfo;
import com.christo.moneyplant.models.ws.response.BinInfo;
import com.christo.moneyplant.models.ws.response.Response;
import com.christo.moneyplant.models.ws.response.AnalyzeWasteReport;

public interface CallbackHandlers {

    void onConnect(Response<BinInfo> response);

    void onLidOpen(Response<Boolean> response);

    void onLidClose(Response<Boolean> response);

    void onAnalyzeWaste(Response<AnalyzeWasteReport> response);

    void onConfirmTransaction (Response<TransactionInfo> response);
    void onDisconnect();

    void onError(String message);
}
