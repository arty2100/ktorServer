package com.galaktionov.repository.v1

import com.galaktionov.dto.PasswordChangeRequestDto
import com.galaktionov.dto.PostRequestDto
import com.galaktionov.dto.PostSearchRequestDto
import com.galaktionov.dto.UserRegistrationRequestDto
import com.galaktionov.exception.AuthFailException
import com.galaktionov.model.UserModel
import com.galaktionov.services.FileService
import com.galaktionov.services.PostService
import com.galaktionov.services.UserService
import com.galaktionov.services.checkIdAndModel
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.request.receive
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*

class RoutingV1(private val postService: PostService, private val userService: UserService, private val fileService: FileService) {
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
            authenticate {

                route("/api/v1/media") {
                    post {
                        val multipart = call.receiveMultipart()
                        val response = fileService.save(multipart)
                        call.respond(response)
                    }
                }


                route("/api/v1/posts") {
                    get {
                        val response = postService.getAll()
                        call.respond(response)
                    }
                    post("/findById") {
                        val request = call.receive<PostSearchRequestDto>()
                        val response = postService.addViews(request)
                        call.respond(response)
                    }
                    post {
                        val user = call.authentication.principal<UserModel>()
                        val request = call.receive<PostRequestDto>()
                        val model = postService.save(request, user!!.username)
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
                        val user = call.authentication.principal<UserModel>()
                        val model = checkIdAndModel(postService)
                        if (user!!.username == model.author) {
                            postService.remove(model)
                            call.respondText("Post has been deleted ")
                        } else {
                            throw AuthFailException("This user can't delete the post")
                        }

                    }
                }
            }
        }

    }
}
