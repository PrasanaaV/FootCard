package com.example.footcard;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static final String BEARER_TOKEN = "eyJhbGciOiJIUzM4NCJ9.eyJpYXQiOjE3Mjk0NTA0NzksImV4cCI6MTcyOTQ2NDg3OSwic3ViIjoiMyIsInJvbGUiOiJVU0VSIn0.Lm60KopS8y9WpFx-d-wfU44hMww7XkiuFGzdcaUjhpDP_ldFDhv341RHU1bsudnb";
    private RecyclerView recyclerView;
    private PlayerAdapter playerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);  // Ensure this matches the ID in activity_main.xml
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));  // Set GridLayoutManager with 4 columns

        // Create an OkHttpClient and add the AuthInterceptor to include the Bearer token
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(BEARER_TOKEN))  // Add Bearer token interceptor
                .build();

        // Build Retrofit with the custom OkHttpClient
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        // Create your API interface
        PlayerApi playerApi = retrofit.create(PlayerApi.class);

        Call<PlayerResponse> call = playerApi.getPlayers(3);  // Fetch players from API
        call.enqueue(new Callback<PlayerResponse>() {
            @Override
            public void onResponse(Call<PlayerResponse> call, Response<PlayerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PlayerResponse playerResponse = response.body();
                    Log.d("MainActivity", "Response: " + playerResponse.toString());
                    // Get the list of players from the content field
                    List<Player> players = playerResponse.getContent();

                    // Set up the adapter with the fetched player data
                    playerAdapter = new PlayerAdapter(players);
                    recyclerView.setAdapter(playerAdapter);

                    // Handle pagination if needed
                    PlayerResponse.PageInfo pageInfo = playerResponse.getPage();
                    Log.d("MainActivity", "Page: " + pageInfo.getNumber() + " of " + pageInfo.getTotalPages());
                } else {
                    Log.e("MainActivity", "Failed to retrieve players: " + response.code() + " " + response.message());
                    Toast.makeText(MainActivity.this, "Failed to retrieve players", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PlayerResponse> call, Throwable t) {
                Log.e("MainActivity", "Error fetching players", t);
                Toast.makeText(MainActivity.this, "Error fetching players", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
