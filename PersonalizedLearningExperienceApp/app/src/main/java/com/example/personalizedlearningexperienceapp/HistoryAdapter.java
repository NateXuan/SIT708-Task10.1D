package com.example.personalizedlearningexperienceapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final Context context;
    private final List<QuizHistory> historyList;

    public HistoryAdapter(Context context, List<QuizHistory> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        QuizHistory history = historyList.get(position);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            @SuppressLint("SimpleDateFormat") Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(history.getDate());
            assert date != null;
            holder.textViewDate.setText(dateFormat.format(date));
        } catch (Exception e) {
            holder.textViewDate.setText("Invalid date");
        }

        holder.textViewTopic.setText(history.getTopic());
        holder.linearLayoutResults.removeAllViews();

        for (QuizHistory.QuestionResult result : history.getResults()) {
            TextView questionView = new TextView(context);
            questionView.setText("Question: " + result.getQuestionText());
            questionView.setTextSize(14);
            holder.linearLayoutResults.addView(questionView);

            TextView resultView = new TextView(context);
            String answerText = "Answer: " + (result.isCorrect() ? "Correct" : "Incorrect - Correct Answer: " + result.getCorrectAnswer());
            resultView.setText(answerText);
            resultView.setTextSize(14);
            resultView.setTextColor(ContextCompat.getColor(context, result.isCorrect() ? R.color.correct_answer_color : R.color.wrong_answer_color));
            holder.linearLayoutResults.addView(resultView);
        }
    }


    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDate, textViewTopic;
        LinearLayout linearLayoutResults;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewTopic = itemView.findViewById(R.id.textViewTopic);
            linearLayoutResults = itemView.findViewById(R.id.linearLayoutResults);
        }
    }
}
