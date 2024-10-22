package com.example.footcard;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.SearchView;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static final String BEARER_TOKEN = "eyJhbGciOiJIUzM4NCJ9.eyJpYXQiOjE3Mjk2MDY2ODIsImV4cCI6MTcyOTYyMTA4Miwic3ViIjoiMyIsInJvbGUiOiJVU0VSIn0.MCpGYondYFnrHAH5JmtaehbOvt8t6JcQ3vDogBhLtta0V0qOrSyG9KoxOHUCuI7y";
    private RecyclerView recyclerView;
    private PlayerAdapter playerAdapter;
    private PlayerApi playerApi;
    private AuthApi authApi; // For sign-out API call

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        // Initialize PlayerAdapter with an empty list
        playerAdapter = new PlayerAdapter(new ArrayList<>());
        recyclerView.setAdapter(playerAdapter);

        // Initialize SearchView
        SearchView searchView = findViewById(R.id.searchView);

        // Create OkHttpClient with AuthInterceptor for Bearer token
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(BEARER_TOKEN))
                .build();

        // Build Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        // Create PlayerApi
        playerApi = retrofit.create(PlayerApi.class);

        // Create AuthApi for logout
        authApi = retrofit.create(AuthApi.class);

        // Handle initial load of players (for the user by default)
        loadPlayers(3, 0); // Start with page 0 for user with ID 3

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            playerAdapter.setPlayers(new ArrayList<>()); // Clear the list before loading new data
            switch (item.getItemId()) {
                case R.id.nav_all_players:
                    loadAllPlayers(0); // Load all players starting from page 0
                    return true;

                case R.id.nav_user_players:
                    loadPlayers(3, 0); // Load players for the current user (user ID 3)
                    return true;

                case R.id.nav_logout:
                    signOut(); // Handle user logout
                    return true;
            }
            return false;
        });

        // Setup SearchView listeners
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchPlayersForUser(3, query, 0); // Reset search with page 0 for user 3
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadPlayers(3, 0); // Load default players if the search is cleared
                } else {
                    searchPlayersForUser(3, newText, 0); // Reset search with page 0 for user 3
                }
                return false;
            }
        });
    }

    // Load all players from the general endpoint with pagination
    private void loadAllPlayers(int page) {
        Call<PlayerResponse> call = playerApi.getAllPlayers(page, 20);
        call.enqueue(new Callback<PlayerResponse>() {
            @Override
            public void onResponse(Call<PlayerResponse> call, Response<PlayerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Player> players = response.body().getContent();
                    if (page == 0) {
                        playerAdapter.setPlayers(players); // Reset adapter with all players
                    } else {
                        playerAdapter.addPlayers(players); // Add more players to the existing list
                    }

                    // Check if there are more pages to load
                    if (page < response.body().getPage().getTotalPages() - 1) {
                        // Load the next page recursively
                        loadAllPlayers(page + 1);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Failed to retrieve players", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PlayerResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error fetching players", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Load players for a specific user with pagination
    private void loadPlayers(int userId, int page) {
        Call<PlayerResponse> call = playerApi.getPlayers(userId, page, 20);
        call.enqueue(new Callback<PlayerResponse>() {
            @Override
            public void onResponse(Call<PlayerResponse> call, Response<PlayerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Player> players = response.body().getContent();
                    if (page == 0) {
                        playerAdapter.setPlayers(players); // Reset adapter with user players
                    } else {
                        playerAdapter.addPlayers(players); // Add more players to the existing list
                    }

                    // Check if there are more pages to load
                    if (page < response.body().getPage().getTotalPages() - 1) {
                        // Load the next page recursively
                        loadPlayers(userId, page + 1);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Failed to retrieve players", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PlayerResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error fetching players", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signOut() {
        Call<Void> call = authApi.signOut();
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Successfully logged out", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity and log out the user
                } else {
                    Toast.makeText(MainActivity.this, "Failed to log out", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error logging out", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchPlayersForUser(int userId, String searchTerm, int page) {
        Call<PlayerResponse> call = playerApi.searchPlayersForUser(userId, searchTerm, page);
        call.enqueue(new Callback<PlayerResponse>() {
            @Override
            public void onResponse(Call<PlayerResponse> call, Response<PlayerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Player> players = response.body().getContent();

                    if (page == 0) {
                        // Reset adapter with the new search results
                        playerAdapter.setPlayers(players);
                    } else {
                        // Add new players to the existing adapter list
                        playerAdapter.addPlayers(players);
                    }

                    // Check if there are more pages to load
                    if (page < response.body().getPage().getTotalPages() - 1) {
                        // Load the next page recursively
                        searchPlayersForUser(userId, searchTerm, page + 1);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "No players found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PlayerResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error searching players", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
