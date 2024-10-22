package com.example.footcard;

import android.content.Intent;
import android.os.Bundle;
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

public class ActivateActivity extends AppCompatActivity {

    private EditText codeEditText;
    private Button activateButton, resendCodeButton;
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate);

        codeEditText = findViewById(R.id.code);
        activateButton = findViewById(R.id.activate_button);
        resendCodeButton = findViewById(R.id.resend_code_button);

        // get email from past activity
        String email = getIntent().getStringExtra("email");

        // account activation
        activateButton.setOnClickListener(v -> {
            String code = codeEditText.getText().toString().trim();
            try {
                activateAccount(email, code);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        // ask for a new code
        resendCodeButton.setOnClickListener(v -> {
            try {
                requestNewActivationCode(email); // method to ask a new code
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    // function to send activation request to the back
    private void activateAccount(String email, String code) throws JSONException {
        String url = "http://10.0.2.2:8080/api/activate"; // API's activation url

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", email);
        jsonObject.put("code", code);

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ActivateActivity.this, "Activation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(ActivateActivity.this, "Account activated!", Toast.LENGTH_SHORT).show();
                        // redirect to login page
                        Intent intent = new Intent(ActivateActivity.this, SigninActivity.class);
                        startActivity(intent);
                        finish(); // close activation activity
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(ActivateActivity.this, "Activation failed: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    // function to ask for a new activation code
    private void requestNewActivationCode(String email) throws JSONException {
        OkHttpClient client = new OkHttpClient();

        // create the json with the email
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", email);

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/api/new-activation-code")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ActivateActivity.this, "Failed to request new activation code", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(ActivateActivity.this, "New activation code sent", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(ActivateActivity.this, "Failed to send new activation code", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
