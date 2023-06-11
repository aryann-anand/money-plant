package com.christo.moneyplant.services;

import com.christo.moneyplant.models.api.Transactions;
import com.christo.moneyplant.models.bin.Bins;
import com.christo.moneyplant.models.api.RegisterForm;
import com.christo.moneyplant.models.api.ResponseModel;
import com.christo.moneyplant.models.api.Token;
import com.christo.moneyplant.models.transaction.TransactionInfo;
import com.christo.moneyplant.models.user.User;
import com.christo.moneyplant.models.user.UserBase;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @GET("test")
    Call<ResponseModel> checkConnection ();


    @POST("auth/register/user")
    Call<UserBase> register (@Body RegisterForm user);

    @FormUrlEncoded
    @POST("auth/token")
    Call<Token> login (@Field("username") String email,
                       @Field("password") String password);

//    Paths that require authentication to access
    @GET("user/info")
    Call<User> getInfo ();

    @GET("user/bin/info")
    Call<Bins> getBinInfo();

    @GET("user/transaction/all")
    Call<Transactions> getTransactions();

}
