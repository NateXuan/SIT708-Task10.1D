const mongoose = require('mongoose');
const connectDB = require('./config/dbConnection');
const Interest = require('./model/Interest');

const interests = [
    { name: 'Algorithm' },
    { name: 'Data Structures' },
    { name: 'Web Development' },
    { name: 'Testing' },
];

async function insertInterests() {
    try {
        await connectDB();
        console.log('Connected to MongoDB...');

        await Interest.deleteMany({});
        console.log('Existing interests have been removed.');

        for (let interest of interests) {
            let interestExists = await Interest.findOne({
                name: interest.name,
            });
            if (!interestExists) {
                await new Interest(interest).save();
            }
        }
        console.log('Interests have been inserted!');
    } catch (error) {
        console.error('Error inserting interests:', error);
    } finally {
        await mongoose.disconnect();
        console.log('MongoDB disconnected.');
    }
}

insertInterests();
