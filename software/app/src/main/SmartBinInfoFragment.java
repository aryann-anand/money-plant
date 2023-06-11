package com.christo.moneyplant.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.christo.moneyplant.R;
import com.christo.moneyplant.databinding.FragmentSmartBinInfoBinding;
import com.christo.moneyplant.helpers.IconListAdapter;
import com.christo.moneyplant.models.ui.IconListItem;

import java.util.ArrayList;
import java.util.Objects;

public class SmartBinInfoFragment extends Fragment {

    private static final String TAG = "Smart Bin Info Fragment";

    private static final String CONNECTION_STATE = "CONNECTION_STATE";
    private static final String BIN_UID = "BIN_UID";
    private static final String BIN_LID_STATE = "BIN_LID_STATE";
    private static final String ANALYSIS_RESULT = "ANALYSIS";
    private static final String RECORDED_WEIGHT = "RECORDED_WEIGHT";
    private static final String ESTIMATE_CREDITS = "ESTIMATE_CREDITS";

    private static String[] ConnectionStates = { "Disconnected", "Connected to Bin", "Connected and Authenticated"};

    private static String[] BinLidStates = {"Closed", "Open"};

    private String[] AnalysisResults = {"Failed verification", "Verified"};

    private IconListAdapter listAdapter;
    private FragmentSmartBinInfoBinding binding;
    private final ArrayList<IconListItem> items = new ArrayList<>();

    public SmartBinInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSmartBinInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //        Set adapter for bin status
        listAdapter = new IconListAdapter(items);
        binding.fragmentSmartBinInfoRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.fragmentSmartBinInfoRecyclerView.setAdapter(listAdapter);
    }

    private void updateInfo (String tag, IconListItem item) {
        int index =  items.stream()
                .filter(i -> Objects.equals(i.getTag(), tag))
                .findFirst()
                .map(items::indexOf).orElse(-1);
        if (Objects.isNull(item) && index == -1) return;
        else if (Objects.isNull(item)) listAdapter.remove(index);
        else if (index == -1) listAdapter.addItemToBottom(item);
        else listAdapter.updateItem(index, item);
    }

    public void setConnectionState (int state) {
        IconListItem item = Objects.isNull(state) ? null : new IconListItem(
                CONNECTION_STATE,
                getString(R.string.status_connection_to_bin) + ConnectionStates[state],
                R.drawable.ic_baseline_settings_ethernet_24,
                R.color.purple_200
        );
        updateInfo(CONNECTION_STATE, item);
    }

    public void setUid (String uid) {
        IconListItem item = Objects.isNull(uid) ? null :new IconListItem(
                BIN_UID,
                getString(R.string.status_bin_uid) + uid,
                R.drawable.ic_baseline_perm_identity_24,
                R.color.purple_500
        );
        updateInfo(BIN_UID, item);
    }

    public void setBinLidState (int state) {
        IconListItem item = Objects.isNull(state) ? null : new IconListItem(
                BIN_LID_STATE,
                getString(R.string.bin_lid_state) + BinLidStates[state],
                R.drawable.ic_baseline_delete_24,
                R.color.purple_200
        );
        updateInfo(BIN_LID_STATE, item);
    }

    public void setAnalysisResult (Integer result) {
        IconListItem item = Objects.isNull(result) ? null : new IconListItem(
                ANALYSIS_RESULT,
                getString(R.string.waste_analysis_report) + AnalysisResults[result],
                R.drawable.ic_baseline_remove_red_eye_24,
                R.color.purple_200
        );
        updateInfo(ANALYSIS_RESULT, item);
    }

    public void setRecordedWeight (Float result) {
        IconListItem item = Objects.isNull(result) ? null :new IconListItem(
                RECORDED_WEIGHT,
                getString(R.string.weight_state) + result*1000 + " gm",
                R.drawable.ic_baseline_monitor_weight_24,
                R.color.purple_200
        );
        updateInfo(RECORDED_WEIGHT, item);
    }

    public void setEstimateCredits (Float result) {
        IconListItem item = Objects.isNull(result) ? null : new IconListItem(
                ESTIMATE_CREDITS,
                getString(R.string.estimate_credits) + "₹" + result ,
                R.drawable.ic_baseline_attach_money_24,
                R.color.purple_500
        );
        updateInfo(ESTIMATE_CREDITS, item);
    }

}