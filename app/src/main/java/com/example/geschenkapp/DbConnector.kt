package com.example.geschenkapp

import androidx.lifecycle.ViewModel
import java.sql.*
import java.time.LocalDate


//Connector for mysql database, contains all SQL Methods for backend
@Suppress("SpellCheckingInspection", "unused")
class DbConnector : ViewModel() {
    private lateinit var connection: Connection

    fun connect(url: String, usr: String, pwd: String) {
        connection = DriverManager.getConnection(
            url,
            usr,
            pwd
        )
        println("Database connection established!")
    }

    //Load friends list when on home screen
    //Loads all users the user is friends with
    //Returns user_id, first_name, last_name, date_of_birth, is_favourite, count_gifts
    fun getFriendsFeed(userId: Int): ResultSet {
        val query = "SELECT f.id, f.friend_id, u.first_name, u.last_name, " +
                "u.date_of_birth, f.is_favourite, " +
                "(SELECT COUNT(*) FROM gifts WHERE user_id=f.friend_id) AS count_gifts, " +
                "IF((DAYOFYEAR(u.date_of_birth) - DAYOFYEAR(NOW()))<0, " +
                "(DAYOFYEAR(u.date_of_birth) - DAYOFYEAR(NOW()))+365, " +
                "(DAYOFYEAR(u.date_of_birth) - DAYOFYEAR(NOW()))) AS days_remaining " +
                "FROM friends AS f " +
                "LEFT JOIN users AS u on f.friend_id=u.id " +
                "WHERE f.user_id = $userId " +
                "ORDER BY days_remaining ASC;"
        val statement = connection.prepareStatement(query)
        return statement.executeQuery()
    }

    //Load User when logging in
    //Returns user_id, first_name, last_name, date_of_birth, email, profile_privacy, profile_picture
    //Needed for settings
    fun loginUser(email: String, password: String): ResultSet {
        val query = "CALL login('$email', '$password');"
        val statement = connection.prepareCall(query)
        return statement.executeQuery()
    }

    //Create new account and log in
    //Returns user_id, first_name, last_name, date_of_birth, email, profile_privacy, profile_picture
    //Needed for settings
    fun createUser(
        FirstName: String, LastName: String, DateOfBirth: LocalDate,
        Email: String, UserPassword: String
    ): ResultSet {
        val query =
            "CALL addUser('$FirstName', '$LastName', '$DateOfBirth', '$Email', '$UserPassword');"
        val statement = connection.prepareCall(query)
        return statement.executeQuery()

    }

    //Search user in search bar with keywords
    //Returns user_id, first_name, last_name
    //Only returns users without privacy status 4 or friends
    fun searchUser(userId: Int, keywords: String): ResultSet {
        val query = "SELECT id, first_name, last_name FROM users AS u " +
                "WHERE (profile_privacy <> 4 OR EXISTS( " +
                "SELECT * FROM friends where user_id=$userId AND friend_id=u.id)) " +
                "AND (CONCAT(first_name, ' ', last_name) LIKE CONCAT('$keywords', '%') " +
                "OR first_name LIKE CONCAT('$keywords', '%') " +
                "OR last_name LIKE CONCAT('$keywords', '%') " +
                "OR email LIKE CONCAT('$keywords', '%'));"
        val statement = connection.prepareStatement(query)
        return statement.executeQuery()

    }

    //Get User from Id for new Activities
    fun getUserById(userId: Int): ResultSet {
        val query = "SELECT id, first_name, last_name, date_of_birth, email, " +
                "profile_privacy, profile_picture FROM users WHERE id=$userId;"
        val statement = connection.prepareStatement(query)
        return statement.executeQuery()
    }

    //Open user profile in search bar. Returning columns depended on privacy and friendship status
    //privacy 4: returns nothing
    //privacy 3: returns first_name, last_name, profile_privacy
    //privacy 2&1 or friend: return first_name, last_name, profile_picture, profile_privacy
    fun getUser(currentUserId: Int, userId: Int): ResultSet {
        val query = "CALL getUserById($currentUserId, $userId);"
        val statement = connection.prepareCall(query)
        return statement.executeQuery()
    }

