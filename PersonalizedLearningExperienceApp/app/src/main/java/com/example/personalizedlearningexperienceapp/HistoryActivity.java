package com.example.personalizedlearningexperienceapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private String userId;
    private HistoryAdapter historyAdapter;
    private final List<QuizHistory> historyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        RecyclerView recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter(this, historyList);
        recyclerViewHistory.setAdapter(historyAdapter);

        userId = getUserId();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Error: User ID is missing.", Toast.LENGTH_SHORT).show();
            finish();
        }

        loadHistoryData();

        Button backButton = findViewById(R.id.backToHomepage);
        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(HistoryActivity.this, HomePageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private String getUserId() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        if (userId == null) {
            Log.e("GetUserId", "User ID is null");
            Toast.makeText(this, "User ID is missing. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
        }
        return userId;
    }

    private void loadHistoryData() {
        Log.d("HistoryActivity", "Loading history for userId: " + userId);
        String url = "http://10.0.2.2:3000/getQuizHistory?userId=" + userId;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject historyObj = response.getJSONObject(i);
                        QuizHistory history = new QuizHistory(
                                historyObj.getString("date"),
                                historyObj.getString("topic"),
                                historyObj.getJSONArray("results")
                        );
                        historyList.add(history);
                    }
                    historyAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, error -> {
            Toast.makeText(HistoryActivity.this, "Error fetching history", Toast.LENGTH_SHORT).show();
            Log.e("History", "Fetch error: " + error.getMessage());
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonArrayRequest);
    }
}
