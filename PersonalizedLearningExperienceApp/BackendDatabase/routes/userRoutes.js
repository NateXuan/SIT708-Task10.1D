const express = require('express');
const router = express.Router();
const userController = require('../controller/userController');

router.post('/register', userController.registerUser);
router.get('/data', userController.getUserData);
router.post('/interests', userController.handleUserInterests);
router.get('/userInterests', userController.getUserInterests);
router.post('/login', userController.login);
router.get('/getAllInterests', userController.getAllInterests);
router.get('/userProfile/:userId', userController.getUserProfile);

module.exports = router;
