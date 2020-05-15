package com.galaktionov

import com.galaktionov.dto.PostRequestDto
import com.galaktionov.dto.PostResponseDto
import com.galaktionov.firstandroidapp.dto.PostModel
import com.galaktionov.model.ErrorModel
import com.galaktionov.repository.PostMutexRepository
import com.galaktionov.repository.PostRepository
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.get
import io.ktor.features.ContentNegotiation
import io.ktor.features.NotFoundException
import io.ktor.features.ParameterConversionException
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.kodein.di.generic.bind
import org.kodein.di.generic.eagerSingleton
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.kodein.di.ktor.KodeinFeature
import org.kodein.di.ktor.kodein

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
            val error = ErrorModel(HttpStatusCode.BadRequest.value, HttpStatusCode.BadRequest.description, cause.toString())
            call.respond(error)
            throw cause
        }
        exception<NotFoundException> { cause ->
            val error = ErrorModel(HttpStatusCode.NotFound.value, HttpStatusCode.NotFound.description, cause.toString())
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
                        save(PostModel(it.id, it.author, it.content, it.created, it.likedByMe, it.dislikedByMe, it.likes, it.comments, it.shares, it.address, it.location, it.videoUrl, it.postTpe, it.advLink, it.companyImg))
                    }
                }
            }
        }
    }

    routing {
        val repo by kodein().instance<PostRepository>()

        route("/api/v1/posts") {
            get {
                val response = repo.getAll().map(PostResponseDto.Companion::fromModel)
                call.respond(response)
            }
            get("/{id}") {
                val model = checkIdAndModel(repo)
                val response = PostResponseDto.fromModel(model)
                call.respond(response)
            }
            post {
                val request = call.receive<PostRequestDto>()
                val modelToSave = PostModel(null, request.author, request.content, request.created, request.likedByMe, request.dislikedByMe, request.likes, request.comments, request.shares, request.address, request.location, request.videoUrl, request.postTpe, request.advLink, request.companyImg)
                val model = repo.save(modelToSave)
                call.respond(PostResponseDto.fromModel(model))
            }
            post("/like/{id}") {
                val model = checkIdAndModel(repo)
                call.respond(PostResponseDto.fromModel(repo.like(model)))
            }
            post("/dislike/{id}") {
                val model = checkIdAndModel(repo)
                call.respond(PostResponseDto.fromModel(repo.dislike(model)))
            }
            post("/repost/{id}") {
                val model = checkIdAndModel(repo)
                call.respond(PostResponseDto.fromModel(repo.repost(model)))
            }
            delete("/{id}") {
                val model = checkIdAndModel(repo)
                repo.remove(model)
                call.respondText("Post has been deleted ")
            }
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.checkIdAndModel(repo: PostRepository): PostModel {
    val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException("id", "Long")
    return repo.getById(id) ?: throw NotFoundException()
}
