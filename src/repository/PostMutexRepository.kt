package com.galaktionov.repository

import com.galaktionov.firstandroidapp.dto.PostModel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PostMutexRepository : PostRepository {
    private val posts = mutableListOf<PostModel>()
    private var nextId = 1L
    private val mutex = Mutex()
    override suspend fun getAll(): List<PostModel> = mutex.withLock {
        posts
    }

    override suspend fun getById(id: Long): PostModel? = mutex.withLock {
        posts.find { it.id == id }
    }


    override suspend fun save(item: PostModel, author: String?): PostModel =
            mutex.withLock {
                when (val index = posts.indexOfFirst { it.id == item.id }) {
                    -1 -> {
                        val copy = item.copy(id = nextId++)
                        posts.add(copy)
                        copy
                    }
                    else -> {
                        if (author == item.author || author==null) {
                            posts[index] = item
                            item
                        } else {
                            TODO()
                        }

                    }
                }
            }

    override suspend fun remove(item: PostModel) {

        mutex.withLock {
            posts.remove(item)
        }
    }

    override suspend fun like(item: PostModel): PostModel = mutex.withLock {

        val newItem: PostModel

        if (!item.likedByMe) {
            val likes = if (item.dislikedByMe) {
                item.likes + 2
            } else {
                item.likes.inc()
            }
            newItem = item.copy(likes = likes, likedByMe = true, dislikedByMe = false)

        } else {
            newItem = item.copy(likes = item.likes.dec(), likedByMe = false)


        }
        posts[posts.indexOf(item)] = newItem
        newItem

    }

    override suspend fun dislike(item: PostModel): PostModel = mutex.withLock {

        val newItem: PostModel

        if (!item.dislikedByMe) {
            val likes = if (item.likedByMe) item.likes - 2 else item.likes.dec()

            newItem = item.copy(likes = likes, dislikedByMe = true, likedByMe = false)

        } else {
            newItem = item.copy(likes = item.likes.inc(), dislikedByMe = false)


        }
        posts[posts.indexOf(item)] = newItem
        newItem
    }

    override suspend fun repost(item: PostModel): PostModel = mutex.withLock {

        val copy = item.copy(id = nextId++, postTpe = PostModel.POST_TYPE.REPOST)
        posts.add(copy)
        copy


    }

    override suspend fun addViews(item: PostModel, userId: String): PostModel = mutex.withLock {

        val newItem = if (!item.userIdList.contains(userId)) {
            val newList = item.userIdList.toMutableList().apply {
                add(userId)
            }
            item.copy(views = item.views.inc(), userIdList = newList)
        } else {
            item.copy()
        }
        posts[posts.indexOf(item)] = newItem
        newItem
    }
}