const express = require('express');
const app = express();
const connectDB = require('./config/dbConnection');
const userRoutes = require('./Routes/userRoutes');
const quizRoutes = require('./routes/quizRoutes');

connectDB();

app.use(express.json());
app.use('/', userRoutes);
app.use('/', quizRoutes);

app.use((err, _req, res) => {
    console.error(err.stack);
    res.status(500).send({ message: 'Something broke!' });
});

const PORT = process.env.PORT;
app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}.`);
});
