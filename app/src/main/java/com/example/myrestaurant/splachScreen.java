package com.example.myrestaurant;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.myrestaurant.retrofit.RestaurantApi;
import com.example.myrestaurant.retrofit.RetrofitCleint;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import dmax.dialog.SpotsDialog;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class splachScreen extends AppCompatActivity {
    private static final String TAG = "splash screen";
    private FirebaseAuth auth;
    private RestaurantApi restaurantApi;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private AlertDialog dialog;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splach_screen);
        auth = FirebaseAuth.getInstance();

        init();

        Dexter.withContext(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            dialog.show();
                            compositeDisposable.add(restaurantApi.getUser(common.API_KEY, user.getUid())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(userModel -> {
                                                if (userModel.isSuccess()) {
                                                    common.currentUser = userModel.getResult().get(0);
                                                    startActivity(new Intent(splachScreen.this, HomeActivity.class));
                                                    finish();
                                                } else {
                                                    startActivity(new Intent(splachScreen.this, MainActivity.class));
                                                    finish();
                                                }
                                                dialog.dismiss();
                                            },
                                            throwable -> {
                                                Log.e(TAG, "onPermissionGranted: ",throwable );
                                                Toast.makeText(splachScreen.this, "[GET USER API]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            }));
                        } else {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(splachScreen.this, MainActivity.class));
                                    finish();
                                }
                            }, 2000);
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(splachScreen.this, "you must give the app this permission to be able to use it", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();


    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        restaurantApi = RetrofitCleint.getInstance(common.API_RESTAURANT_ENDPOINT).create(RestaurantApi.class);
    }
}