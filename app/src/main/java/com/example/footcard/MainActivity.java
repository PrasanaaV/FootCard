package com.example.footcard;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import androidx.appcompat.widget.SearchView;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // example
    private int userId = 3;
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static final String BEARER_TOKEN = "eyJhbGciOiJIUzM4NCJ9.eyJpYXQiOjE3Mjk2MTU1NzIsImV4cCI6MTcyOTYyOTk3Miwic3ViIjoiMyIsInJvbGUiOiJVU0VSIn0.sB5jJxdJEdflcEjVeYiDWBYR035hQOZagl8Kv4HCFPEfItoTw3iI0ZvGRez2Yy6M";
    private RecyclerView recyclerView;
    private PlayerAdapter playerAdapter;
    private PlayerApi playerApi;
    private AuthApi authApi;
    private int currentTabId = R.id.nav_all_players; // initial tab

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        playerAdapter = new PlayerAdapter(new ArrayList<>());
        recyclerView.setAdapter(playerAdapter);

        SearchView searchViewAllPlayers = findViewById(R.id.searchViewAllPlayers);
        SearchView searchViewMyPlayers = findViewById(R.id.searchViewMyPlayers);

        searchViewAllPlayers.setVisibility(View.GONE);  // Commencez par cacher une des deux
        searchViewMyPlayers.setVisibility(View.VISIBLE);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(BEARER_TOKEN))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        playerApi = retrofit.create(PlayerApi.class);
        authApi = retrofit.create(AuthApi.class);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            currentTabId = item.getItemId();
            switch (item.getItemId()) {
                case R.id.nav_all_players:
                    loadAllPlayers(0);
                    return true;
                case R.id.nav_user_players:
                    fetchAndDisplayDailyPlayer();
                    loadPlayers(userId, 0);
                    return true;
                case R.id.nav_logout:
                    signOut();
                    return true;
            }
            return false;
        });

        searchViewMyPlayers.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchPlayersForUser(userId, query, 0);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadPlayers(userId, 0);
                } else {
                    searchPlayersForUser(userId, newText, 0);
                }
                return true;
            }
        });

        searchViewAllPlayers.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (currentTabId == R.id.nav_all_players) { // Assurez-vous que l'onglet All Players est actif
                    searchAllPlayers(query, 0);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (currentTabId == R.id.nav_all_players) { // Idem pour la modification du texte
                    if (newText.isEmpty()) {
                        loadAllPlayers(0);
                    } else {
                        searchAllPlayers(newText, 0);
                    }
                }
                return true;
            }
        });


        loadAllPlayers(0);


    }


    private void loadAllPlayers(int page) {
        playerApi.getAllPlayers(page, 30).enqueue(new Callback<PlayerResponse>() {
            @Override
            public void onResponse(Call<PlayerResponse> call, Response<PlayerResponse> response) {
                if (currentTabId != R.id.nav_all_players) {
                    Log.d("MainActivity", "Ignoring all players load for inactive tab");
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    List<Player> players = response.body().getContent();
                    if (page == 0) {
                        playerAdapter.setPlayers(players);
                    } else {
                        playerAdapter.addPlayers(players);
                    }
                    if (page < response.body().getPage().getTotalPages() - 1) {
                        loadAllPlayers(page + 1);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Failed to retrieve players", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PlayerResponse> call, Throwable t) {
                if (currentTabId != R.id.nav_all_players) {
                    Log.d("MainActivity", "Ignoring failure for inactive tab");
                    return;
                }
                Toast.makeText(MainActivity.this, "Error fetching players", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPlayers(int userId, int page) {
        playerApi.getPlayers(userId, page, 30).enqueue(new Callback<PlayerResponse>() {
            @Override
            public void onResponse(Call<PlayerResponse> call, Response<PlayerResponse> response) {
                if (currentTabId != R.id.nav_user_players) {
                    Log.d("MainActivity", "Ignoring user players load for inactive tab");
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    List<Player> players = response.body().getContent();
                    if (page == 0) {
                        playerAdapter.setPlayers(players);
                    } else {
                        playerAdapter.addPlayers(players);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Failed to retrieve players", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PlayerResponse> call, Throwable t) {
                if (currentTabId != R.id.nav_user_players) {
                    Log.d("MainActivity", "Ignoring failure for inactive tab");
                    return;
                }
                Toast.makeText(MainActivity.this, "Error fetching players", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchPlayersForUser(int userId, String searchTerm, int page) {
        playerApi.searchPlayersForUser(userId, searchTerm, page).enqueue(new Callback<PlayerResponse>() {
            @Override
            public void onResponse(Call<PlayerResponse> call, Response<PlayerResponse> response) {
                if (currentTabId != R.id.nav_user_players) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    List<Player> players = response.body().getContent();
                    if (page == 0) {
                        playerAdapter.setPlayers(players);
                    } else {
                        playerAdapter.addPlayers(players);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "No players found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PlayerResponse> call, Throwable t) {
                if (currentTabId != R.id.nav_user_players) {
                    Log.d("MainActivity", "Ignoring search failure for inactive tab");
                    return;
                }
                Toast.makeText(MainActivity.this, "Error searching players", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchAllPlayers(String searchTerm, int page) {
        playerApi.searchPlayers(searchTerm, page).enqueue(new Callback<PlayerResponse>() {
            @Override
            public void onResponse(Call<PlayerResponse> call, Response<PlayerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Player> players = response.body().getContent();
                    if (page == 0) {
                        playerAdapter.setPlayers(players);
                    } else {
                        playerAdapter.addPlayers(players);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "No players found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PlayerResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error during search", Toast.LENGTH_SHORT).show();
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
    private void fetchAndDisplayDailyPlayer() {
        Call<Player> call = playerApi.getDailyPlayer();
        call.enqueue(new Callback<Player>() {
            @Override
            public void onResponse(Call<Player> call, Response<Player> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateDailyPlayerUI(response.body());
                } else {
                    Toast.makeText(MainActivity.this, "No daily player yet", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Player> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error fetching daily player", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPlayerImage(ImageView imageView, String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e("MainActivity", "Image load failed for url: " + imageUrl, e);
                        return false;
                    }

                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(imageView);
    }

    private void updateDailyPlayerUI(Player player) {
        TextView dailyPlayerName = findViewById(R.id.dailyPlayerName);
        TextView dailyPlayerPosition = findViewById(R.id.dailyPlayerPosition);
        ImageView dailyPlayerImage = findViewById(R.id.dailyPlayerImage);

        dailyPlayerName.setText(player.getName());
        dailyPlayerPosition.setText(player.getPosition());

        // Load the player image
        loadPlayerImage(dailyPlayerImage, BASE_URL + player.getImageUrl());
    }

}
