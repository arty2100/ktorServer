package com.galaktionov.firstandroidapp.dto

data class PostModel(
        val id: Long? = null,
        val author: String,
        val content: String? = null,
        val created: Long,//milliseconds
        val likedByMe: Boolean = false,
        val dislikedByMe: Boolean = false,
        val likes: Int = 0,
        val comments: Int = 0,
        val shares: Int = 0,
        val address: String? = null,
        val location: LocationModel? = null,
        val videoUrl: String? = null,
        val postTpe: POST_TYPE,
        val advLink: String? = null,
        val companyImg: String? = null,
        var views: Int = 0,
        val userIdList: MutableList<Long>
) {
    enum class POST_TYPE {
        VIDEO, TEXT, REPOST, EVENT, ADV
    }
}
