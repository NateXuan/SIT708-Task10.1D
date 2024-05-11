const express = require('express');
const router = express.Router();
const quizController = require('../controller/quizController');

router.post('/addQuizHistory', quizController.addQuizHistory);
router.get('/getQuizHistory', quizController.getQuizHistory);

module.exports = router;
