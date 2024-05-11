const User = require('../model/User');
const UserInterest = require('../model/UserInterest');
const Interest = require('../model/Interest');

exports.registerUser = async (req, res) => {
    const { username, email, password, phone_number } = req.body;
    if (!username || !email || !password || !phone_number) {
        return res.status(400).send({ message: 'Missing required fields' });
    }

    try {
        const newUser = new User({ username, email, password, phone_number });
        const user = await newUser.save();
        res.status(201).send({ userId: user._id });
    } catch (error) {
        console.error('Error during registration:', error);
        res.status(500).send({ message: 'Registration failed', error });
    }
};

exports.getUserData = async (req, res) => {
    const userId = req.query.userId;
    if (!userId) {
        return res.status(400).send({ message: 'Missing userId parameter' });
    }

    try {
        const user = await User.findById(userId);
        if (!user) {
            return res.status(404).send('User not found');
        }
        res.json({ username: user.username });
    } catch (error) {
        res.status(500).send('Database query failed');
    }
};

exports.handleUserInterests = async (req, res) => {
    const { userId, interests } = req.body;

    console.log('Received interests data:', req.body);

    if (!req.body || !Array.isArray(interests)) {
        console.log('Invalid or missing interests array in request:', req.body);
        return res
            .status(400)
            .send({ message: 'Invalid or missing interests array' });
    }

    try {
        console.log(
            'Inserting interests for user:',
            userId,
            'interests:',
            interests
        );
        const interestDocs = interests.map((interestId) => ({
            userId,
            interestId,
        }));
        const results = await UserInterest.insertMany(interestDocs);
        console.log('Interests inserted successfully:', results);
        res.send('Interests saved successfully');
    } catch (error) {
        console.error('Error saving interests:', error);
        res.status(500).send({ message: 'Failed to save interests', error });
    }
};

exports.getUserInterests = async (req, res) => {
    const { userId } = req.query;
    if (!userId) {
        return res.status(400).send({ message: 'Missing userId parameter' });
    }

    try {
        const interests = await UserInterest.find({ userId: userId }).populate(
            'interestId'
        );
        const interestNames = interests.map((i) => i.interestId.name);
        res.json({ interests: interestNames });
    } catch (error) {
        res.status(500).send({ message: 'Error retrieving interests', error });
    }
};

exports.login = async (req, res) => {
    const { username, password } = req.body;
    try {
        const user = await User.findOne({ username, password }).select(
            'id username'
        );
        if (user) {
            res.json({
                success: true,
                userId: user._id,
                username: user.username,
            });
        } else {
            res.status(404).send({
                success: false,
                message: 'User not found or password does not match',
            });
        }
    } catch (error) {
        res.status(500).send({ message: 'Login error', error });
    }
};

exports.getAllInterests = async (req, res) => {
    try {
        const interests = await Interest.find();
        res.json({ interests });
    } catch (error) {
        console.error('Error retrieving all interests:', error);
        res.status(500).send({
            message: 'Error retrieving all interests',
            error,
        });
    }
};

exports.getUserProfile = async (req, res) => {
    const userId = req.params.userId;
    if (!userId) {
        return res.status(400).send({ message: 'Missing userId parameter' });
    }

    try {
        const user = await User.findById(userId);
        if (!user) {
            return res.status(404).send('User not found');
        }
        res.send(`
            <html>
            <head><title>User Profile</title></head>
            <body>
                <h1>Username: ${user.username}</h1>
                <p>Email: ${user.email}</p>
                <p>Phone: ${user.phone_number}</p>
            </body>
            </html>
        `);
    } catch (error) {
        console.error('Error retrieving user:', error);
        res.status(500).send({ message: 'Failed to retrieve user' });
    }
};
