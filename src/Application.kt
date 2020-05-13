package com.galaktionov

import com.galaktionov.dto.PostRequestDto
import com.galaktionov.dto.PostResponseDto
import com.galaktionov.firstandroidapp.dto.PostModel
import com.galaktionov.repository.PostMutexRepository
import com.galaktionov.repository.PostRepository
import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import kotlinx.coroutines.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.url
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.kodein.di.ktor.KodeinFeature
import org.kodein.di.ktor.kodein
import kotlin.coroutines.EmptyCoroutineContext

const val posts_url = "https://raw.githubusercontent.com/arty2100/gson/master/posts.json"
const val adv_posts_url = "https://raw.githubusercontent.com/arty2100/gson/master/adv_posts.json"

var allPosts: MutableList<PostModel> = emptyList<PostModel>().toMutableList()
private fun prepareList(): MutableList<PostModel> = mutableListOf(

    PostModel(
        1L,
        "Google",
        "Try the best search engine!",
        1523496778000,
        true,
        5,
        1,
        0,
        postTpe = PostModel.POST_TYPE.ADV,
        advLink = "http://www.google.com",
        companyImg = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/53/Google_%22G%22_Logo.svg/1004px-Google_%22G%22_Logo.svg.png"
    ),
    PostModel(
        15L,
        "Google",
        "Try the best search engine!",
        1523496778000,
        true,
        5,
        1,
        0,
        postTpe = PostModel.POST_TYPE.ADV,
        advLink = "http://www.google.com",
        companyImg = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/53/Google_%22G%22_Logo.svg/1004px-Google_%22G%22_Logo.svg.png"
    ),
    PostModel(
        10L,
        "Google",
        "Try the best search engine!",
        1523496778000,
        true,
        5,
        1,
        0,
        postTpe = PostModel.POST_TYPE.ADV,
        advLink = "http://www.google.com",
        companyImg = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/53/Google_%22G%22_Logo.svg/1004px-Google_%22G%22_Logo.svg.png"
    )
)

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)


fun Application.module(testing: Boolean = false) {
    val posts = prepareList()

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
        exception<AuthenticationException> { cause ->
            call.respond(HttpStatusCode.Unauthorized)
            throw cause
        }
        exception<AuthorizationException> { cause ->
            call.respond(HttpStatusCode.Forbidden)
            throw cause
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
        bind<PostRepository>() with singleton {
            PostMutexRepository().apply {
                allPosts.forEach {
                    runBlocking {
                        save(PostModel(it.id, it.author, it.content,it.created,it.likedByMe,it.likes,it.comments,it.shares,it.address,it.location,it.videoUrl,it.postTpe,it.advLink,it.companyImg))
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
                val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException("id", "Long")
                val model = repo.getById(id) ?: throw NotFoundException()
                val response = PostResponseDto.fromModel(model)
                call.respond(response)
            }
            post {
                val request = call.receive<PostRequestDto>()
                val model = PostModel( null,request.author, request.content,request.created,request.likedByMe,request.likes,request.comments,request.shares,request.address,request.location,request.videoUrl,request.postTpe,request.advLink,request.companyImg)
                val response = repo.save(model)
                call.respond(response)
            }
        }
    }
}


class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()

data class JsonSampleClass(val hello: String)

