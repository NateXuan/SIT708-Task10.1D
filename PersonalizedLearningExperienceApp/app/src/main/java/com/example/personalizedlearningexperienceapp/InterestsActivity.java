package com.example.personalizedlearningexperienceapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;

public class InterestsActivity extends AppCompatActivity {
    private String userId;
    HashMap<String, String> selectedInterests = new HashMap<>();
    LinearLayout interestsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);

        interestsContainer = findViewById(R.id.interestsContainer);

        userId = getUserId();

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Error: User ID is missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchInterests();

        Button nextButton = findViewById(R.id.buttonNext);
        nextButton.setOnClickListener(view -> {
            if (!selectedInterests.isEmpty()) {
                saveUserInterests();
            } else {
                Toast.makeText(InterestsActivity.this, "Please select at least one interest.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getUserId() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return prefs.getString("userId", null);
    }

    private void fetchInterests() {
        String url = "http://10.0.2.2:3000/getAllInterests";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray interests = response.getJSONArray("interests");
                        for (int i = 0; i < interests.length(); i++) {
                            JSONObject interest = interests.getJSONObject(i);
                            String id = interest.getString("_id");
                            String name = interest.getString("name");
                            addInterestView(id, name);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(InterestsActivity.this, "Error parsing interests", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(InterestsActivity.this, "Failed to fetch interests", Toast.LENGTH_SHORT).show()
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }

    private void addInterestView(String interestId, String name) {
        TextView textView = new TextView(this);
        textView.setText(name);
        textView.setTextSize(18);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(40, 20, 40, 20);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(10, 10, 10, 10);
        textView.setLayoutParams(params);

        textView.setOnClickListener(view -> handleInterestSelection(interestId, textView));
        interestsContainer.addView(textView);
    }


    private void handleInterestSelection(String interestId, TextView textView) {
        textView.setSelected(!textView.isSelected());
        updateTextViewBackground(textView);
        if (textView.isSelected()) {
            selectedInterests.put(interestId, textView.getText().toString());
        } else {
            selectedInterests.remove(interestId);
        }
    }

    private void updateTextViewBackground(TextView textView) {
        textView.setBackgroundResource(R.drawable.selector_interest_background);
    }

    private void saveUserInterests() {
        JSONObject params = new JSONObject();
        try {
            JSONArray interestsArray = new JSONArray();
            for (String interestId : selectedInterests.keySet()) {
                interestsArray.put(interestId);
            }
            params.put("userId", userId);
            params.put("interests", interestsArray);
            Log.d("SaveInterests", "Request params: " + params);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("SaveInterests", "JSON Exception: " + e.getMessage());
        }

        String saveInterestsUrl = "http://10.0.2.2:3000/interests";
        Log.d("SaveInterests", "Sending request to: " + saveInterestsUrl);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, saveInterestsUrl,
                response -> {
                    Log.d("SaveInterests", "Response received: " + response);
                    Intent intent = new Intent(InterestsActivity.this, HomePageActivity.class);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    Log.e("SaveInterests", "Failed to save interests: " + error.toString());
                    Toast.makeText(InterestsActivity.this, "Failed to save interests", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("userId", userId);
                    jsonBody.put("interests", new JSONArray(selectedInterests.keySet()));
                    return jsonBody.toString().getBytes(StandardCharsets.UTF_8);
                } catch (JSONException e) {
                    Log.e("SaveInterests", "JSON exception", e);
                    return null;
                }
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }
}
