package com.christo.moneyplant.activities.AuthFragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.christo.moneyplant.R;
import com.christo.moneyplant.activities.MainActivity;
import com.christo.moneyplant.helpers.TextInputForm;
import com.christo.moneyplant.models.api.Token;
import com.christo.moneyplant.services.ApiBuilder;
import com.christo.moneyplant.services.AuthenticationToken;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginFragment extends Fragment {
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    View view;
    View layout;
    FragmentManager fragmentManager;
    Button button_login;
    Button button_register;
    TextInputForm loginForm;

    public LoginFragment() {
        // Required empty public constructor
    }

     @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_login, container, false);
        fragmentManager = getParentFragmentManager();
        loginForm = new TextInputForm();
        setValidators();
        return  view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        layout = view.findViewById(R.id.fragment_login_layout);

        button_login = view.findViewById(R.id.fragment_login_button_login);
        button_register = view.findViewById(R.id.fragment_login_button_register);
        loginForm.addInput(EMAIL, view.findViewById(R.id.fragment_login_textInputLayout_email));
        loginForm.addInput(PASSWORD, view.findViewById(R.id.fragment_login_textInputLayout_password));
        if (getArguments() != null){
            String init_email = getArguments().getString(EMAIL);
            loginForm.getInput(EMAIL).getEditText().setText(init_email);
        }

        button_register.setOnClickListener(view1 -> setRegisterFragment());
        button_login.setOnClickListener(view1 -> login());


    }

    private void setValidators () {
        loginForm.addValidator(
                EMAIL,
                (text) -> {if(! text.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,20}$"))throw new Error(getString(R.string.email_invalid));
                });
        loginForm.addValidator(
                PASSWORD,
                (text) -> {if(! text.matches("^^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^*&+=()])(?=\\S+$).{8,20}$"))throw new Error(getString(R.string.password_invalid));
                });

    }

    private void login () {
        Log.i("Login Fragment", "Attempting to login");
        if (! loginForm.validateForm()) return;
        HashMap<String, String> data = loginForm.getFormData();


        ApiBuilder.getService()
                .login(data.get(EMAIL), data.get(PASSWORD)).enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                if (response.code() == 200 && response.body() != null) {
                    Snackbar.make(layout, "Auth successful !" + response.body().getAccess_token(), Snackbar.LENGTH_LONG).show();
                    AuthenticationToken.saveToken(requireActivity(), response.body().getAccess_token());
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                }
                else {
                    Snackbar.make(layout, "Auth failed", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                Snackbar.make(layout, "Network Error", Snackbar.LENGTH_LONG).show();
            }
        });
            ;
    }


    private void setRegisterFragment() {
        fragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragmentContainerView, RegisterFragment.class, null)
                .commit();
    }
}