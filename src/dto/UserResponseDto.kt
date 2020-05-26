package com.galaktionov.dto

import com.galaktionov.model.UserModel

data class UserResponseDto(val id: Long, val token :String ) {

    companion object {
        fun fromModel(user: UserModel) = UserResponseDto(
            id = user.id!!, token = user.token!!
        )
    }
}