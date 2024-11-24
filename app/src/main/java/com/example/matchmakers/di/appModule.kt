package com.example.matchmakers.di

import com.example.matchmakers.domain.UserService
import com.example.matchmakers.repository.LocalUserRepository
import com.example.matchmakers.repository.RemoteUserRepository
import org.koin.dsl.module

val appModule = module {
    single { LocalUserRepository(get()) } // Requires UserDao, provided in DatabaseModule
    single { RemoteUserRepository() }
    single { UserService(get(), get()) } // Requires LocalUserRepository & RemoteUserRepository
}
