package com.gvteam.sisimpresion3d.data

import com.gvteam.sisimpresion3d.model.Printer
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PrinterRepository {
    suspend fun getPrinters(): List<Printer> {
        return withContext(Dispatchers.IO) {
            SupabaseClient.client.from("printers").select().decodeList<Printer>()
        }
    }
}