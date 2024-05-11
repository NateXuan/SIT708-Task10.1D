const mongoose = require('mongoose');

const quizHistorySchema = new mongoose.Schema({
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        required: true,
        ref: 'User'
    },
    topic: {
        type: String,
        required: true
    },
    results: [{
        questionText: {
            type: String,
            required: true
        },
        isCorrect: {
            type: Boolean,
            required: true
        },
        correctAnswer: {
            type: String,
            required: true
        }
    }],
    date: {
        type: Date,
        default: Date.now
    }
});

const QuizHistory = mongoose.model('QuizHistory', quizHistorySchema);
module.exports = QuizHistory;
