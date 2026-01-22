package com.gvteam.sisimpresion3d.data.repository

import com.gvteam.sisimpresion3d.model.Material
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

class MaterialRepository(private val supabase: SupabaseClient) {

    suspend fun getMaterials(): List<Material> {
        return supabase.from("materials")
            .select {
                filter {
                    eq("is_active", true)
                }
                order("type", Order.ASCENDING)
            }
            .decodeList<Material>()
    }

    suspend fun subscribeToRealtime(): Flow<PostgresAction> {
        val channel = supabase.channel("materials-updates") {}

        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "materials"
        }

        channel.subscribe()

        return changeFlow.onEach {
        }
    }

    suspend fun updateWeight(id: String, newWeight: Int) {
        supabase.from("materials").update(
            {
                set("remaining_weight_g", newWeight)
            }
        ) {
            filter {
                eq("id", id)
            }
        }
    }
}