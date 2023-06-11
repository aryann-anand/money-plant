package com.christo.moneyplant.activities.UserFragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.christo.moneyplant.R;
import com.christo.moneyplant.databinding.FragmentLocateBinding;
import com.christo.moneyplant.models.bin.Bin;
import com.christo.moneyplant.models.bin.Bins;
import com.christo.moneyplant.services.ApiBuilder;
import com.christo.moneyplant.services.AuthenticationToken;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LocateFragment extends Fragment implements OnMapReadyCallback {

    private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private static final LatLngBounds IndiaBounds = new LatLngBounds(new LatLng(8, 68), new LatLng(37, 98));
    private static final String TAG = "LocateFragment";
    private FragmentLocateBinding binding;
    private final ActivityResultLauncher<String> requestPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (!isGranted)
                    Snackbar.make(binding.getRoot(), "Cannot locate you, please allow location permissions", Snackbar.LENGTH_LONG).show();
                else setMapToCurrentLocation();
            });

    public LocateFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLocateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        supportMapFragment.getMapAsync(this);
        getBinDetails();
        binding.floatingActionButton.setOnClickListener(v -> setMapToCurrentLocation());

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(IndiaBounds.getCenter(), 5));
    }

    @SuppressLint("MissingPermission")
    private void setMapToCurrentLocation() {
        Snackbar snackbar = Snackbar.make(binding.getRoot(), "Locating you hold on ...", Snackbar.LENGTH_INDEFINITE);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            snackbar.show();
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            String provider = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S ? LocationManager.FUSED_PROVIDER : LocationManager.NETWORK_PROVIDER ;
            locationManager.requestLocationUpdates(provider, 0, 0, location -> {
                if (getActivity() == null) return;
                getMapFragment().getMapAsync(googleMap -> {
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(location.getLatitude(), location.getLongitude()))
                            .zoom(10)
                            .bearing(10)
                            .build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                });
                snackbar.dismiss();
            });

        } else requestPermission.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    SupportMapFragment getMapFragment() {
        return (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
    }

    void getBinDetails() {
        ApiBuilder.getService(AuthenticationToken.getAuthenticationToken(getActivity()))
                .getBinInfo()
                .enqueue(new Callback<Bins>() {
                    @Override
                    public void onResponse(Call<Bins> call, Response<Bins> response) {
                        if (getActivity() == null) return;
                        getMapFragment().getMapAsync(googleMap -> {
                            if (response.body() == null) return;
                            for (Bin bin : response.body().getBins()) {
//                                Log.d("locate fragment", "adding bin : "+bin.getAddress());
                                googleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(bin.getLatitude(), bin.getLongitude()))
                                        .title(bin.getAddress())
                                );
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<Bins> call, Throwable t) {
                        Snackbar.make(binding.getRoot(), "Error while getting bins, " + call, Snackbar.LENGTH_LONG).show();
                    }

                });
    }


}