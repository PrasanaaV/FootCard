package com.example.footcard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SigninActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView createAccountTextView, forgotPasswordTextView;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        emailEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        createAccountTextView = findViewById(R.id.signup_link);
        forgotPasswordTextView = findViewById(R.id.forgot_password);

        // Initialise OkHttpClient avec un Interceptor pour injecter automatiquement le token
        client = getAuthenticatedClient();

        loginButton.setOnClickListener(v -> {
            // Logique de connexion
            String username = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(SigninActivity.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    loginUser(username, password); // Appel à la fonction de connexion
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        createAccountTextView.setOnClickListener(v -> {
            Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        forgotPasswordTextView.setOnClickListener(v -> {
            Intent intent = new Intent(SigninActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        });
    }

    // OkHttpClient avec un Interceptor qui ajoute automatiquement le Bearer token
    private OkHttpClient getAuthenticatedClient() {
        return new OkHttpClient.Builder().addInterceptor(chain -> {
            Request original = chain.request();
            SharedPreferences sharedPreferences = getSharedPreferences("auth_prefs", MODE_PRIVATE);
            String accessToken = sharedPreferences.getString("access_token", null);

            if (accessToken != null) {
                // Ajoute le Bearer token seulement s'il est présent
                Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", "Bearer " + accessToken); // Ajouter le token Bearer
                Request request = requestBuilder.build();
                return chain.proceed(request);
            } else {
                return chain.proceed(original);
            }
        }).build();
    }

    private void loginUser(String username, String password) throws JSONException {
        String url = "http://10.0.2.2:8080/api/signin"; // URL de connexion de l'API

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", username);
        jsonObject.put("password", password);

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(SigninActivity.this, "Login failed", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body() != null ? response.body().string() : null;
                    if (responseData != null) {
                        try {
                            JSONObject jsonResponse = new JSONObject(responseData);
                            String accessToken = jsonResponse.getString("bearer");
                            String refreshToken = jsonResponse.getString("refresh");

                            // Stocker les tokens dans SharedPreferences
                            storeTokens(accessToken, refreshToken);

                            runOnUiThread(() -> {
                                Toast.makeText(SigninActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(SigninActivity.this, "Login failed: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    // Fonction pour stocker les tokens
    private void storeTokens(String accessToken, String refreshToken) {
        SharedPreferences sharedPreferences = getSharedPreferences("auth_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("access_token", accessToken);
        editor.putString("refresh_token", refreshToken);
        editor.apply();
    }

    // Récupérer le token d'accès et vérifier s'il est valide
    private void performAuthenticatedRequest() throws JSONException {
        SharedPreferences sharedPreferences = getSharedPreferences("auth_prefs", MODE_PRIVATE);
        String accessToken = sharedPreferences.getString("access_token", null);

        if (accessToken != null) {
            sendAuthenticatedRequest(accessToken);
        } else {
            runOnUiThread(() -> Toast.makeText(SigninActivity.this, "No access token found. Please log in again.", Toast.LENGTH_SHORT).show());
        }
    }

    private void sendAuthenticatedRequest(String accessToken) {
        // Construire la requête avec le Bearer token
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/api/some_protected_endpoint") // Remplacez cette URL par celle de l'API protégée
                .header("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(SigninActivity.this, "Request failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(SigninActivity.this, "Authenticated request successful!", Toast.LENGTH_SHORT).show());
                } else if (response.code() == 401) {
                    // Le token d'accès a expiré, utilisez le refresh token pour obtenir un nouveau token
                    try {
                        refreshToken();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(SigninActivity.this, "Request failed: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }




    // Fonction pour rafraîchir le token
    private void refreshToken() throws JSONException {
        SharedPreferences sharedPreferences = getSharedPreferences("auth_prefs", MODE_PRIVATE);
        String refreshToken = sharedPreferences.getString("refresh_token", null);

        if (refreshToken != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("refresh", refreshToken);

            RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

            Request request = new Request.Builder()
                    .url("http://10.0.2.2:8080/api/token/refresh")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(SigninActivity.this, "Failed to refresh token: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        try {
                            JSONObject jsonResponse = new JSONObject(responseData);
                            String newAccessToken = jsonResponse.getString("bearer");
                            String newRefreshToken = jsonResponse.getString("refresh");

                            // Mettre à jour les tokens dans les SharedPreferences
                            storeTokens(newAccessToken, newRefreshToken);

                            runOnUiThread(() -> {
                                Toast.makeText(SigninActivity.this, "Token refreshed successfully", Toast.LENGTH_SHORT).show();
                                // Relancer la requête avec le nouveau token d'accès
                                sendAuthenticatedRequest(newAccessToken);
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(SigninActivity.this, "Failed to refresh token: " + response.message(), Toast.LENGTH_SHORT).show());
                    }
                }
            });
        } else {
            runOnUiThread(() -> Toast.makeText(SigninActivity.this, "No refresh token available. Please log in again.", Toast.LENGTH_SHORT).show());
        }
    }

}
