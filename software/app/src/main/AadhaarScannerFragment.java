package com.christo.moneyplant.activities.AuthFragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.christo.moneyplant.R;

import java.util.concurrent.Callable;

public class AadhaarScannerFragment extends Fragment {
    public static final String RESULT_STRING = "QR_CODE_SCANNER_TEXT";
    private String requestKey = "QR_CODE_SCANNER";
    private DecodeCallback callback;
    private CodeScanner mCodeScanner;

    public static AadhaarScannerFragment newInstance (String requestKey, DecodeCallback onScan) {
        AadhaarScannerFragment fragment = new AadhaarScannerFragment();
        fragment.requestKey = requestKey;
        fragment.callback = onScan;
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final Activity activity = getActivity();
        View root = inflater.inflate(R.layout.fragment_aadhaar_scanner, container, false);
        CodeScannerView scannerView = root.findViewById(R.id.scanner_view);
        assert activity != null;
        mCodeScanner = new CodeScanner(activity, scannerView);
        mCodeScanner.setDecodeCallback(callback);
        scannerView.setOnClickListener(view -> mCodeScanner.startPreview());
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    public void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }
}