package com.gvteam.sisimpresion3d.data

import com.gvteam.sisimpresion3d.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.serialization.json.Json

object SupabaseClient {
    private const val SUPABASE_URL = BuildConfig.SUPABASE_URL
    private const val SUPABASE_KEY = BuildConfig.SUPABASE_KEY

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY,
    ) {
        httpEngine = OkHttp.create()
        install(Postgrest)
        install(Auth)
        install(Realtime)

        defaultSerializer = KotlinXSerializer(Json { ignoreUnknownKeys = true })
    }
}