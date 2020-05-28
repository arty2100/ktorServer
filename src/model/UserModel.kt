package com.galaktionov.model

import io.ktor.auth.Principal

data class UserModel(
    val id: Long?,
    val username: String,
    val password: String
): Principal
interface UserRepository {
    suspend fun getAll(): List<UserModel>
    suspend fun getById(id: Long): UserModel?
    suspend fun getByIds(ids: Collection<Long>): List<UserModel>
    suspend fun getByUsername(username: String): UserModel?
    suspend fun save(item: UserModel): UserModel
}