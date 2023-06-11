package com.christo.moneyplant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;

import com.christo.moneyplant.R;
import com.christo.moneyplant.activities.AuthFragments.LoginFragment;
import com.christo.moneyplant.activities.AuthFragments.RegisterFragment;
import com.christo.moneyplant.helpers.CallBack;
import com.christo.moneyplant.models.api.ResponseModel;
import com.christo.moneyplant.models.user.User;
import com.christo.moneyplant.services.ApiBuilder;
import com.christo.moneyplant.services.AuthenticationToken;
import com.google.android.material.snackbar.Snackbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LandingActivity extends AppCompatActivity {

    private View layout;
    private FragmentContainerView fragmentContainer;
    private ProgressBar spinner;
    private FragmentManager fragmentManager;
    public final LoginFragment loginFragment = new LoginFragment();
    public final RegisterFragment registerFragment = new RegisterFragment();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        layout = findViewById(R.id.layout_splash_screen);

        fragmentContainer = (FragmentContainerView) findViewById(R.id.fragmentContainerView);
        spinner = (ProgressBar) findViewById(R.id.progressBar);
        fragmentManager = getSupportFragmentManager();
        spinner.setVisibility(View.VISIBLE);
        fragmentContainer.setVisibility(View.INVISIBLE);

        testConnection(this::showFragment);

    }

    private void showFragment() {
//        fragmentManager.beginTransaction()
//                .setReorderingAllowed(true)
//                .replace(R.id.fragmentContainerView, loginFragment, null)
//                .commit();
        fragmentContainer.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.INVISIBLE);

    }

    private void testConnection (CallBack callBack) {
        Log.i("API", "testConnection: Testing connection to API");
        ApiBuilder.getService().checkConnection()
                .enqueue(new Callback<ResponseModel>() {
                    @Override
                    public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                        Snackbar.make(layout, "Connection to API successful", Snackbar.LENGTH_SHORT).show();
                        Log.d("API", "Request successful");
                        checkTokenValid();
                        callBack.run();
                    }

                    @Override
                    public void onFailure(Call<ResponseModel> call, Throwable t) {
                        Snackbar snackbar = Snackbar.make(layout, "Network Error, Please check you connection", Snackbar.LENGTH_INDEFINITE);
                        Log.d("API", t.toString());
                        snackbar.setAction("RETRY", view -> testConnection(callBack));
                        snackbar.show();
                    }
                });
    }

    private void checkTokenValid () {
        if (! AuthenticationToken.isKeyAvailable(this)) return;
        String token = AuthenticationToken.getAuthenticationToken(this);
        ApiBuilder.getService(token).getInfo().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                startActivity(new Intent(LandingActivity.this, MainActivity.class));
                Toast.makeText(LandingActivity.this, "Welcome back " + response.body().getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Snackbar.make(layout, "Session expired please login again !",Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
