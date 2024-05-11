package com.example.personalizedlearningexperienceapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class ResultsActivity extends AppCompatActivity {
    private String userId;
    private ArrayList<Boolean> results;
    private ArrayList<String> correctAnswers;
    ArrayList<String> questions;
    private String topic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        questions = getIntent().getStringArrayListExtra("questions");

        results = (ArrayList<Boolean>) getIntent().getSerializableExtra("results");
        correctAnswers = (ArrayList<String>) getIntent().getSerializableExtra("correctAnswers");
        topic = getIntent().getStringExtra("topic");
        LinearLayout resultsContainer = findViewById(R.id.resultsContainer);

        userId = getUserId();
        Log.d("ResultsActivity", "Fetched userId: " + userId);
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Error: User ID is missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (results != null) {
            saveQuizHistory();
        } else {
            Toast.makeText(this, "No results data provided.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        for (int i = 0; i < Objects.requireNonNull(results).size(); i++) {
            // Create a TextView for the question number
            TextView questionView = new TextView(this);
            questionView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            questionView.setText(String.format(Locale.getDefault(), "Question %d: %s", i + 1, questions.get(i)));
            questionView.setTextSize(18);
            questionView.setPadding(0, 10, 0, 10);
            resultsContainer.addView(questionView);

            // Create a TextView for the answer correctness
            TextView resultView = new TextView(this);
            resultView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            resultView.setText(results.get(i) ? "Correct" : "Incorrect");
            resultView.setTextSize(16);
            resultView.setTextColor(results.get(i) ? getColor(R.color.correct_answer_color) : getColor(R.color.wrong_answer_color));
            resultView.setPadding(0, 0, 0, 10);
            resultsContainer.addView(resultView);
        }

        Button continueButton = findViewById(R.id.continueButton);
        continueButton.setOnClickListener(v -> {
            Intent intent = new Intent(ResultsActivity.this, HomePageActivity.class);
            // FLAG_ACTIVITY_CLEAR_TOP ensures that all other activities on top of the HomePageActivity are cleared.
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    private String getUserId() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return prefs.getString("userId", null);
    }

    private void saveQuizHistory() {
        String url = "http://10.0.2.2:3000/addQuizHistory";
        JSONObject params = new JSONObject();
        try {
            params.put("userId", userId);
            params.put("topic", topic);
            JSONArray resultsArray = new JSONArray();
            for (int i = 0; i < results.size(); i++) {
                JSONObject result = new JSONObject();
                result.put("questionText", questions.get(i));
                result.put("isCorrect", results.get(i));
                result.put("correctAnswer", correctAnswers.get(i));
                resultsArray.put(result);
            }
            params.put("results", resultsArray);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("SaveHistory", "Error building JSON for history.");
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                response -> Log.d("SaveHistory", "History saved"),
                error -> Log.e("SaveHistory", "Failed to save history: " + error.getMessage())
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }

}

