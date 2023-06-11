package com.christo.moneyplant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.christo.moneyplant.R;
import com.christo.moneyplant.activities.UserFragments.ConnectFragment;
import com.christo.moneyplant.activities.UserFragments.DashboardFragment;
import com.christo.moneyplant.activities.UserFragments.LocateFragment;
import com.christo.moneyplant.activities.UserFragments.ReportFragment;
import com.christo.moneyplant.activities.UserFragments.TransactionFragment;
import com.christo.moneyplant.databinding.ActivityMainBinding;
import com.christo.moneyplant.models.user.User;
import com.christo.moneyplant.services.ApiBuilder;
import com.christo.moneyplant.services.AuthenticationToken;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main Activity";
    ActivityMainBinding binding;
    DashboardFragment dashboardFragment = new DashboardFragment();
    LocateFragment locateFragment = new LocateFragment();
    ReportFragment reportFragment = new ReportFragment();
    ConnectFragment connectFragment = new ConnectFragment();
    TransactionFragment transactionFragment = new TransactionFragment();

    User user = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!AuthenticationToken.isKeyAvailable(this)) {
            Log.d("Main Activity", "Token key not retrieved !");
            Snackbar.make(binding.getRoot(), "The authentication token has expired !", Snackbar.LENGTH_LONG).show();
            goToLogin();
        }

        getUserInfo(AuthenticationToken.getAuthenticationToken(this));
        BottomNavigationView navView = binding.activityMainBottomNavView;

        navView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.dashboardFragment:
                    setCurrentFragment(dashboardFragment);
                    break;
                case R.id.locateFragment:
                    setCurrentFragment(locateFragment);
                    break;
                case R.id.transactionFragment:
                    setCurrentFragment(transactionFragment);
                    break;
                case R.id.connectFragment:
                    setCurrentFragment(connectFragment);
                    break;
            };
            return true;
        });

    }


    private void getUserInfo (String authToken) {
        ApiBuilder.getService(authToken)
                .getInfo()
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        Log.i("Main Activity", "User Info connection successful");
                        user = response.body();
                        binding.progressBar2.setVisibility(View.GONE);
                        setCurrentFragment(dashboardFragment);
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Snackbar.make(binding.getRoot(),t.toString()    ,Snackbar.LENGTH_LONG).show();
                        goToLogin();
                    }
                });
    }

    private void setCurrentFragment (Fragment fragment) {
        Log.i(TAG, "setCurrentFragment: called");
        Bundle bundle = new Bundle();
        bundle.putSerializable("USER", user);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_fragment_container_view, fragment).commitAllowingStateLoss();
    }

    private void goToLogin () {
        Intent intent = new Intent(this, LandingActivity.class);
        startActivity(intent);
    }

}