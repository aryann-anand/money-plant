package com.christo.moneyplant.activities.UserFragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.christo.moneyplant.activities.LandingActivity;
import com.christo.moneyplant.activities.MainActivity;
import com.christo.moneyplant.databinding.FragmentDashboardBinding;
import com.christo.moneyplant.models.user.User;
import com.christo.moneyplant.services.AuthenticationToken;

import java.util.Objects;


public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private User user;

    public DashboardFragment() {
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDashboardBinding.inflate(inflater, container,false);
        binding.fragmetnDashboarrdButtonLogout.setOnClickListener(v -> logout());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (user != null) updateUserInfo(user);
    }



    private void updateUserInfo (User user) {
        String greeting = "Hi, " + user.getName() + "\uD83D\uDC4B";
        binding.fragmentDashboardTextViewUserName.setText(greeting);
        String credit_balance = user.getCredit_balance().toString() + "$";
        binding.fragmentDashboardTextViewCreditBalance.setText(credit_balance);
    }

    private void logout () {
        AuthenticationToken.clearToken(requireContext());
        startActivity(new Intent(getActivity(), LandingActivity.class));
    }

}