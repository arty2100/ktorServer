package com.galaktionov.dto

import com.galaktionov.firstandroidapp.dto.Location
import com.galaktionov.firstandroidapp.dto.PostModel

data class PostResponseDto(
    val id: Long,
    val author: String,
    val content: String? = null,
    val created: Long,//milliseconds
    var likedByMe: Boolean = false,
    var likes: Int = 0,
    var comments: Int = 0,
    var shares: Int = 0,
    val address: String? = null,
    val location: Location? = null,
    val videoUrl: String? = null,
    val postTpe: PostModel.POST_TYPE,
    val advLink: String? = null,
    val companyImg: String? = null
) {
    companion object {
        fun fromModel(model: PostModel) = PostResponseDto(
            id = model.id,
            author = model.author,
            content = model.content,
            created = model.created,
            likedByMe = model.likedByMe,
            likes = model.likes,
            comments = model.comments,
            shares = model.shares,
            address = model.address,
            location = model.location,
            videoUrl = model.videoUrl,
            postTpe = model.postTpe,
            advLink = model.advLink,
            companyImg = model.companyImg
        )
    }
}