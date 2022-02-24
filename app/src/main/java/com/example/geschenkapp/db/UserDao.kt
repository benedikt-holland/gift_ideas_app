package com.example.geschenkapp.db

import androidx.room.*
import com.example.geschenkapp.data.*
//import java.util.*

//Data access object for user data
@Dao
interface UserDao {
    //Create new account and log in
    //Returns user_id, first_name, last_name, date_of_birth, email, profile_privacy, profile_picture
    //Needed for settings
    //@Query("CALL addUser(:FirstName, :LastName, :DateOfBirth, :Email, :UserPassword);")
    //fun addNewUser(FirstName: String, LastName: String, DateOfBirth: Date, Email: String, UserPassword: String): User

    //Load User when logging in
    //Returns user_id, first_name, last_name, date_of_birth, email, profile_privacy, profile_picture
    //Needed for settings
    //@Query("CALL login(:email, :userPassword);")
    //fun getUserByLogin(email: String, userPassword: String): User

    //Load friends list when on home screen
    //Loads all users the user is friends with
    //Returns user_id, first_name, last_name, date_of_birth, is_favourite, count_gifts
    @Query("SELECT f.friend_id AS user_id, u.first_name, u.last_name, u.date_of_birth, f.is_favourite, " +
            "(SELECT COUNT(*) FROM gifts WHERE user_id=f.friend_id) AS count_gifts " +
            "FROM friends AS f " +
            "LEFT JOIN users AS u on f.friend_id=u.id " +
            "WHERE f.user_id = :userId;")
    fun getFriendsByUserId(userId: String): List<Friend>

    //Search user in search bar with keywords
    //Returns user_id, first_name, last_name
    //Only returns users without privacy status 4 or friends
    @Query("SELECT id, first_name, last_name FROM users AS u WHERE (profile_privacy <> 4 OR EXISTS(SELECT * FROM friends where user_id= :userId AND friend_id=u.id))" +
            "AND (CONCAT(first_name, ' ', last_name) LIKE CONCAT(:keywords, '%') OR first_name LIKE CONCAT(:keywords, '%') OR last_name LIKE CONCAT(:keywords, '%'));")
    fun searchUserByKeyword(userId: Int, keywords: String): User

    //Open user profile in search bar. Returning columns depended on privacy and friendship status of user
    //privacy 4: returns nothing
    //privacy 3: returns first_name, last_name, profile_privacy
    //privacy 2&1 or friend: return first_name, last_name, profile_picture, profile_privacy
    //@Query("CALL getUserById(:userId);")
    //fun getUserById(userId: Int): User
}