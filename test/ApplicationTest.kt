package com.galaktionov

import com.galaktionov.firstandroidapp.dto.PostModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.withCharset
import io.ktor.server.testing.contentType
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import javafx.geometry.Pos
import kotlin.test.Test
import kotlin.test.assertEquals


class ApplicationTest {
    @Test
    fun testGetAll() {
        withTestApplication({ module() }) {
            with(handleRequest(HttpMethod.Get, "/posts")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(
                        ContentType.Application.Json.withCharset(Charsets.UTF_8),
                        response.contentType()
                )

                val myType = object : TypeToken<List<PostModel>>() {}.type
                val posts = Gson().fromJson<List<PostModel>>(response.content, myType)
            }
        }
    }
}
