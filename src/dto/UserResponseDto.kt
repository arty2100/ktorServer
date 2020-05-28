package com.galaktionov.dto

import com.galaktionov.model.UserModel

data class UserResponseDto(val token: String) {

    companion object {
        fun fromModel(token: String) = UserResponseDto(
                token = token
        )
    }
}