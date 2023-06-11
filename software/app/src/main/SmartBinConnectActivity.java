package com.christo.moneyplant.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Bundle;

import com.christo.moneyplant.R;
import com.christo.moneyplant.databinding.ActivitySmartBinConnectBinding;
import com.christo.moneyplant.models.transaction.TransactionInfo;
import com.christo.moneyplant.models.user.User;
import com.christo.moneyplant.models.ws.response.AnalyzeWasteReport;
import com.christo.moneyplant.models.ws.response.BinInfo;
import com.christo.moneyplant.models.ws.response.Response;
import com.christo.moneyplant.services.WebSocket.WebSocketClient;
import com.christo.moneyplant.services.WebSocket.CallbackHandlers;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class SmartBinConnectActivity extends AppCompatActivity {

    private static final String TAG = "SmartBinConnect";
    private ActivitySmartBinConnectBinding binding;
    private final static String WEB_SOCKET_URI = "ws://192.168.0.10:8001/";
    private WebSocketClient webSocketClient;
    private User user;
    private ArrayAdapter<String> wasteItemsListAdapter;
    private final ArrayList<String> wasteItemsList = new ArrayList<>(
            Arrays.asList("Plastic: 0.0$/kg ", "Paper based: 0.0$/kg", "Wet Waste: 0.0$/kg", "Glass: 0.0$/kg", "Aluminium Cans: 0.0$/kg")
    );

    private SmartBinInfoFragment binInfoFragment;
    public boolean CANT_CONNECT = false;


    ConnectivityManager connectivityManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySmartBinConnectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();

        String SSID = intent.getStringExtra("SSID");
        String PSK = intent.getStringExtra("PSK");
        if (SSID == null || PSK == null) gotoDashBoard();
        connectToBin(SSID, PSK);

        this.user = (User) intent.getSerializableExtra("USER");
        assert this.user != null;
        Log.i(TAG, "onCreate: user email -> " + new Gson().toJson(user));

        setUIProcessing();
        binding.activitySmartBinConnectTextViewHeader.setText("Connecting to Bin ...");
        binding.activitySmartBinConnectButtonMain.setText("Connect To Bin");
        binding.textInputLayoutWasteTypeSelector.setVisibility(View.INVISIBLE);
        binding.activitySmartBinConnectButtonNegative.setOnClickListener(v->disconnectFromBin());

//        Set adapter for the waste type selector
        wasteItemsListAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.list_item, wasteItemsList);
        binding.autoCompleteWasteTypeSelector.setAdapter(wasteItemsListAdapter);

