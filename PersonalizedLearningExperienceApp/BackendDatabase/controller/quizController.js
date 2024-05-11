const mongoose = require('mongoose');
const QuizHistory = require('../model/QuizHistory');

exports.addQuizHistory = async (req, res) => {
    const { userId, results, topic } = req.body;
    if (!userId || !results || !topic) {
        return res.status(400).send({ message: 'Missing required fields' });
    }
    try {
        const newHistory = new QuizHistory({
            userId,
            results,
            topic,
            date: new Date(),
        });
        await newHistory.save();
        res.status(201).send({ message: 'History saved' });
    } catch (error) {
        console.error('Error saving quiz history:', error);
        res.status(500).send({ message: 'Failed to save quiz history' });
    }
};

exports.getQuizHistory = async (req, res) => {
    const { userId } = req.query;
    if (!userId || !mongoose.Types.ObjectId.isValid(userId)) {
        return res
            .status(400)
            .send({ message: 'Invalid or missing userId parameter' });
    }

    try {
        const histories = await QuizHistory.find({ userId }).sort({ date: -1 });
        res.json(histories);
    } catch (error) {
        console.error('Error retrieving quiz history:', error);
        res.status(500).send({
            message: 'Failed to retrieve quiz history',
            error,
        });
    }
};
