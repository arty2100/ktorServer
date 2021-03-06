package com.galaktionov

import com.galaktionov.exception.AuthFailException
import com.galaktionov.exception.PasswordChangeException
import com.galaktionov.firstandroidapp.dto.PostModel
import com.galaktionov.model.ErrorModel
import com.galaktionov.model.UserRepository
import com.galaktionov.repository.PostMutexRepository
import com.galaktionov.repository.PostRepository
import com.galaktionov.repository.UserRepositoryInMemoryWithMutexImpl
import com.galaktionov.repository.v1.RoutingV1
import com.galaktionov.services.FileService
import com.galaktionov.services.JWTTokenService
import com.galaktionov.services.PostService
import com.galaktionov.services.UserService
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.get
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.kodein.di.generic.*
import org.kodein.di.ktor.KodeinFeature
import org.kodein.di.ktor.kodein
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import javax.naming.ConfigurationException

const val posts_url = "https://raw.githubusercontent.com/arty2100/gson/master/posts.json"
const val adv_posts_url = "https://raw.githubusercontent.com/arty2100/gson/master/adv_posts.json"

var allPosts: MutableList<PostModel> = emptyList<PostModel>().toMutableList()

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)


fun Application.module(testing: Boolean = false) {

    val client = HttpClient(CIO) {

        install(Logging) {
            level = LogLevel.HEADERS
        }
        install(JsonFeature) {
            acceptContentTypes = listOf(
                    ContentType.Text.Plain,
                    ContentType.Application.Json
            )
            serializer = GsonSerializer()
        }
    }
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
            serializeNulls()
        }
    }
    install(StatusPages) {

        exception<ParameterConversionException> { cause ->
            val error = ErrorModel(HttpStatusCode.BadRequest.value, HttpStatusCode.BadRequest.description, cause.message)
            call.respond(error)
            throw cause
        }
        exception<UnsupportedMediaTypeException> { cause ->
            val error = ErrorModel(HttpStatusCode.UnsupportedMediaType.value, HttpStatusCode.UnsupportedMediaType.description, cause.message)
            call.respond(error)
        }
        exception<NotFoundException> { cause ->
            val error = ErrorModel(HttpStatusCode.NotFound.value, HttpStatusCode.NotFound.description, cause.message)
            call.respond(error)
        }
        exception<BadRequestException> { cause ->
            val error = ErrorModel(HttpStatusCode.BadRequest.value, HttpStatusCode.BadRequest.description, cause.message)
            call.respond(error)
        }
        exception<DuplicateName> { cause ->
            val error = ErrorModel(value = HttpStatusCode.BadRequest.value, additionalMsg = cause.name)
            call.respond(error)
        }
        exception<AuthFailException> { cause ->
            val error = ErrorModel(value = HttpStatusCode.Unauthorized.value, description = HttpStatusCode.Unauthorized.description, additionalMsg = cause.message)
            call.respond(error)
        }
        exception<PasswordChangeException> { cause ->
            val error = ErrorModel(value = HttpStatusCode.Forbidden.value, description = HttpStatusCode.Forbidden.description, additionalMsg = cause.message)
            call.respond(error)
        }

    }


    runBlocking {

        launch {
            val posts = withContext(Dispatchers.IO) {
                client.get<MutableList<PostModel>>(posts_url)
            }
            allPosts.addAll(posts)
            val advPosts = withContext(Dispatchers.IO) {
                client.get<MutableList<PostModel>>(adv_posts_url)
            }
            allPosts.addAll(advPosts)

        }
    }

    install(KodeinFeature) {
        bind<PostRepository>() with eagerSingleton {
            PostMutexRepository().apply {
                allPosts.forEach {
                    runBlocking {
                        save(PostModel(it.id, it.author, it.content, it.created, it.likedByMe, it.dislikedByMe, it.likes, it.comments, it.shares, it.address, it.location, it.videoUrl, it.postTpe, it.advLink, it.companyImg, 0, mutableListOf()), null)
                    }
                }
            }
        }
        constant(tag = "upload-dir") with (
                environment.config.propertyOrNull("ktor.ktorServer.upload.dir")?.getString()
                        ?: throw ConfigurationException("Upload dir is not specified")
                )
        constant(tag = "secret") with (
                environment.config.propertyOrNull("ktor.secret.key")?.getString()
                        ?: throw ConfigurationException("Secret key is not specified")
                )
        bind<PasswordEncoder>() with singleton {
            BCryptPasswordEncoder()
        }
        bind<UserRepository>() with eagerSingleton {
            UserRepositoryInMemoryWithMutexImpl()
        }
        bind<JWTTokenService>() with eagerSingleton {
            JWTTokenService(instance(tag = "secret"))
        }
        bind<FileService>() with eagerSingleton {
            FileService(instance(tag = "upload-dir"))
        }
        bind<UserService>() with eagerSingleton {
            UserService(repo = instance(), tokenService = instance(), passwordEncoder = instance())
        }
        bind<PostService>() with eagerSingleton {
            PostService(repo = instance())
        }
        bind<RoutingV1>() with eagerSingleton {
            RoutingV1(postService = instance(), userService = instance(), fileService = instance())
        }

    }
    install(Authentication) {
        jwt {
            val jwtService by kodein().instance<JWTTokenService>()
            verifier(jwtService.verifier)
            val userService by kodein().instance<UserService>()
            challenge { _, _ ->
                throw AuthFailException("Authentication error!")
            }
            validate {
                val id = it.payload.getClaim("id").asLong()
                userService.getModelById(id)
            }
        }
    }
    install(Routing) {
        val routingV1 by kodein().instance<RoutingV1>()
        routingV1.setup(this)
    }
}


