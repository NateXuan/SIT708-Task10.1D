package com.example.personalizedlearningexperienceapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;

public class HomePageActivity extends AppCompatActivity {
    TextView textViewYourName, textViewNotification;
    ImageView imageViewProfile;
    LinearLayout containerTask;
    private String userId;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        textViewYourName = findViewById(R.id.textViewYourName);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        textViewNotification = findViewById(R.id.textViewNotification);
        containerTask = findViewById(R.id.containerTask);
        prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        userId = getUserId();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Error: User ID is missing.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            fetchUserData(userId);
            checkForNewTask();
        }

        Button shareButton = findViewById(R.id.buttonShare);
        shareButton.setOnClickListener(v -> shareProfile());

        Button historyButton = findViewById(R.id.historyButton);
        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, HistoryActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        Button upgradeButton = findViewById(R.id.upgradeButton);
        upgradeButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, UpgradeActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
    }

    private void shareProfile() {
        String shareUrl = "http://10.0.2.2:3000/userProfile/" + userId;
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out my profile here: " + shareUrl);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    private String getUserId() {
        return prefs.getString("userId", null);
    }

    private void fetchUserData(String userId) {
        String url = "http://10.0.2.2:3000/data?userId=" + userId;
        Log.d("FetchData", "Request URL: " + url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String username = response.getString("username");
//                      String profilePictureUrl = response.getString("profilePicture");
                        textViewYourName.setText(username);
                        Log.d("FetchData", "Username set to: " + username);
                        // Picasso.get().load(profilePictureUrl).into(imageViewProfile);
                    } catch (JSONException e) {
                        Log.e("FetchData", "JSON Parsing error: " + e.getMessage());
                        Toast.makeText(HomePageActivity.this, "Error parsing user data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(HomePageActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void checkForNewTask() {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastTaskDate = prefs.getString("lastTaskDate", "");
        Log.d("TaskCheck", "Current Date: " + currentDate + ", Last Task Date: " + lastTaskDate);
        // if it is a new day
        if (!currentDate.equals(lastTaskDate)) {
            Log.d("TaskCheck", "New day detected, fetching new tasks.");
            fetchUserInterests(userId);
            prefs.edit().putString("lastTaskDate", currentDate).apply();
        } else {
            Log.d("TaskCheck", "Same day, no new tasks fetched.");
            fetchUserInterests(userId);
        }
    }

    private void resetTaskCompletionStatus(JSONArray interests) {
        SharedPreferences.Editor editor = prefs.edit();
        try {
            for (int i = 0; i < interests.length(); i++) {
                String interest = interests.getString(i);
                editor.putBoolean("Generated Task for " + interest, false);
            }
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fetchUserInterests(String userId) {
        String url = "http://10.0.2.2:3000/userInterests?userId=" + userId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray interests = response.getJSONArray("interests");
                        int tasksDue = interests.length();
                        textViewNotification.setText(String.format(Locale.getDefault(), "You have %d task%s due", tasksDue, tasksDue > 1 ? "s" : ""));
                        containerTask.removeAllViews();
                        resetTaskCompletionStatus(interests);

                        for (int i = 0; i < interests.length(); i++) {
                            String interest = interests.getString(i);
                            boolean isCompleted = prefs.getBoolean("Generated Task for " + interest, false);
                            if (!isCompleted) {
                                addTaskView("Generated Task for " + interest, "Please complete today's quiz about " + interest + ".", interest);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(HomePageActivity.this, "Failed to parse interests", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(HomePageActivity.this, "Failed to fetch interests", Toast.LENGTH_SHORT).show()
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }

    private void addTaskView(String taskName, String description, final String topic) {
        boolean isCompleted = prefs.getBoolean("Generated Task for " + topic, false);
        if (!isCompleted) {
            TextView taskTitle = new TextView(this);
            taskTitle.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            taskTitle.setText(taskName);
            taskTitle.setTextColor(ContextCompat.getColor(this, R.color.black));
            taskTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

            TextView taskDescription = new TextView(this);
            taskDescription.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            taskDescription.setText(description);
            taskDescription.setTextColor(ContextCompat.getColor(this, R.color.dark_gray));
            taskDescription.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

            Button viewTaskButton = new Button(this);
            viewTaskButton.setText("View Task");
            viewTaskButton.setOnClickListener(v -> {
                Intent intent = new Intent(HomePageActivity.this, QuizActivity.class);
                intent.putExtra("topic", topic);
                startActivity(intent);
            });

            containerTask.addView(taskTitle);
            containerTask.addView(taskDescription);
            containerTask.addView(viewTaskButton);
        }
    }
}
