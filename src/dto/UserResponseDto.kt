package com.galaktionov.dto

import com.galaktionov.firstandroidapp.dto.PostModel
import com.galaktionov.model.UserModel

data class UserResponseDto(val id: Long, val username: String, val password: String) {

    companion object {
        fun fromModel(user: UserModel) = UserResponseDto(
            id = user.id, username = user.username, password = user.password
        )
    }
}