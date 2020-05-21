package com.galaktionov.repository

import com.galaktionov.model.UserModel
import com.galaktionov.model.UserRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class UserRepositoryInMemoryWithMutexImpl : UserRepository {
    private var nextId = 1L
    private val items = mutableListOf<UserModel>()
    private val mutex = Mutex()
    override suspend fun getAll(): List<UserModel> {
        mutex.withLock {
            return items.toList()
        }
    }
    override suspend fun getById(id: Long): UserModel? {
        mutex.withLock {
            return items.find { it.id == id }
        }
    }
    override suspend fun getByIds(ids: Collection<Long>): List<UserModel> {
        mutex.withLock {
            return items.filter { ids.contains(it.id) }
        }
    }
    override suspend fun getByUsername(username: String): UserModel? {
        mutex.withLock {
            return items.find { it.username == username }
        }
    }

    override suspend fun save(item: UserModel): UserModel {
        TODO("Not yet implemented")
    }
}