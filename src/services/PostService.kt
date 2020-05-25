package com.galaktionov.services

import com.galaktionov.dto.PostRequestDto
import com.galaktionov.dto.PostResponseDto
import com.galaktionov.firstandroidapp.dto.PostModel
import com.galaktionov.repository.PostRepository
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.features.NotFoundException
import io.ktor.features.ParameterConversionException
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.sync.withLock

class PostService(private val repo: PostRepository) {
    suspend fun getAll(): List<PostResponseDto> {
        return repo.getAll().map {
            PostResponseDto.fromModel(it)
        }
    }

    suspend fun getById(id: Long): PostModel {
        return repo.getById(id) ?: throw NotFoundException()
    }

    suspend fun save(input: PostRequestDto, author: String): PostResponseDto {

        val model = PostModel(input.id, input.author, input.content, input.created, input.likedByMe, input.dislikedByMe, input.likes, input.comments, input.shares, input.address, input.location, input.videoUrl, input.postTpe, input.advLink, input.companyImg, 0, mutableListOf())

        return PostResponseDto.fromModel(repo.save(model, author))
    }

    suspend fun addViews(item: PostModel, userId: String): PostResponseDto {

        return PostResponseDto.fromModel(repo.addViews(item, userId))
    }

    suspend fun remove(item: PostModel) {

        repo.remove(item)
    }

    suspend fun like(item: PostModel): PostResponseDto = PostResponseDto.fromModel(repo.like(item))

    suspend fun dislike(item: PostModel): PostResponseDto = PostResponseDto.fromModel(repo.dislike(item))

    suspend fun repost(item: PostModel): PostResponseDto = PostResponseDto.fromModel(repo.repost(item))
}

suspend fun PipelineContext<Unit, ApplicationCall>.checkIdAndModel(postService: PostService): PostModel {
    val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException("id", "Long")
    return postService.getById(id)
}