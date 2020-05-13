package com.galaktionov.dto

import com.galaktionov.firstandroidapp.dto.LocationModel
import com.galaktionov.firstandroidapp.dto.PostModel

data class PostRequestDto(
        val author: String,
        val content: String? = null,
        val created: Long,//milliseconds
        var likedByMe: Boolean = false,
        var likes: Int = 0,
        var comments: Int = 0,
        var shares: Int = 0,
        val address: String? = null,
        val location: LocationModel? = null,
        val videoUrl: String? = null,
        val postTpe: PostModel.POST_TYPE,
        val advLink: String? = null,
        val companyImg: String? = null
)