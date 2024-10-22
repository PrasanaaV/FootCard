package com.example.footcard;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PlayerApi {

    // Define the endpoint to get players of a specific user
    @GET("api/users/{userId}/players")
    Call<PlayerResponse> getPlayers(@Path("userId") int userId,
                                    @Query("page") int page,
                                    @Query("size") int size);
    // Add this method to perform a search
    @GET("api/players/search")
    Call<PlayerResponse> searchPlayers(@Query("term") String searchTerm,
                                       @Query("page") int page);

    @GET("api/players")
    Call<PlayerResponse> getAllPlayers(@Query("page") int page, @Query("size") int size);

    // Add this method to search players for a specific user with a search term
    @GET("api/users/{userId}/players/search")
    Call<PlayerResponse> searchPlayersForUser(@Path("userId") int userId,
                                              @Query("term") String searchTerm,
                                              @Query("page") int page);
}
