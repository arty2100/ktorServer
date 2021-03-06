package com.galaktionov.services

import com.galaktionov.dto.PasswordChangeRequestDto
import com.galaktionov.dto.UserRegistrationRequestDto
import com.galaktionov.dto.UserResponseDto
import com.galaktionov.exception.PasswordChangeException
import com.galaktionov.model.UserModel
import com.galaktionov.model.UserRepository
import io.ktor.features.NotFoundException
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName
import org.springframework.security.crypto.password.PasswordEncoder

class UserService(
        private val repo: UserRepository,
        private val tokenService: JWTTokenService,
        private val passwordEncoder: PasswordEncoder
) {
    suspend fun getModelById(id: Long): UserModel? {
        return repo.getById(id)
    }

    suspend fun getByUsername(username: String): UserModel? =
            repo.getByUsername(username)


    private suspend fun save(input: UserRegistrationRequestDto): UserModel = repo.save(UserModel(null, input.username, passwordEncoder.encode(input.password)))

    suspend fun register(input: UserRegistrationRequestDto): UserResponseDto {

        if (getByUsername(input.username) != null) {
            throw DuplicateName("Duplicated user!")
        } else {

            val model = save(input)
            val token = tokenService.generate(model.id!!)
            return UserResponseDto.fromModel(token)
        }
    }

    suspend fun authenticate(input: UserRegistrationRequestDto): UserResponseDto {
        val model = repo.getByUsername(input.username) ?: throw NotFoundException()
        if (!passwordEncoder.matches(input.password, model.password)) {
            throw PasswordChangeException("Wrong password!")
        }
        val token = tokenService.generate(model.id!!)
        return UserResponseDto.fromModel(token)
    }

    suspend fun changePassword(input: PasswordChangeRequestDto) {

        val model = repo.getById(input.id) ?: throw NotFoundException()
        if (!passwordEncoder.matches(input.old, model.password)) {
            throw PasswordChangeException("Wrong password!")
        }
        val copy = model.copy(password = passwordEncoder.encode(input.new))
        repo.save(copy)
    }
}