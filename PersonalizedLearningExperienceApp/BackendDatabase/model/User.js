const mongoose = require('mongoose');

const userSchema = new mongoose.Schema(
    {
        username: { type: String, required: true, unique: true },
        email: { type: String, required: true, unique: true },
        password: { type: String, required: true },
        phone_number: { type: String, required: true },
        // profile_picture: { type: String }
    },
    {
        timestamps: true,
    }
);

const User = mongoose.model('User', userSchema);

module.exports = User;