    //Load gift feed when on home screen
    //Loads all open gifts the user is a member of
    //Returns gift_id, title, price, gift_picture, user_first_name, user_last_name,
    // owner_first_name, owner_last_name
    fun getGiftFeedByMemberId(memberId: Int): ResultSet {
        /*val query = "SELECT g.id, g.title, g.price, g.gift_picture, " +
                "u.first_name AS user_first_name, u.last_name AS user_last_name, " +
                "o.first_name AS owner_first_name, o.last_name AS owner_last_name " +
                "FROM gifts AS g " +
                "LEFT JOIN users AS o ON g.owner_id=o.id " +
                "LEFT JOIN users AS u ON g.user_id = u.id " +
                "LEFT JOIN members AS m ON g.id=m.gift_id " +
                "WHERE m.user_id = $memberId AND g.is_closed=0;"*/
        val query = "SELECT g.id, g.title, g.price, g.owner_id, g.is_wish, " +
                "g.post_privacy, g.gift_picture, g.is_closed, " +
                "o.first_name AS owner_first_name, o.last_name AS owner_last_name, " +
                "(SELECT COUNT(*) FROM members WHERE gift_id=g.id) AS member_count, " +
                "SUM(l.likes) AS likes, " +
                "(SELECT id FROM members WHERE user_id=$memberId AND gift_id=g.id) AS member_id, " +
                "(SELECT likes FROM likes WHERE gift_id=g.id AND user_id=$memberId LIMIT 1) AS isLiked " +
                "FROM gifts AS g " +
                "LEFT JOIN users AS o ON g.owner_id=o.id " +
                "LEFT JOIN likes AS l ON g.id=l.gift_id " +
                "LEFT JOIN members AS m ON g.id=m.gift_id " +
                "WHERE m.user_id = $memberId AND " +
                "(post_privacy=0 OR post_privacy=1 OR post_privacy=2 AND EXISTS( " +
                "SELECT * FROM friends WHERE user_id=$memberId AND friend_id=g.owner_id)) " +
                "GROUP BY g.id " +
                "ORDER BY g.is_closed DESC, likes DESC;"
        val statement = connection.prepareStatement(query)
        return statement.executeQuery()

    }

    //Load gift feed when on friends profile page
    //Loads all public gifts or private gifts of friends
    //Returns gift_id, title, price, owner_id, is_wish, post_privacy, gift_picture, is_closed,
    // owner_first_name, owner_last_name, member_count, likes
    fun getGiftFeedByUserId(currentUserId: Int, userId: Int): ResultSet {
        val query = "SELECT g.id, g.title, g.price, g.owner_id, g.is_wish, " +
                "g.post_privacy, g.gift_picture, g.is_closed, " +
                "o.first_name AS owner_first_name, o.last_name AS owner_last_name, " +
                "(SELECT COUNT(*) FROM members WHERE gift_id=g.id) AS member_count, " +
                "SUM(l.likes) AS likes, " +
                "(SELECT id FROM members WHERE user_id=$currentUserId AND gift_id=g.id) AS member_id, " +
                "(SELECT likes FROM likes WHERE gift_id=g.id AND user_id=$currentUserId LIMIT 1) AS isLiked, " +
                "g.user_id FROM gifts AS g " +
                "LEFT JOIN users AS o ON g.owner_id=o.id " +
                "LEFT JOIN likes AS l ON g.id=l.gift_id " +
                "WHERE g.user_id =$userId AND " +
                "(post_privacy=0 OR post_privacy=1 OR post_privacy=2 AND EXISTS( " +
                "SELECT * FROM friends WHERE user_id=$currentUserId AND friend_id=g.owner_id)) " +
                "GROUP BY g.id " +
                "ORDER BY g.is_closed DESC, likes DESC;"
        val statement = connection.prepareStatement(query)
        return statement.executeQuery()

    }

    //Get gift when clicking on giftfeed on profile page
    fun getGiftById(userId: Int, giftId: Int): ResultSet {
        val query = "SELECT g.id, g.title, g.price, g.owner_id, g.user_id, g.is_wish, " +
                "g.post_privacy, g.gift_link, g.gift_picture, g.is_closed, " +
                "o.first_name AS owner_first_name, o.last_name AS owner_last_name, " +
                "u.first_name AS user_first_name, u.last_name AS user_last_name, " +
                "(SELECT COUNT(*) FROM members WHERE gift_id=g.id) AS member_count, " +
                "(SELECT id FROM members WHERE user_id=$userId AND gift_id=g.id) AS member_id " +
                "FROM gifts AS g " +
                "LEFT JOIN users AS o ON g.owner_id=o.id " +
                "LEFT JOIN users AS u ON g.user_id=u.id " +
                "WHERE g.id =$giftId; "
        val statement = connection.prepareStatement(query)
        return statement.executeQuery()
    }

