//Dependency Injection
package com.example.matchmakers.di

import android.content.Context
import androidx.room.Room
import com.example.matchmakers.database.UserDatabase
import com.example.actiontabskotlin.database.UserDao
import org.koin.dsl.module

// Koin module for database setup and access
val databaseModule = module {

    // Provides a singleton instance of the UserDatabase
    single { provideDatabase(get()) }

    // Provides a singleton instance of UserDao, derived from UserDatabase
    single { provideUserDao(get()) }
}

// Function to create and provide a Room Database instance
fun provideDatabase(context: Context): UserDatabase {
    return Room.databaseBuilder(
        context, // The application context
        UserDatabase::class.java, // The Room database class
        "user_database" // The name of the database file
    ).build()
}

// Function to provide a UserDao instance from the database
fun provideUserDao(database: UserDatabase): UserDao {
    return database.userDao() // Retrieves the DAO for accessing User data
}