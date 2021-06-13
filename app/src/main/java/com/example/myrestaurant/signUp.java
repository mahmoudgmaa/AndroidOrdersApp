package com.example.myrestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.myrestaurant.retrofit.RestaurantApi;
import com.example.myrestaurant.retrofit.RetrofitCleint;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class signUp extends AppCompatActivity {
    private MaterialEditText mEmail, mPassword, mPhone, mName, mAddress;
    private Button signUp;
    private boolean isEmailOk = false;
    private boolean isPasswordOk = false;
    private boolean isPhone = false;
    private boolean isNameOk = false;
    private boolean isAddressOk = false;
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
        setContentView(R.layout.sign_up);
        mEmail = findViewById(R.id.email_edit_text);
        mPassword = findViewById(R.id.password_edit_text);
        mPhone = findViewById(R.id.phone_edit_text);
        signUp = findViewById(R.id.signUp);
        mName = findViewById(R.id.name_edit_text);
        mAddress = findViewById(R.id.address_edit_text);
        auth = FirebaseAuth.getInstance();
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        restaurantApi = RetrofitCleint.getInstance(common.API_RESTAURANT_ENDPOINT).create(RestaurantApi.class);


        mName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String name = mName.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    mName.setError("Name field cannot be empty");
                } else {
                    isNameOk = true;
                }
            }
        });

        mAddress.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String address = mAddress.getText().toString();
                if (TextUtils.isEmpty(address)) {
                    mAddress.setError("Address field cannot be empty");
                } else {
                    isAddressOk = true;
                }
            }
        });

        mEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String email = mEmail.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email field cannot be empty");
                } else {
                    isEmailOk = true;
                }
            }
        });

        mPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String password = mPassword.getText().toString();
                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("password field cannot be empty");
                } else if (password.length() < 6) {
                    mPassword.setError("password must be at least 6 characters");
                } else {
                    isPasswordOk = true;
                }
            }
        });

        mPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String phone = mPhone.getText().toString();
                if (!hasFocus) {
                    if (TextUtils.isEmpty(phone)) {
                        mPhone.setError("phone field cannot be empty");
                    } else if (phone.length() <= 11) {
                        mPhone.setError("phone must be at least 11 characters");
                    } else {
                        isPhone = true;
                    }
                }
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isEmailOk || !isPasswordOk || !isPhone || !isNameOk || !isAddressOk) {
                    Toast.makeText(com.example.myrestaurant.signUp.this, "please check your inputs", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    dialog.show();
                    String email = mEmail.getText().toString();
                    String password = mPassword.getText().toString();
                    String phone = mPhone.getText().toString();
                    String name = mName.getText().toString();
                    String address = mAddress.getText().toString();
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener((OnCompleteListener<AuthResult>) task -> {
                        if (task.isSuccessful()) {
                            SharedPreferences.Editor preferences = getSharedPreferences("user", MODE_PRIVATE).edit();
                            FirebaseAuth auth = FirebaseAuth.getInstance();
                            FirebaseUser user = auth.getCurrentUser();
                            compositeDisposable.add(restaurantApi.updateUserInfo(common.API_KEY, phone, name, address, user.getUid())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(updateUserModel -> {
                                                if (updateUserModel.isSuccess()) {
                                                    preferences.putString("email", email);
                                                    preferences.putString("phone", phone);
                                                    preferences.putString("name", name);
                                                    preferences.putString("address", address);
                                                    preferences.apply();

                                                    compositeDisposable.add(restaurantApi.getUser(common.API_KEY, user.getUid())
                                                            .subscribeOn(Schedulers.io())
                                                            .observeOn(AndroidSchedulers.mainThread())
                                                            .subscribe(userModel -> {
                                                                if(userModel.isSuccess()) {
                                                                    common.currentUser = userModel.getResult().get(0);
                                                                    startActivity(new Intent(com.example.myrestaurant.signUp.this, HomeActivity.class));
                                                                    finish();
                                                                }else {
                                                                    Toast.makeText(com.example.myrestaurant.signUp.this,"[GET USER RESULT]"+userModel.getMessage(),Toast.LENGTH_SHORT).show();

                                                                }
                                                                dialog.dismiss();
                                                                    },
                                                                    throwable -> {
                                                                dialog.dismiss();
                                                                Toast.makeText(com.example.myrestaurant.signUp.this,"[GET USER]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                                                    }));
                                                } else {
                                                    dialog.dismiss();
                                                    Toast.makeText(com.example.myrestaurant.signUp.this, "[UPDATE USER API RETURN]" + updateUserModel.getMessage(), Toast.LENGTH_SHORT).show();

                                                }
                                                dialog.dismiss();
                                            },
                                            throwable -> {
                                                dialog.dismiss();
                                                Toast.makeText(com.example.myrestaurant.signUp.this, "[UPDATE USER API]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                            }));
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(com.example.myrestaurant.signUp.this, "signed up successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            dialog.dismiss();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(com.example.myrestaurant.signUp.this, "failed to sign up" + task.getException(), Toast.LENGTH_SHORT).show();

                        }
                    });

                }
            }
        });
    }

}