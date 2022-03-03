package com.example.geschenkapp

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import java.sql.*


class DbConnector: ViewModel() {
    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private lateinit var connection: Connection

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun connect() {
        try {
            connection = DriverManager.getConnection(
                "jdbc:mysql://db.montesvoss.de:3306/db",
                "android",
                "!#IAl{Q]/WmP@:0="
            )
            println("Database connection established!")
        } catch (e: SQLException) {
            e.printStackTrace()
            println("Unable to establish database connection")
        }
    }

    //Load friends list when on home screen
    //Loads all users the user is friends with
    //Returns user_id, first_name, last_name, date_of_birth, is_favourite, count_gifts
    fun getFriendsFeed(userId: Int): ResultSet {
        val query: String = "SELECT f.friend_id AS user_id, u.first_name, u.last_name, u.date_of_birth, f.is_favourite, \n" +
                "            \"(SELECT COUNT(*) FROM gifts WHERE user_id=f.friend_id) AS count_gifts \n" +
                "            \"FROM friends AS f \n" +
                "            \"LEFT JOIN users AS u on f.friend_id=u.id \n" +
                "            \"WHERE f.user_id = $userId;"
        var statement = connection.prepareStatement(query)
        var result: ResultSet = statement.executeQuery()
        return result
    }

    //Create new account and log in
    //Returns user_id, first_name, last_name, date_of_birth, email, profile_privacy, profile_picture
    //Needed for settings
    fun loginUser(email: String, password: String): ResultSet? {
        try {
            //val query = "CALL login('$email', '$password');"
                val query = "SELECT * FROM users LIMIT 10;"
            var statement = connection.prepareStatement(query)
            return statement.executeQuery()
        } catch (e: SQLException) {
            e.printStackTrace()
            return null
        }
    }

    //Load User when logging in
    //Returns user_id, first_name, last_name, date_of_birth, email, profile_privacy, profile_picture
    //Needed for settings
    fun createUser(FirstName: String, LastName: String, DateOfBirth: Date, Email: String, UserPassword: String): ResultSet {
        val query: String = "CALL addUser($FirstName, $LastName, $DateOfBirth, $Email, $UserPassword);"
        var statement = connection.prepareStatement(query)
        var result: ResultSet = statement.executeQuery()
        return result

    }

    //Search user in search bar with keywords
    //Returns user_id, first_name, last_name
    //Only returns users without privacy status 4 or friends
    fun searchUser(userId: Int, keywords: String): ResultSet {
        val query: String = "SELECT id, first_name, last_name FROM users AS u " +
                "WHERE (profile_privacy <> 4 OR EXISTS(SELECT * FROM friends where user_id=$userId AND friend_id=u.id))" +
                "AND (CONCAT(first_name, ' ', last_name) LIKE CONCAT($keywords, '%') " +
                "OR first_name LIKE CONCAT($keywords, '%') OR last_name LIKE CONCAT($keywords, '%'));"
        var statement = connection.prepareStatement(query)
        var result: ResultSet = statement.executeQuery()
        return result

    }

    //Open user profile in search bar. Returning columns depended on privacy and friendship status of user
    //privacy 4: returns nothing
    //privacy 3: returns first_name, last_name, profile_privacy
    //privacy 2&1 or friend: return first_name, last_name, profile_picture, profile_privacy
    fun getUser(currentUserId: Int, userId: Int): ResultSet {
        val query: String = "CALL getUserById($currentUserId, $userId);"
        var statement = connection.prepareStatement(query)
        var result: ResultSet = statement.executeQuery()
        return result
    }

    //Load gift feed when on home screen
    //Loads all open gifts the user is a member of
    //Returns gift_id, title, price, gift_picture, user_first_name, user_last_name, owner_first_name, owner_last_name
    fun getGiftFeedByMemberId(memberId: Int): ResultSet {
        val query: String = "SELECT g.id, g.title, g.price, g.gift_picture, \n" +
                "u.first_name AS user_first_name, u.last_name AS user_last_name, o.first_name AS owner_first_name, o.last_name AS owner_last_name\n" +
                "FROM gifts AS g \n" +
                "LEFT JOIN users AS o ON g.owner_id=o.id \n" +
                "LEFT JOIN users AS u ON g.user_id = u.id \n" +
                "LEFT JOIN members AS m ON g.id=m.gift_id \n" +
                "WHERE m.user_id = $memberId AND g.is_closed=0;"
        var statement = connection.prepareStatement(query)
        var result: ResultSet = statement.executeQuery()
        return result

    }

