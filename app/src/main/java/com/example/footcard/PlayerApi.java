package com.example.footcard;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PlayerApi {

    // Define the endpoint to get players of a specific user
    @GET("api/users/{userId}/players")
    Call<PlayerResponse> getPlayers(@Path("userId") int userId);
}
