package com.example.footcard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.List;

import androidx.appcompat.widget.SearchView;

import com.example.footcard.PlayerAdapter;
import com.example.footcard.PlayerApi;
import com.example.footcard.AuthApi;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static final String BEARER_TOKEN = "eyJhbGciOiJIUzM4NCJ9.eyJpYXQiOjE3Mjk1OTA1NDksImV4cCI6MTcyOTYwNDk0OSwic3ViIjoiMyIsInJvbGUiOiJVU0VSIn0.bFUBAM-Bg8hvOmEZKy2c2FuUB15n31y7zdji0bUcour4BlJSRco5QsOxST2odZBL";
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

        // Initialize SearchView
        SearchView searchView = findViewById(R.id.searchView);

        // Initialize Logout Button
        ImageView logoutButton = findViewById(R.id.logoutButton);

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

        // Handle initial load of players (if needed)
        loadPlayers(3);

        // Set up search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchPlayers(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadPlayers(3); // Load default players if the search is cleared
                } else {
                    searchPlayers(newText);
                }
                return false;
            }
        });

        // Handle Logout Click
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    private void loadPlayers(int userId) {
        Call<PlayerResponse> call = playerApi.getPlayers(userId);
        call.enqueue(new Callback<PlayerResponse>() {
            @Override
            public void onResponse(Call<PlayerResponse> call, Response<PlayerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Player> players = response.body().getContent();
                    playerAdapter = new PlayerAdapter(players);
                    recyclerView.setAdapter(playerAdapter);
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

    private void searchPlayers(String searchTerm) {
        Call<PlayerResponse> call = playerApi.searchPlayers(searchTerm); // Assuming you're using page 1
        call.enqueue(new Callback<PlayerResponse>() {
            @Override
            public void onResponse(Call<PlayerResponse> call, Response<PlayerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Player> players = response.body().getContent();
                    playerAdapter = new PlayerAdapter(players);
                    recyclerView.setAdapter(playerAdapter);
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

    private void signOut() {
        Call<Void> call = authApi.signOut();
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Successfully logged out", Toast.LENGTH_SHORT).show();
                    // Redirect to login or close activity
                    finish();
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
}