    //Load gift feed when on friends profile page
    //Loads all public gifts or private gifts of friends
    //Returns gift_id, title, price, owner_id, is_wish, post_privacy, gift_picture, is_closed, owner_first_name, owner_last_name, member_count, likes
    fun getGiftFeedByUserId(currentUserId: Int, userId: Int): ResultSet {
        val query: String = "SELECT g.id, g.title, g.price, g.owner_id, g.is_wish, g.post_privacy, g.gift_picture, g.is_closed,\n" +
                "o.first_name AS owner_first_name, o.last_name AS owner_last_name, \n" +
                "(SELECT COUNT(*) FROM members WHERE gift_id=g.id) AS member_count, \n" +
                "SUM(l.likes) AS likes\n" +
                "FROM gifts AS g \n" +
                "LEFT JOIN users AS o ON g.owner_id=o.id \n" +
                "LEFT JOIN likes AS l ON g.id=l.gift_id \n" +
                "WHERE g.user_id =$userId AND \n" +
                "(post_privacy=0 OR post_privacy=1 OR post_privacy=2 AND EXISTS(SELECT * FROM friends WHERE user_id=$currentUserId AND friend_id=g.owner_id))\n" +
                "GROUP BY g.id \n" +
                "ORDER BY g.is_closed DESC, likes DESC;"
        var statement = connection.prepareStatement(query)
        var result: ResultSet = statement.executeQuery()
        return result

    }

    //Load members on gift page
    //Returns membership_id, first_name, last_name, max_price, is_fixed
    //No implemented privacy check
    fun getMembers(giftId: Int): ResultSet {
        val query: String = "SELECT m.id, u.first_name, u.last_name, m.max_price, m.is_fixed FROM members as m \n" +
                "LEFT JOIN users AS u ON u.id=m.user_id \n" +
                "WHERE m.gift_id=$giftId;"
        var statement = connection.prepareStatement(query)
        var result: ResultSet = statement.executeQuery()
        return result

    }

    //Load comments on gift page
    //Returns comment_id, first_name, last_name, content, likes
    //No implemented privacy check
    fun getComments(giftId: Int): ResultSet {
        val query: String = "SELECT c.id AS comment_id, u.first_name, u.last_name, c.content, SUM(l.likes) AS likes FROM comments AS c\n" +
                "LEFT JOIN users AS u ON u.id=c.user_id\n" +
                "LEFT JOIN likes AS l ON l.comment_id=c.id\n" +
                "WHERE c.gift_id=$giftId\n" +
                "GROUP BY c.id\n" +
                "ORDER BY likes DESC;"
        var statement = connection.prepareStatement(query)
        var result: ResultSet = statement.executeQuery()
        return result
    }

    fun likeComment(userId: Int, commentId: Int, likes: Int): ResultSet {
        val query: String = "CALL applyLikeToComment($userId, $commentId, $likes)"
        var statement = connection.prepareStatement(query)
        var result: ResultSet = statement.executeQuery()
        return result
    }

    fun likeGift(userId: Int, giftId: Int, likes: Int): ResultSet {
        val query: String = "CALL applyLikeToGift($userId, $giftId, $likes)"
        var statement = connection.prepareStatement(query)
        var result: ResultSet = statement.executeQuery()
        return result
    }

    fun insertComment(userId: Int, giftId: Int, comment: String) {
        val query: String = "INSERT INTO comments(user_id, gift_id, content) VALUES ($userId, $giftId, $comment;"
        var statement = connection.prepareStatement(query)
        var result: ResultSet = statement.executeQuery()
    }

    fun removeComment(commentId: String) {
        val query: String = "DELETE FROM comments WHERE comment_id=$commentId;"
        var statement = connection.prepareStatement(query)
        var result: ResultSet = statement.executeQuery()
    }

    fun addFriend(friendUserId: Int, userId: Int) {
        val query: String = "INSERT INTO friends (friend_id, user_id) VALUES ($friendUserId, $userId);"
        var statement = connection.prepareStatement(query)
        var result: ResultSet = statement.executeQuery()
    }

    fun removeFriend(friendId: Int) {
        val query: String = "DELETE FROM friends WHERE id=$friendId;"
        var statement = connection.prepareStatement(query)
        var result: ResultSet = statement.executeQuery()
    }

    fun updateFavourite(friendId: Int, isFavourite: Int) {
        val query: String = "UPDATE friends SET is_favourite=$isFavourite WHERE id=$friendId;"
        var statement = connection.prepareStatement(query)
        var result: ResultSet = statement.executeQuery()
    }

    fun insertGift(title: String, price: Int?=null, userId: Int, ownerId: Int?=null, postPrivacy: Int=0, giftPicture: String?=null, giftLink: String?=null) {
        val query: String =
            "INSERT INTO gifts(title, price, user_id, owner_id, post_privacy, gift_picture, gift_link)" +
                    "VALUES ($title, $price, $userId, $ownerId, $postPrivacy, $giftPicture, $giftLink);"
        var statement = connection.prepareStatement(query)
        var result: ResultSet = statement.executeQuery()
    }

    fun deleteGift(currentUserId: Int, giftId: Int) {
        val query: String = "DELETE FROM gifts WHERE owner_id=$currentUserId AND id=$giftId;"
        var statement = connection.prepareStatement(query)
        var result: ResultSet = statement.executeQuery()
    }

}