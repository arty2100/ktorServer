package com.galaktionov.dto

import com.galaktionov.model.MediaModel

data class MediaResponseDto(val id: String, val mediaType: MediaModel.MediaType) {
    companion object {
        fun fromModel(model: MediaModel) = MediaResponseDto(
                id = model.id,
                mediaType = model.mediaType
        )
    }
}