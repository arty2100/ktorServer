package com.galaktionov.dto

import com.galaktionov.firstandroidapp.dto.PostModel

data class PostResponseDto(
    val id: Long,
    val author: String,
    val content: String? = null
) {
    companion object {
        fun fromModel(model: PostModel) = PostResponseDto(
            id = model.id,
            author = model.author,
            content = model.content
        )
    }
}