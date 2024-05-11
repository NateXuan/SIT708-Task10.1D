const mongoose = require('mongoose');

const userInterestSchema = new mongoose.Schema({
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true,
    },
    interestId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Interest',
        required: true,
    },
});

const UserInterest = mongoose.model('UserInterest', userInterestSchema);

module.exports = UserInterest;
