package com.example.footcard;

import android.content.Intent;
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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignupActivity extends AppCompatActivity {

    private EditText usernameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button signupButton;
    private TextView loginLinkTextView;
    private OkHttpClient client = new OkHttpClient(); // for requests

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        signupButton = findViewById(R.id.signup_button);
        loginLinkTextView = findViewById(R.id.login_link);

        signupButton.setOnClickListener(v -> {
            // signup logic
            String username = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            // verif if passwords match
            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                // send the signin request to the back
                try {
                    signupUser(username, email, password); // call signupUser function
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // Toast.makeText(SignupActivity.this, "Signup clicked", Toast.LENGTH_SHORT).show();
            }
        });

        loginLinkTextView.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
            startActivity(intent);
        });
    }

    // function to send signup request to the back
    private void signupUser(String username, String email, String password) throws JSONException {
        String url = "http://10.0.2.2:8080/api/signup";

        // create json object for signup
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", username);
        jsonObject.put("email", email);
        jsonObject.put("password", password);

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Log.d("Signup", "Request: " + jsonObject.toString()); // test

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Signup", "Failed: " + e.getMessage()); // test
                runOnUiThread(() -> Toast.makeText(SignupActivity.this, "Signup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("Signup", "Response Success: " + response.body().string()); // test
                    runOnUiThread(() -> {
                        Toast.makeText(SignupActivity.this, "Signup successful! Check your email for activation code", Toast.LENGTH_SHORT).show();
                        // redirect to activation code verification
                        Intent intent = new Intent(SignupActivity.this, ActivateActivity.class);
                        intent.putExtra("email", email); // fill email into activation code verification
                        startActivity(intent);
                    });
                } else {
                    Log.e("Signup", "Response Failed: " + response.message()); // test
                    runOnUiThread(() -> Toast.makeText(SignupActivity.this, "Signup failed: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });

    }
}
