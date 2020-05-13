package com.galaktionov.firstandroidapp.dto

import com.sun.jndi.toolkit.url.Uri

data class PostModel(
    val id: Long? = null,
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
    val postTpe: POST_TYPE,
    val advLink: String? = null,
    val companyImg: String? = null
) {
    enum class POST_TYPE {
        VIDEO, TEXT, REPLY, EVENT, ADV
    }
}



