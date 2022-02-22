package com.example.geschenkapp.fragments

import androidx.room.*
import com.example.geschenkapp.data.*
//import java.util.*

@Dao
interface MainDao {
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

    //Load gift feed when on home screen
    //Loads all open gifts the user is a member of
    //Returns gift_id, title, price, gift_picture, user_first_name, user_last_name, owner_first_name, owner_last_name
    @Query("SELECT g.id, g.title, g.price, g.gift_picture, \n" +
            "u.first_name AS user_first_name, u.last_name AS user_last_name, o.first_name AS owner_first_name, o.last_name AS owner_last_name\n" +
            "FROM gifts AS g \n" +
            "LEFT JOIN users AS o ON g.owner_id=o.id \n" +
            "LEFT JOIN users AS u ON g.user_id = u.id \n" +
            "LEFT JOIN members AS m ON g.id=m.gift_id \n" +
            "WHERE m.user_id = :memberId AND g.is_closed=0;")
    fun getGiftsByMemberId(memberId: Int): List<Gift>

    //Load gift feed when on friends profile page
    //Loads all public gifts or private gifts of friends
    //Returns gift_id, title, price, owner_id, is_wish, post_privacy, gift_picture, is_closed, owner_first_name, owner_last_name, member_count, likes
    @Query("SELECT g.id, g.title, g.price, g.owner_id, g.is_wish, g.post_privacy, g.gift_picture, g.is_closed,\n" +
            "o.first_name AS owner_first_name, o.last_name AS owner_last_name, \n" +
            "(SELECT COUNT(*) FROM members WHERE gift_id=g.id) AS member_count, \n" +
            "SUM(l.likes) AS likes\n" +
            "FROM gifts AS g \n" +
            "LEFT JOIN users AS o ON g.owner_id=o.id \n" +
            "LEFT JOIN likes AS l ON g.id=l.gift_id \n" +
            "WHERE g.user_id = :userId AND \n" +
            "(post_privacy=0 OR post_privacy=1 OR post_privacy=2 AND EXISTS(SELECT * FROM friends WHERE user_id=:accessId AND friend_id=g.owner_id))\n" +
            "GROUP BY g.id \n" +
            "ORDER BY g.is_closed DESC, likes DESC;")
    fun getGiftsByUserId(accessId: Int, userId: Int): List<Gift>

    //Load members on gift page
    //Returns membership_id, first_name, last_name, max_price, is_fixed
    //No implemented privacy check
    @Query("SELECT m.id, u.first_name, u.last_name, m.max_price, m.is_fixed FROM members as m \n" +
            "LEFT JOIN users AS u ON u.id=m.user_id \n" +
            "WHERE m.gift_id= :giftId;")
    fun getMembersByGiftId(giftId: Int): List<Member>

    //Load comments on gift page
    //Returns comment_id, first_name, last_name, content, likes
    //No implemented privacy check
    @Query("SELECT c.id AS comment_id, u.first_name, u.last_name, c.content, SUM(l.likes) AS likes FROM comments AS c\n" +
            "LEFT JOIN users AS u ON u.id=c.user_id\n" +
            "LEFT JOIN likes AS l ON l.comment_id=c.id\n" +
            "WHERE c.gift_id= :giftId\n" +
            "GROUP BY c.id\n" +
            "ORDER BY likes DESC;")
    fun getCommentsByGiftId(giftId: Int): List<Comment>

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

    //@Query("CALL applyLikeToComment(userId, commentId, likes)")
    //fun applyLikeByCommentId(userId: Int, commentId: Int, likes: Int)

    //@Query("CALL applyLikeToGift(userId, giftId, likes)")
    //fun applyLikeByGiftId(userId: Int, giftId: Int, likes: Int)
}