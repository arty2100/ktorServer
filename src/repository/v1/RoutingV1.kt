package com.galaktionov.repository.v1

import com.galaktionov.dto.PasswordChangeRequestDto
import com.galaktionov.dto.PostRequestDto
import com.galaktionov.dto.PostSearchRequestDto
import com.galaktionov.dto.UserRegistrationRequestDto
import com.galaktionov.services.PostService
import com.galaktionov.services.UserService
import com.galaktionov.services.checkIdAndModel
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*

class RoutingV1(private val postService: PostService, private val userService: UserService) {
    fun setup(configuration: Routing) {
        with(configuration) {

            route("/api/v1/users") {
                post("/registration") {
                    val input = call.receive<UserRegistrationRequestDto>()
                    call.respond(userService.register(input))
                }
                post("/authenticate") {
                    val input = call.receive<UserRegistrationRequestDto>()
                    call.respond(userService.authenticate(input))
                }
                post("/changePassword") {
                    val input = call.receive<PasswordChangeRequestDto>()
                    call.respond(userService.changePassword(input))
                }
            }

            route("/api/v1/posts") {
                get {
                    val response = postService.getAll()
                    call.respond(response)
                }
                post("/findById") {
                    val request = call.receive<PostSearchRequestDto>()
                    val model = postService.getById(request.id)
                    val userId = request.userId
                    val response = postService.addViews(model, userId)
                    call.respond(response)
                }
                post {
                    val request = call.receive<PostRequestDto>()
                    val model = postService.save(request)
                    call.respond(model)
                }
                post("/like/{id}") {
                    val model = checkIdAndModel(postService)
                    call.respond(postService.like(model))
                }
                post("/dislike/{id}") {
                    val model = checkIdAndModel(postService)
                    call.respond(postService.dislike(model))
                }
                post("/repost/{id}") {
                    val model = checkIdAndModel(postService)
                    call.respond(postService.repost(model))
                }
                delete("/{id}") {
                    val model = checkIdAndModel(postService)
                    postService.remove(model)
                    call.respondText("Post has been deleted ")
                }
            }
        }
    }
}
