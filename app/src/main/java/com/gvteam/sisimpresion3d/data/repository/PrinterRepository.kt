package com.gvteam.sisimpresion3d.data.repository

import android.util.Log
import com.gvteam.sisimpresion3d.data.SupabaseClient
import com.gvteam.sisimpresion3d.model.Printer
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

class PrinterRepository {

    suspend fun getPrinters(): List<Printer> {
        return withContext(Dispatchers.IO) {
            SupabaseClient.client.from("printers").select().decodeList<Printer>()
        }
    }

    suspend fun subscribeToRealtime(): Flow<PostgresAction> {
        val channel = SupabaseClient.client.channel("printers-updates") {}

        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "printers"
        }

        channel.subscribe()

        return changeFlow
    }
}