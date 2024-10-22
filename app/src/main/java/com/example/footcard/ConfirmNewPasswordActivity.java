package com.example.footcard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConfirmNewPasswordActivity extends AppCompatActivity {
    private EditText codeEditText, newPasswordEditText;
    private Button confirmButton;
    private OkHttpClient client = new OkHttpClient();
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmnewpassword);

        codeEditText = findViewById(R.id.code);
        newPasswordEditText = findViewById(R.id.new_password);
        confirmButton = findViewById(R.id.confirm_button);

        // Récupérer l'email passé depuis l'activité précédente
        email = getIntent().getStringExtra("email");

        confirmButton.setOnClickListener(v -> {
            String code = codeEditText.getText().toString().trim();
            String newPassword = newPasswordEditText.getText().toString().trim();

            if (code.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(ConfirmNewPasswordActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    confirmNewPassword(code, newPassword);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void confirmNewPassword(String code, String newPassword) throws JSONException {
        String url = "http://10.0.2.2:8080/api/password/new"; // URL de l'API pour confirmer le nouveau mot de passe

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", email);
        jsonObject.put("code", code);
        jsonObject.put("new_password", newPassword);

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Log.d("ConfirmNewPassword", "Request: " + jsonObject.toString());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ConfirmNewPassword", "Failed: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(ConfirmNewPasswordActivity.this, "Failed to set new password: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                if (response.isSuccessful()) {
                    Log.d("ConfirmNewPassword", "Response Success: " + response.body().string());
                    runOnUiThread(() -> {
                        Toast.makeText(ConfirmNewPasswordActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                        // Redirection vers la page de connexion
                        Intent intent = new Intent(ConfirmNewPasswordActivity.this, SigninActivity.class);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    Log.e("ConfirmNewPassword", "Response Failed: " + response.message());
                    runOnUiThread(() -> Toast.makeText(ConfirmNewPasswordActivity.this, "Failed to set new password: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
