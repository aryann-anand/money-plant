package com.christo.moneyplant.activities.AuthFragments;

import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
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
import com.christo.moneyplant.activities.LandingActivity;
import com.christo.moneyplant.helpers.AadhaarXMLParser;
import com.christo.moneyplant.helpers.TextInputForm;
import com.christo.moneyplant.models.api.RegisterForm;
import com.christo.moneyplant.models.user.User;
import com.christo.moneyplant.models.user.UserBase;
import com.christo.moneyplant.services.ApiBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.IOException;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {
    private static final String TAG = "Register Fragment";
    public static final String AADHAAR = "aadhaar";
    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    View view;
    View layout;
    FragmentManager fragmentManager;
    Button button_login;
    Button button_register;
    Button button_qrReader;
    TextInputForm registerForm;

    String name;
    String aadhaarID;


    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                Log.d(TAG, "result: "+result.getContents());
                Log.d(TAG, result.toString());
                if(result.getContents() != null) {
                    try {
                        AadhaarXMLParser aadhaarXMLParser = new AadhaarXMLParser(result.getContents());
                        registerForm.getInput(AADHAAR).getEditText().setText(aadhaarXMLParser.getAttribute("uid"));
                        registerForm.getInput(NAME).getEditText().setText(aadhaarXMLParser.getAttribute("name"));
                        Snackbar.make(layout, "Successfully read Aadhaar Card !", Snackbar.LENGTH_LONG).show();
//                        fragmentManager.beginTransaction().replace(R.id.fragmentContainerView, this).commit();
                    }catch (Exception e){
                        Snackbar.make(layout, "Failed to parse data !", Snackbar.LENGTH_LONG).show();
                        Log.e("Aadhaar Card reader", e.toString());
                    }
                }
    });

    public RegisterFragment() {
        // Required empty public constructor
    }
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (this.getArguments() != null) {
//            name = this.getArguments().getString(NAME);
//            aadhaarID = this.getArguments().getString(AADHAAR);
//        }
//    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layout = view.findViewById(R.id.fragment_register_layout);
        button_login = view.findViewById(R.id.fragment_register_button_login);
        button_register = view.findViewById(R.id.fragment_register_button_register);
        button_qrReader = view.findViewById(R.id.fragment_register_button_qrScanner);

        registerForm.addInput(AADHAAR, view.findViewById(R.id.fragment_register_textInputLayout_aadhaarNo));
        registerForm.addInput(NAME, view.findViewById(R.id.fragment_register_textInputLayout_name));
        registerForm.addInput(EMAIL, view.findViewById(R.id.fragment_register_textInputLayout_email));
        registerForm.addInput(PASSWORD, view.findViewById(R.id.fragment_register_textInputLayout_password));


        button_login.setOnClickListener(view1 -> setLoginFragment(null));
        button_register.setOnClickListener(view1 -> register());
        button_qrReader.setOnClickListener(view1 -> scanAadhaar());
//
//        if (aadhaarID != null) registerForm.getInput(AADHAAR).getEditText().setText(aadhaarID);
//        if (name != null) registerForm.getInput(NAME).getEditText().setText(name);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_register, container, false);
        fragmentManager = getParentFragmentManager();
        registerForm = new TextInputForm();
        setValidators();
        return view;

    }

    private void setValidators () {
        registerForm.addValidator(
                AADHAAR,
                (text) -> {if(!text.matches("^[0-9]{12}$"))throw new Error(getString(R.string.aadhaar_invalid));
                });
        registerForm.addValidator(
                EMAIL,
                (text) -> {if(! text.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,20}$"))throw new Error(getString(R.string.email_invalid));
                });
        registerForm.addValidator(
                PASSWORD,
                (text) -> {if(! text.matches("^^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^*&+=()])(?=\\S+$).{8,20}$"))throw new Error(getString(R.string.password_invalid));
                });

    }

    private void register () {
        Log.i("Register Fragment", "Attempting to register");

        if (! registerForm.validateForm()) return;
        HashMap<String, String> data = registerForm.getFormData();
        RegisterForm user = new RegisterForm(data.get(AADHAAR), data.get(EMAIL), data.get(NAME), data.get(PASSWORD));
        enqueueRegisterRequest(user);
    }

    private void enqueueRegisterRequest (RegisterForm user) {
        ApiBuilder.getService()
                .register(user)
                .enqueue(new Callback<UserBase>()
                {
                    @Override
                    public void onResponse(Call<UserBase> call, Response<UserBase> response) {
                        if (response.code() == 200 && response.body() != null) {
                            Snackbar.make(layout, "Registration is  successful ! ", Snackbar.LENGTH_LONG).show();
                            setLoginFragment(response.body().getEmail());
                        } else {
                            assert response.errorBody() != null;
                            try {
                                Snackbar
                                        .make(layout, "Registration failed : "+response.errorBody().string(), Snackbar.LENGTH_LONG)
                                        .show();
                                registerForm.getInput(PASSWORD).getEditText().setText(null);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                    @Override
                    public void onFailure(Call<UserBase> call, Throwable t) {
                        Snackbar
                                .make(layout, "Network Error !", Snackbar.LENGTH_LONG)
                                .setAction("Retry", (view) -> enqueueRegisterRequest(user))
                                .show();
                    }
                });
    }


    private void setLoginFragment (String email) {
        LoginFragment loginFragment = ((LandingActivity) getActivity()).loginFragment;
        Bundle bundle = new Bundle();
        bundle.putString(LoginFragment.EMAIL, email);
        fragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragmentContainerView, loginFragment)
                .commit();
    }


    private void scanAadhaar () {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Scan the Aadhaar Card");
        options.setCameraId(0);  // Use a specific camera of the device
        options.setOrientationLocked(false);
        options.setBeepEnabled(false);
        options.setBarcodeImageEnabled(true);
        barcodeLauncher.launch(options);
//        AadhaarScannerFragment fragment = AadhaarScannerFragment.newInstance("QR_CODE", result -> {
//            fragmentManager.beginTransaction()
//                    .replace(R.id.fragmentContainerView, this)
//                    .commit();
//            if(result.getText() != null) {
//                Log.d(TAG, "scanAadhaar: Text : "+result.getText());
//                    try {
//                        AadhaarXMLParser aadhaarXMLParser = new AadhaarXMLParser(result.getText());
//                        registerForm.getInput(AADHAAR).getEditText().setText(aadhaarXMLParser.getAttribute("uid"));
//                        registerForm.getInput(NAME).getEditText().setText(aadhaarXMLParser.getAttribute("name"));
//                        Snackbar.make(layout, "Successfully read Aadhaar Card !", Snackbar.LENGTH_LONG).show();
//                    }catch (Exception e){
//                        Snackbar.make(layout, "Failed to parse data !", Snackbar.LENGTH_LONG).show();
//                        Log.e("Aadhaar Card reader", e.toString());
//                    }
//            }
//            });
//
//            fragmentManager.beginTransaction()
//                    .replace(R.id.fragmentContainerView, fragment)
//                    .commit();
    }

    // Launch
    public void onButtonClick(View view) {
        barcodeLauncher.launch(new ScanOptions());
    }
}