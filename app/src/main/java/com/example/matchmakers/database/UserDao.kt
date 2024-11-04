package com.example.actiontabskotlin.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import com.example.matchmakers.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    // Placeholder for fetching data, with optional filtering
    @Query("SELECT * FROM user_table WHERE interest = :interestFilter")
    fun getUsersByInterest(interestFilter: String): Flow<List<User>>

    // Placeholder for fetching all data
    @Query("SELECT * FROM user_table")
    fun getAllUsers(): Flow<List<User>>

    // Placeholder for inserting data
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    // Placeholder for deleting a user
    @Delete
    suspend fun deleteUser(user: User)
}
