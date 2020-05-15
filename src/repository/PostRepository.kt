package com.galaktionov.repository

import com.galaktionov.firstandroidapp.dto.PostModel

interface PostRepository {
    suspend fun getAll(): List<PostModel>
    suspend fun getById(id: Long): PostModel?
    suspend fun save(item: PostModel): PostModel
    suspend fun remove(item: PostModel)
    suspend fun like(item: PostModel): PostModel
    suspend fun dislike(item: PostModel): PostModel
    suspend fun repost(item: PostModel): PostModel

}