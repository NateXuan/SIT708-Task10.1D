package com.example.personalizedlearningexperienceapp;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QuizHistory {
    private String date;
    private String topic;
    private List<QuestionResult> results;

    public QuizHistory(String date, String topic, JSONArray resultsArray) throws JSONException {
        this.date = date;
        this.topic = topic;
        this.results = new ArrayList<>();
        for (int i = 0; i < resultsArray.length(); i++) {
            JSONObject result = resultsArray.getJSONObject(i);
            String questionText = result.getString("questionText");
            boolean isCorrect = result.getBoolean("isCorrect");
            String correctAnswer = result.optString("correctAnswer");
            results.add(new QuestionResult(questionText, isCorrect, correctAnswer));
        }
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<QuestionResult> getResults() {
        return results;
    }

    public void setResults(List<QuestionResult> results) {
        this.results = results;
    }

    public static class QuestionResult {
        private String questionText;
        private boolean isCorrect;
        private String correctAnswer;

        public QuestionResult(String questionText, boolean isCorrect, String correctAnswer) {
            this.questionText = questionText;
            this.isCorrect = isCorrect;
            this.correctAnswer = correctAnswer;
        }

        public String getQuestionText() {
            return questionText;
        }

        public void setQuestionText(String questionText) {
            this.questionText = questionText;
        }

        public boolean isCorrect() {
            return isCorrect;
        }

        public void setCorrect(boolean correct) {
            isCorrect = correct;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }

        public void setCorrectAnswer(String correctAnswer) {
            this.correctAnswer = correctAnswer;
        }
    }
}
