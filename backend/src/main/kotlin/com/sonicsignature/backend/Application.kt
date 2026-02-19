package com.sonicsignature.backend

import com.sonicsignature.backend.routes.spotifyProxyRoutes
import com.sonicsignature.backend.service.SpotifyAuthService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port) {
                configurePlugins()
                configureRouting()
            }
            .start(wait = true)
}

private fun Application.configurePlugins() {
    install(ContentNegotiation) {
        json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = true
                }
        )
    }

    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.ContentType)
    }
}

private fun Application.configureRouting() {
    val authService = SpotifyAuthService()

    routing { spotifyProxyRoutes(authService) }
}
