package com.galaktionov.repository

import com.galaktionov.firstandroidapp.dto.PostModel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PostMutexRepository() : PostRepository {
    private val posts = mutableListOf<PostModel>()
    private var nextId = 1L
    val mutex = Mutex()
    override suspend fun getAll(): List<PostModel> = mutex.withLock {
        posts
    }

    override suspend fun getById(id: Long): PostModel? = mutex.withLock {
        posts.find { it.id == id }
    }


    override suspend fun save(item: PostModel): PostModel =
        mutex.withLock {
            when (val index = posts.indexOfFirst { it.id == item.id }) {
                -1 -> {
                    val copy = item.copy(id = nextId++)
                    posts.add(copy)
                    copy
                }
                else -> {
                    posts[index] = item
                    item
                }
            }
        }

    override suspend fun removeById(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun likeById(id: Long): PostModel? {
        TODO("Not yet implemented")
    }

    override suspend fun dislikeById(id: Long): PostModel? {
        TODO("Not yet implemented")
    }
}