//        Setup fragment
        binInfoFragment = binding.activitySmartBinConnectFragmentBinInfo.getFragment();
    }

    private void gotoDashBoard () {
        disconnectFromBin();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void disconnectFromBin () {
        if (webSocketClient != null) {
            webSocketClient.sendTerminate();
        }
        if (connectivityManager != null) {
            connectivityManager.bindProcessToNetwork(null);
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
        displayErrorSnackBar("Disconnected from Bin");
    }

    private void setUIProcessing () {
        runOnUiThread(() ->{
            binding.activitySmartBinConnectImageViewBin.setColorFilter(getColor(R.color.yellow));
            binding.activitySmartBinConnectButtonMain.setEnabled(false);
            binding.activitySmartBinConnectProgressBar.setVisibility(View.VISIBLE);
            binding.activitySmartBinConnectTextViewHeader.setText("Waiting for response ... ");
        });
    }

    private void setUIReady (String message, String action) {
        runOnUiThread(() -> {
            binding.activitySmartBinConnectProgressBar.setVisibility(View.INVISIBLE);
            binding.activitySmartBinConnectImageViewBin.setColorFilter(getColor(R.color.green));
            binding.activitySmartBinConnectButtonMain.setEnabled(true);
            binding.activitySmartBinConnectProgressBar.setVisibility(View.INVISIBLE);
            binding.activitySmartBinConnectTextViewHeader.setText(message);
            binding.activitySmartBinConnectButtonMain.setText(action);
        });
    }


    private void displayErrorSnackBar (String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_INDEFINITE)
                .setAction("Exit", view -> gotoDashBoard())
                .show();
    }

    private void connectWebSocket () {
        URI uri = null;
        try {
            uri = new URI(WEB_SOCKET_URI);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        webSocketClient = new WebSocketClient(
                uri,
                user,
                webSocketCallBackHandlers
        );

        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(60000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }

    private void    connectToBin (String SSID, String PSK) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            connectivityManager =  (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkSpecifier networkSpecifier = new WifiNetworkSpecifier.Builder()
                    .setSsid(SSID)
                    .setWpa2Passphrase(PSK)
                    .setIsHiddenSsid(false ) //specify if the network does not broadcast itself and OS must perform a forced scan in order to connect
                    .build();

            NetworkRequest networkRequest = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .setNetworkSpecifier(networkSpecifier)
                    .build();

            connectivityManager.requestNetwork(networkRequest, networkCallback);

        } else {
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = String.format("\"%s\"", SSID);
            conf.preSharedKey = String.format("\"%s\"", PSK);
            conf.status = WifiConfiguration.Status.ENABLED;
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            int netId =  wifiManager.addNetwork(conf);
            Log.e("connect fragment", String.format("connectToBin: the netid is : %d", netId) );
            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();
        }
    }

    final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {

        @Override
        public void onAvailable(Network network) {
            super.onAvailable(network);
            connectivityManager.bindProcessToNetwork(network);
            Log.i(TAG, "onAvailable: Connection successful");
            runOnUiThread(() -> binInfoFragment.setConnectionState(1));
            setUIReady("Connected to Bin", "Authenticate");
            binding.activitySmartBinConnectButtonMain.setOnClickListener(view -> {
                connectWebSocket();
                setUIProcessing();
            });
        }

        @Override
        public void onLosing(@NonNull Network network, int maxMsToLive) {
            super.onLosing(network, maxMsToLive);
            Log.e(TAG, "onLosing");
        }

        @Override
        public void onLost(Network network) {
            super.onLost(network);
            Log.e(TAG, "losing active connection");
            connectivityManager.bindProcessToNetwork(null);
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }

        @Override
        public void onUnavailable() {
            super.onUnavailable();
            Toast.makeText(SmartBinConnectActivity.this, "Sorry we cannot connect to the bin try again later", Toast.LENGTH_LONG).show();
            CANT_CONNECT = true;
            runOnUiThread(() -> {
                gotoDashBoard();
            });
        }
    };

    final CallbackHandlers webSocketCallBackHandlers = new CallbackHandlers () {
        @Override
        public void onConnect(Response<BinInfo> response) {
            BinInfo body = response.getMessage();
            if (body.getState() == 0) {
                setUIReady("Bin is ready !", "Open Bin");
//              Update lid information on the list view
                runOnUiThread(() -> {
                    binInfoFragment.setConnectionState(2);
                    binInfoFragment.setUid(body.getUid());
                    binInfoFragment.setBinLidState(0);
                });

                binding.activitySmartBinConnectButtonMain.setOnClickListener(view->{
                    webSocketClient.sendOpenBinLid();
                    setUIProcessing();
                });

//                Add the waste types to the List View
                runOnUiThread(() -> {
                    wasteItemsListAdapter.clear();
                    for ( Map.Entry<String, Float> value : body.getWaste_types()) {
                        wasteItemsListAdapter.add(value.getKey() + ": "+ value.getValue() + " ₹/kg");
                    }
                    wasteItemsListAdapter.notifyDataSetChanged();
                });
            }
        }

        @Override
        public void onDisconnect() {
            Log.e(TAG, "onDisconnect: disconnecting");
            displayErrorSnackBar("Connection to bin terminated !");
        }

        @Override
        public void onLidOpen(Response<Boolean> response) {
            Log.d(TAG, "openBin: open bin event received");
            if (response.getMessage()) {
                setUIReady("The bin is open. \nPlease place your waste in the bin now", "Close Bin");
                runOnUiThread(()->{

                    //Reset the Bin Info state
                    binInfoFragment.setRecordedWeight(null);
                    binInfoFragment.setEstimateCredits(null);
                    binInfoFragment.setAnalysisResult(null);
                    binInfoFragment.setBinLidState(1);

                    //Reset the functioning of
                    // the disconnect button
                    binding.activitySmartBinConnectButtonNegative.setText(R.string.disconnect_from_bin);
                    binding.activitySmartBinConnectButtonNegative.setOnClickListener(v->{
                        disconnectFromBin();
                        displayErrorSnackBar("Disconnected from bin");
                    });
                });
                binding.activitySmartBinConnectButtonMain.setOnClickListener(view -> {
                    webSocketClient.sendCloseBinLid();
                    setUIProcessing();
                });
            }
        }

        @Override
        public void onLidClose(Response<Boolean> response) {
            Log.d(TAG, "closeBin: close bin event received");
            setUIReady("The bin is closed.\nPlease select the waste type deposited", "Analyze Bin");
            runOnUiThread(()-> {
//                update bin state
                binInfoFragment.setBinLidState(0);
//                display waste type selector
                binding.textInputLayoutWasteTypeSelector.setVisibility(View.VISIBLE);
//                Set the negative button to cancel transaction
                binding.activitySmartBinConnectButtonNegative.setText("Cancel Transaction");
            });
            binding.activitySmartBinConnectButtonNegative.setOnClickListener(v->{
                setUIProcessing();
                webSocketClient.sendOpenBinLid();
                Snackbar.make(binding.getRoot(), "Transaction Cancelled Successfully", BaseTransientBottomBar.LENGTH_LONG).show();
            });
            binding.activitySmartBinConnectButtonMain.setOnClickListener(view -> {
                String selection = Objects.requireNonNull(binding.textInputLayoutWasteTypeSelector.getEditText())
                        .getText()
                        .toString()
                        .split(":")[0]
                        .toLowerCase();

                if (! selection.isEmpty()) {
                    Log.d(TAG, "onLidClose: Sending waste analysis request for "+selection);
                    webSocketClient.sendAnalyzeWaste(selection);
                    setUIProcessing();
                    binding.textInputLayoutWasteTypeSelector.setVisibility(View.INVISIBLE);
                } else {
                    Snackbar.make(binding.getRoot(), "Please choose a value !", BaseTransientBottomBar.LENGTH_LONG).show();
                }

            });
        }

        @Override
        public void onAnalyzeWaste(Response<AnalyzeWasteReport> response) {
            Log.d(TAG, "onAnalyzeWaste: waste analysis report received "+response.getMessage().verification);
            AnalyzeWasteReport report = response.getMessage();
            runOnUiThread(() -> binInfoFragment.setAnalysisResult(report.verification ? 1 : 0));
            if (report.verification) {
                //If the verification succeeds update the bin info section and set button to confirm transaction
                runOnUiThread(()->{
                    binInfoFragment.setEstimateCredits(report.credits);
                    binInfoFragment.setRecordedWeight(report.weight);
                });
                setUIReady("Waste Analysis Successful", "Confirm Transaction");
                binding.activitySmartBinConnectButtonMain.setOnClickListener(v -> {
                    setUIProcessing();
                    webSocketClient.sendConfirmTransaction();
                });
            } else {
                //If the verification fails, the direct the user to cancel the transaction
                setUIReady("Waste Analysis, could not verify material type", "Open Bin Lid");
                runOnUiThread(()->binding.activitySmartBinConnectImageViewBin.setColorFilter(getColor(R.color.red)));
            }

        }

        @Override
        public void onConfirmTransaction(Response<TransactionInfo> response) {
            Log.d(TAG, "onConfirmTransaction: transaction confirmation received !");
            TransactionInfo transactionInfo = response.getMessage();
            runOnUiThread(() -> {
                Snackbar.make(binding.getRoot(), "Transaction Completed Successfully !", BaseTransientBottomBar.LENGTH_LONG).show();
                binInfoFragment.setAnalysisResult(null);
                binInfoFragment.setRecordedWeight(null);
                binInfoFragment.setEstimateCredits(null);
                setUIReady("Bin is ready", "Open Bin");
                binding.activitySmartBinConnectButtonMain.setOnClickListener(v->{
                    setUIProcessing();
                    webSocketClient.sendOpenBinLid();
                });
                Context context = new ContextThemeWrapper(SmartBinConnectActivity.this, R.style.Theme_MoneyPlantNoActionBar);
                new MaterialAlertDialogBuilder(SmartBinConnectActivity.this)
                        .setTitle("Transaction successful")
                        .setMessage("Transaction uid: " + transactionInfo.getUid())
                        .setPositiveButton("Continue", (d, w) -> {})
                        .setNegativeButton("Exit", (d, w)-> gotoDashBoard())
                        .show();
            });
        }

        @Override
        public void onError(String message) {
            Log.d(TAG, "onError: Error communicating with server" + message);
            displayErrorSnackBar(message);
        }
    };

}