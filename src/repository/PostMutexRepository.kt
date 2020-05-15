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

    override suspend fun remove(model: PostModel) {

        mutex.withLock {
            posts.remove(model)
        }
    }

    override suspend fun like(item: PostModel): PostModel = mutex.withLock {

        if (!item.likedByMe) {
            if (item.dislikedByMe) item.likes + 2 else item.likes++
            item.likedByMe = true
            item.dislikedByMe = false
        } else {
            item.likes--
            item.likedByMe = false
        }
        item
    }

    override suspend fun dislike(item: PostModel): PostModel = mutex.withLock {

        if (!item.dislikedByMe) {
            if (item.likedByMe) item.likes - 2 else item.likes--
            item.dislikedByMe = true
            item.likedByMe = false
        } else {
            item.likes++
            item.dislikedByMe = false
        }
        item
    }

    override suspend fun repost(item: PostModel): PostModel = mutex.withLock {

        val copy = item.copy(id = nextId++, postTpe = PostModel.POST_TYPE.REPOST)
        posts.add(copy)
        copy
    }

}