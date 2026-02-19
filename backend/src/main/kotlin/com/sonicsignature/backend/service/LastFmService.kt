package com.sonicsignature.backend.service

import io.github.cdimascio.dotenv.dotenv

/**
 * Provides the Last.fm API key from .env or environment variables. Last.fm uses a simple API key
 * auth — no OAuth, no token refresh.
 */
class LastFmService {

    private val dotenv = dotenv { ignoreIfMissing = true }

    val apiKey: String =
            dotenv["LASTFM_API_KEY"] ?: error("LASTFM_API_KEY not found in .env or environment")
}
