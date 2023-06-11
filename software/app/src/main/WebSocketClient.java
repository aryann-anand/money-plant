package com.christo.moneyplant.services.WebSocket;

import android.util.Log;

import com.christo.moneyplant.models.transaction.TransactionInfo;
import com.christo.moneyplant.models.user.User;
import com.christo.moneyplant.models.ws.request.AnalyzeWasteRequest;
import com.christo.moneyplant.models.ws.request.RequestBase;
import com.christo.moneyplant.models.ws.request.InitiateRequest;
import com.christo.moneyplant.models.ws.response.AnalyzeWasteReport;
import com.christo.moneyplant.models.ws.response.BinInfo;
import com.christo.moneyplant.models.ws.response.Response;
import com.christo.moneyplant.models.ws.response.ResponseBase;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashMap;
import java.util.Objects;

public class WebSocketClient extends dev.gustavoavila.websocketclient.WebSocketClient {
    private static final String TAG = "Web Socket Client";
    private static final Gson gson = new Gson();
    public static final String EVENT_INITIATE = "initiate";
    public static final String EVENT_TERMINATE = "terminate";
    public static final String EVENT_OPEN_BIN = "open_bin";
    public static final String EVENT_CLOSE_BIN = "close_bin";
    public static final String EVENT_ANALYZE_WASTE = "analyze_waste";
    public static final String EVENT_CONFIRM_TRANSACTION = "confirm_transaction";
    public interface ResponseCallback <T> {
        void  run (Response<T> response);
    }
    public interface RunnableString {
        void run (String message);
    }

    private final User user;
    private final CallbackHandlers callback;
    private final HashMap<String, RunnableString> events = new HashMap<>();

    public WebSocketClient(URI uri, User user, CallbackHandlers callback) {

        super(uri);
        this.user = user;
        this.callback = callback;
        this.events.put(EVENT_INITIATE, msg -> runCallback(msg, callback::onConnect, new TypeToken <Response<BinInfo>>(){}.getType()));
        this.events.put(EVENT_OPEN_BIN, msg -> runCallback(msg, callback::onLidOpen, new TypeToken <Response<Boolean>>(){}.getType()));
        this.events.put(EVENT_CLOSE_BIN, msg -> runCallback(msg, callback::onLidClose, new TypeToken <Response<Boolean>>(){}.getType()));
        this.events.put(EVENT_ANALYZE_WASTE, msg -> runCallback(msg, callback::onAnalyzeWaste, new TypeToken <Response<AnalyzeWasteReport>>(){}.getType()));
        this.events.put(EVENT_CONFIRM_TRANSACTION, msg -> runCallback(msg, callback::onConfirmTransaction, new TypeToken <Response<TransactionInfo>>(){}.getType()));

    }

    private void sendJSON(Object src) {
        Log.d(TAG, "sendJSON: "+ gson.toJson(src));
        this.send(gson.toJson(src));
    }


    @Override
    public void onOpen() {
        Log.i(TAG, "onOpen: Connected to server sending auth packet");
        this.sendJSON(new InitiateRequest(this.user.getEmail()));
    }

    @Override
    public void onTextReceived(String message) {
        Log.d(TAG, "onTextReceived: " + message);

        try {
            ResponseBase data = gson.fromJson(message, Response.class);
            if (data.getStatus_code() != 200) {
                    Response<String> errorResponse = gson.fromJson(message, new TypeToken <Response<String>>(){}.getType());
                    Log.e(TAG, "onTextReceived: Error " + errorResponse.getMessage());
                    callback.onError(errorResponse.getMessage());
                    return;
            }
            Objects.requireNonNull(events.get(data.getEvent())).run(message);
        }
        catch (JsonSyntaxException e) {
            callback.onError("Erroneous response form server");
        }
        catch (JsonParseException e) {
            callback.onError("Cannot parse message form server");
        }
        catch (Exception e) {
            e.printStackTrace();
            callback.onError(e.getMessage());
        }

    }

    private <T> void runCallback (String message, ResponseCallback<T> cb, Type type) {
        Response<T> data = gson.fromJson(message, type);
        cb.run(data);
    }

    @Override
    public void onCloseReceived() {
        Log.e(TAG, "onCloseReceived: The websockets connection has been closed !" );
    }

//    Methods to send events
    public void sendOpenBinLid() {
        sendJSON(new RequestBase(EVENT_OPEN_BIN));
    }

    public void sendCloseBinLid() {
        sendJSON(new RequestBase(EVENT_CLOSE_BIN));
    }

    public void sendAnalyzeWaste (String type) {
        sendJSON(new AnalyzeWasteRequest(type));
    }

    public void sendConfirmTransaction () {sendJSON(new RequestBase(EVENT_CONFIRM_TRANSACTION));}

    public void sendTerminate () {sendJSON(new RequestBase(EVENT_TERMINATE));}
//    Non-vital methods
    @Override
    public void onBinaryReceived(byte[] data) {

    }

    @Override
    public void onPingReceived(byte[] data) {

    }

    @Override
    public void onPongReceived(byte[] data) {

    }

    @Override
    public void onException(Exception e) {
        callback.onError(e.getMessage());
    }


}