    //Update gift as owner
    fun updateGift(
        giftId: Int?,
        title: String,
        price: Int,
        userId: Int,
        ownerId: Int,
        link: String,
        privacy: Int
    ) {
        val query: String = if (giftId != null) {
            "UPDATE gifts SET title='$title', price=$price, user_id=$userId, owner_id=$ownerId, gift_link='$link', post_privacy=$privacy WHERE id=$giftId;"
        } else {
            "INSERT INTO gifts (title, price, user_id, owner_id, gift_link, post_privacy) VALUES ('$title', $price, $userId, $ownerId, '$link', $privacy);"
        }
        val statement = connection.prepareStatement(query)
        statement.executeUpdate()
    }

    //Load members on gift page
    //Returns membership_id, first_name, last_name, max_price, is_fixed
    //No implemented privacy check
    fun getMembers(giftId: Int): ResultSet {
        val query = "SELECT m.id, u.first_name, u.last_name, m.max_price, m.is_fixed " +
                "FROM members as m " +
                "LEFT JOIN users AS u ON u.id=m.user_id " +
                "WHERE m.gift_id=$giftId;"
        val statement = connection.prepareStatement(query)
        return statement.executeQuery()
    }

    //Load comments on gift page
    //Returns comment_id, first_name, last_name, content, likes
    //No implemented privacy check
    fun getComments(giftId: Int): ResultSet {
        val query = "SELECT c.id AS comment_id, u.first_name, u.last_name, c.content, " +
                "SUM(l.likes) AS likes FROM comments AS c " +
                "LEFT JOIN users AS u ON u.id=c.user_id " +
                "LEFT JOIN likes AS l ON l.comment_id=c.id " +
                "WHERE c.gift_id=$giftId " +
                "GROUP BY c.id " +
                "ORDER BY likes DESC;"
        val statement = connection.prepareStatement(query)
        return statement.executeQuery()
    }

    fun likeComment(userId: Int, commentId: Int, likes: Int): ResultSet {
        val query = "CALL applyLikeToComment($userId, $commentId, $likes);"
        val statement = connection.prepareCall(query)
        return statement.executeQuery()
    }

    //Up or downvote gift idea on profile screen
    fun likeGift(userId: Int, giftId: Int, likes: Int): ResultSet {
        val query = "CALL applyLikeToGift($userId, $giftId, $likes);"
        val statement = connection.prepareCall(query)
        return statement.executeQuery()
    }

    fun insertComment(userId: Int, giftId: Int, comment: String) {
        val query = "INSERT INTO comments(user_id, gift_id, content) " +
                "VALUES ($userId, $giftId, $comment;"
        val statement = connection.prepareStatement(query)
        statement.executeUpdate()
    }

    fun removeComment(commentId: String) {
        val query = "DELETE FROM comments WHERE comment_id=$commentId;"
        val statement = connection.prepareStatement(query)
        statement.executeUpdate()
    }

    //Add friend on profile page
    fun addFriend(friendUserId: Int, userId: Int) {
        val query = "INSERT INTO friends (friend_id, user_id) " +
                "VALUES ($friendUserId, $userId);"
        val statement = connection.prepareStatement(query)
        statement.executeUpdate()
    }

    //Remove friend on profile page
    fun removeFriend(friendUserId: Int, userId: Int) {
        val query = "DELETE FROM friends WHERE friend_id=$friendUserId AND user_id=$userId;"
        val statement = connection.prepareStatement(query)
        statement.executeUpdate()
    }

    fun updateFavourite(friendId: Int, isFavourite: Int) {
        val query = "UPDATE friends SET is_favourite=$isFavourite WHERE id=$friendId AND;"
        val statement = connection.prepareStatement(query)
        statement.executeUpdate()
    }

    fun deleteGift(currentUserId: Int, giftId: Int) {
        val query = "DELETE FROM gifts WHERE owner_id=$currentUserId AND id=$giftId;"
        val statement = connection.prepareStatement(query)
        statement.executeUpdate()
    }

    //Join gift idea
    fun joinGift(userId: Int, giftId: Int): ResultSet {
        val query = "CALL joinGift($userId, $giftId);"
        val statement = connection.prepareCall(query)
        return statement.executeQuery()
    }

