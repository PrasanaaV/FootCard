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

public class ResetPasswordActivity extends AppCompatActivity{
    private EditText emailEditText;
    private Button resetPasswordButton;
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resetpassword);

        emailEditText = findViewById(R.id.email);
        resetPasswordButton = findViewById(R.id.reset_password_button);

        resetPasswordButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(ResetPasswordActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    requestPasswordReset(email);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void requestPasswordReset(String email) throws JSONException {
        String url = "http://10.0.2.2:8080/api/password/reset"; // URL de l'API

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", email);

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Log.d("ResetPassword", "Request: " + jsonObject.toString());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ResetPassword", "Failed: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(ResetPasswordActivity.this, "Password reset failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("ResetPassword", "Response Success: " + response.body().string());
                    runOnUiThread(() -> {
                        Toast.makeText(ResetPasswordActivity.this, "Check your email for the reset code", Toast.LENGTH_SHORT).show();
                        // Redirection vers la page de confirmation avec l'email
                        Intent intent = new Intent(ResetPasswordActivity.this, ConfirmNewPasswordActivity.class);
                        intent.putExtra("email", email); // Passer l'email à la nouvelle activité
                        startActivity(intent);
                    });
                } else {
                    Log.e("ResetPassword", "Response Failed: " + response.message());
                    runOnUiThread(() -> Toast.makeText(ResetPasswordActivity.this, "Password reset failed: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
