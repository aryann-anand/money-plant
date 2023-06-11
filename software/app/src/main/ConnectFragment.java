package com.christo.moneyplant.activities.UserFragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.christo.moneyplant.R;
import com.christo.moneyplant.activities.SmartBinConnectActivity;
import com.christo.moneyplant.databinding.FragmentConnectBinding;
import com.christo.moneyplant.models.bin.BinConnectQR;
import com.christo.moneyplant.models.user.User;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.zxing.Result;

import java.util.Map;
import java.util.function.Consumer;


public class ConnectFragment extends Fragment {

    private static final String TAG = "ConnectFragment";
    private CodeScanner mCodeScanner;
    private FragmentConnectBinding binding;
    private User user;
    private Context context;
    private final ActivityResultLauncher<String[]> requestPermission = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                permissions.entrySet().forEach(stringBooleanEntry -> {
                    if (!stringBooleanEntry.getValue()) {
                        Snackbar.make(binding.getRoot(), "Couldn't obtain enough permissions", Snackbar.LENGTH_LONG).show();
                    }
                });
            });



    public ConnectFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.getArguments() != null) {
            user = (User) this.getArguments().getSerializable("USER");
            assert user != null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentConnectBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = requireContext();

        binding.button.setOnClickListener(v -> onStartScanner());

    }

    private void onStartScanner () {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            requestPermission.launch(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.CHANGE_NETWORK_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.INTERNET}
            );
        else  {
            CodeScannerView scannerView = binding.scannerView;
            mCodeScanner = new CodeScanner(requireContext(), scannerView);
            mCodeScanner.setDecodeCallback(this::decodeCallback);
            binding.button.setVisibility(View.INVISIBLE);
            binding.frameLayout.setVisibility(View.VISIBLE);
            binding.textView.setText(R.string.scanner_fragment_point_to_qr_code_prompt);
            mCodeScanner.startPreview();
        }
    }

    private void decodeCallback (Result result) {
        requireActivity().runOnUiThread(() -> {
            binding.textView.setText(result.getText());
            mCodeScanner.releaseResources();
            Gson gson = new Gson();
            try {
                BinConnectQR binConnectQR = gson.fromJson(result.getText(), BinConnectQR.class);
                Snackbar.make(binding.getRoot(), "Connecting to the bin, hang on !", Snackbar.LENGTH_LONG).show();
                Log.d("Connect Fragment", String.format("Parsed QR code successfully %s %s", binConnectQR.SSID, binConnectQR.WPA));
                connect(binConnectQR.SSID, binConnectQR.WPA);
            } catch (JsonSyntaxException e) {
                Snackbar.make(binding.getRoot(), "Cannot Parse QR CODE !", Snackbar.LENGTH_LONG).show();
                Log.e("Connect Fragment", "Failed to parse QR Code");
            }

        });
    }

    
    public void connect(String ssid, String password) {
        Log.d(TAG, "connect: is user null ?" +  user.getEmail());
        Intent intent = new Intent(getActivity(), SmartBinConnectActivity.class);
        intent.putExtra("SSID", ssid);
        intent.putExtra("PSK", password);
        intent.putExtra("USER", user);
        startActivity(intent);
    }

    private void connectToBin_ (String ssid, String pass) {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = String.format("\"%s\"", ssid);
        conf.preSharedKey = String.format("\"%s\"", pass);
        conf.status = WifiConfiguration.Status.ENABLED;
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int netId =  wifiManager.addNetwork(conf);
        Log.e("connect fragment", String.format("connectToBin: the netid is : %d", netId) );
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();

    }

    @Override
    public void onPause() {
        super.onPause();
        binding.button.setVisibility(View.VISIBLE);
        binding.frameLayout.setVisibility(View.INVISIBLE);
        binding.textView.setText(R.string.scanner_fragment_header);
        if (mCodeScanner != null) mCodeScanner.releaseResources();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCodeScanner != null) mCodeScanner.startPreview();
    }
}