package com.christo.moneyplant.activities.UserFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.christo.moneyplant.R;
import com.christo.moneyplant.databinding.FragmentSmartBinInfoBinding;
import com.christo.moneyplant.databinding.FragmentTransactionBinding;
import com.christo.moneyplant.helpers.IconListAdapter;
import com.christo.moneyplant.helpers.SortedTransactionListAdapter;
import com.christo.moneyplant.helpers.TransactionListAdapter;
import com.christo.moneyplant.models.api.Transactions;
import com.christo.moneyplant.models.transaction.TransactionInfo;
import com.christo.moneyplant.services.ApiBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionFragment extends Fragment {

    private FragmentTransactionBinding binding;
    private SortedTransactionListAdapter listAdapter;
    private String TAG = "Transaction Fragment";

    public TransactionFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTransactionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listAdapter = new SortedTransactionListAdapter();
        binding.fragmentTransactionRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.fragmentTransactionRecyclerView.setAdapter(listAdapter);
        getTransactions();
    }

    private void getTransactions () {
        ApiBuilder.getService().getTransactions().enqueue(new Callback<Transactions>() {
            @Override
            public void onResponse(Call<Transactions> call, Response<Transactions> response) {
                if (response.body() == null) {
                    Snackbar.make(binding.getRoot(), "You've no transactions to display", Snackbar.LENGTH_LONG).show();
                    return;
                }

                for (TransactionInfo transaction : response.body().getTransactions()) {
                    Log.d(TAG, "onResponse:" + transaction.getBin_uid());
                    listAdapter.addItem(transaction);
                }
            }

            @Override
            public void onFailure(Call<Transactions> call, Throwable t) {
                Snackbar.make(binding.getRoot(), "Couldn't fetch transactions: "+t.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }
}