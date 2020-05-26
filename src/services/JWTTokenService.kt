package com.galaktionov.services

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm

class JWTTokenService(secret: String) {
    private val algo = Algorithm.HMAC256(secret)
    val verifier: JWTVerifier = JWT.require(algo).build()
    fun generate(id: Long): String = JWT.create()
            .withClaim("id", id)
            .sign(algo)
}