    //Leave gift idea
    fun leaveGift(memberId: Int) {
        val query = "DELETE FROM members WHERE id=$memberId;"
        val statement = connection.prepareStatement(query)
        statement.executeUpdate()
    }

    //Edit own user data on settings page
    fun editUser(
        userId: Int, firstName: String, lastName: String, dateOfBirth: LocalDate,
        email: String, profilePrivacy: Int, profilePicture: String
    ): ResultSet {
        val query =
            "UPDATE db.users SET first_name=\"$firstName\", last_name=\"$lastName\"," +
                    " date_of_birth=\"$dateOfBirth\", email=\"$email\", profile_privacy=$profilePrivacy, profile_picture=\"$profilePicture\"" +
                    " WHERE id=$userId;"
        val statement = connection.prepareStatement(query)
        statement.executeUpdate()

        val query2 =
            "SELECT id, first_name, last_name, date_of_birth, email, profile_privacy, profile_picture FROM db.users WHERE id=$userId;"
        val statement2 = connection.prepareStatement(query2)
        return statement2.executeQuery()
    }

    //Check if email exists on register page
    fun checkIfEmailExistsOnOtherUser(userId: Int, email: String): Boolean {
        val query =
            "SELECT u.id FROM users AS u WHERE u.email = \"$email\" AND u.id != $userId;"
        val statement = connection.prepareStatement(query)
        statement.execute()
        val result: ResultSet = statement.resultSet
        return result.next()
    }

    fun deleteAccount(userId: Int) {
        val query = "DELETE FROM users WHERE users.id = $userId;"
        val statement = connection.prepareStatement(query)
        statement.execute()
    }

    fun getNotificationFeed(userId: Int): ResultSet {
        val query = "SELECT n.id, n.notification_type, u.first_name, u.last_name, " +
                "g.title AS gift_title, n.friend_id, n.gift_id, o.first_name, o.last_name FROM notifications AS n  " +
                "LEFT JOIN users AS u ON u.id = n.friend_id " +
                "LEFT JOIN gifts AS g ON g.id = n.gift_id  " +
                "LEFT JOIN users AS o ON o.id = g.user_id " +
                "WHERE n.user_id=$userId ORDER BY n.id DESC;"
        val statement = connection.prepareStatement(query)
        statement.execute()
        return statement.resultSet
    }

    fun removeNotificationById(notificationId: Int) {
        val query = "DELETE FROM notifications WHERE id=$notificationId;"
        val statement = connection.prepareStatement(query)
        statement.executeUpdate()
    }

    fun removeAllNotifications(userId: Int) {
        val query = "DELETE FROM notifications WHERE user_id=$userId;"
        val statement = connection.prepareStatement(query)
        statement.executeUpdate()
    }

    fun getNotificationCount(userId: Int): Int {
        val query = "SELECT COUNT(*) FROM notifications WHERE user_id=$userId;"
        val statement = connection.prepareStatement(query)
        statement.execute()
        val result: ResultSet = statement.resultSet
        result.next()
        return result.getInt(1)
    }

    fun addNotification(notificationId: Int, userId: Int, friendId: Int, giftId: Int?=null) {
        val query = "INSERT INTO notifications (notification_type, user_id, friend_id, gift_id) " +
                "VALUES($notificationId, $userId, $friendId, $giftId);"
        val statement = connection.prepareStatement(query)
        statement.executeUpdate()
    }

    fun getNotificationId(notificationId: Int, userId: Int, friendId: Int, giftId: Int?=null): Int {
        var query = "SELECT id FROM notifications WHERE notification_type=$notificationId " +
                "AND user_id=$userId AND friend_id=$friendId"
        if (giftId!=null) query += " AND gift_id=$giftId"
        query += ";"
        val statement = connection.prepareStatement(query)
        statement.execute()
        val result: ResultSet = statement.resultSet
        return if (result.next()) {
            result.getInt(1)
        } else {
            0
        }
    }

    fun notifiyAll(notificationId: Int, userId: Int, giftId: Int) {
        val query = "INSERT INTO notifications(notification_type, user_id, friend_id, gift_id) " +
                "SELECT $notificationId, user_id, $userId, $giftId FROM members WHERE gift_id = $giftId " +
                "AND NOT user_id = $userId;"
        val statement = connection.prepareStatement(query)
        statement.executeUpdate()
    }

}
