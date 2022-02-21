package com.example.geschenkapp.fragments

import androidx.room.*

@Dao
public interface MainDao {

    @Query("SELECT * FROM users WHERE id=:userId;")
    User loadUserById(int userId);

    @Query("SELECT u.first_name AS user_first_name, u.last_name AS user_last_name," +
            "o.first_name AS owner_first_name, o.last_name AS owner_last_name, g.*," +
            "(SELECT COUNT(*) FROM members WHERE gift_id=g.id) AS member_count" +
            "FROM gifts as g" +
            "LEFT JOIN users AS u ON g.user_id=u.id" +
            "LEFT JOIN users AS o ON g.owner_id=u.id" +
            "WHERE u.id = :userId;")
    List<Gift> loadGiftsByUserId(int userId);

    @Query("SELECT f.id, u.first_name, u.last_name, f.friend_id, u.date_of_birth, f.is_favourite" +
            "timestampdiff(YEAR, u.date_of_birth, CURDATE()) AS age," +
            "(SELECT COUNT(*) FROM gifts WHERE user_id=f.friend_id) AS count_gifts," +
            "IF(DAYOFYEAR(u.date_of_birth)-DAYOFYEAR(NOW()) < 0, DAYOFYEAR(u.date_of_birth)-DAYOFYEAR(NOW()) + TIMESTAMPDIFF(DAY, NOW(), ADDDATE(NOW(), INTERVAL 1 YEAR)), DAYOFYEAR(u.date_of_birth)-DAYOFYEAR(NOW())) AS remaining" +
            "FROM friends AS f \n" +
            "LEFT JOIN users AS u on f.friend_id=u.id\n" +
            "WHERE f.user_id = :userId\n" +
            "ORDER BY remaining ASC;")
    List<HomeItem> getFriendsByUserId(int userId);

    @Insert("INSERT INTO friends(user_id, friend_id) VALUES(:userId, :friendId;")
    void addFriendById(Int userId, Int friendId);

    @Delete("DELETE FROM friends WHERE id=:friendId;")
    void removeFriendById(Int friendId);

    @Update("UPDATE friends SET is_favourite=:isFavourite WHERE id=:friendId;")
    void updateFavouriteById(Int friendId, Int isFavourite);

    @Insert("INSERT INTO comments(content, user_id, gift_id) VALUES(:content, :userId, :giftId);")
    void addComment(Int userId, Int giftId, String content);
}