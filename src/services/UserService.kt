package com.galaktionov.services

import com.galaktionov.dto.PostResponseDto.Companion.fromModel
import com.galaktionov.dto.UserResponseDto
import com.galaktionov.model.UserModel
import com.galaktionov.model.UserRepository
import io.ktor.features.NotFoundException
import org.springframework.security.crypto.password.PasswordEncoder

class UserService(
    private val repo: UserRepository,
    private val tokenService: JWTTokenService,
    private val passwordEncoder: PasswordEncoder
) {
    suspend fun getModelById(id: Long): UserModel? {
        return repo.getById(id)
    }
    suspend fun getById(id: Long): UserResponseDto {
        val model = repo.getById(id) ?: throw NotFoundException()
        return UserResponseDto.fromModel(model)
    }
//    suspend fun changePassword(id: Long, input: PasswordChangeRequestDto) {
//// TODO: handle concurrency
//        val model = repo.getById(id) ?: throw NotFoundException()
//        if (!passwordEncoder.matches(input.old, model.password)) {
//            throw PasswordChangeException("Wrong password!")
//        }
//        val copy = model.copy(password = passwordEncoder.encode(input.new))
//        repo.save(copy)
//    }
}