package com.example.footcard;

import retrofit2.Call;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("api/signout")
    Call<Void> signOut();
}