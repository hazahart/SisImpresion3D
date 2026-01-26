package com.gvteam.sisimpresion3d.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gvteam.sisimpresion3d.data.SupabaseClient
import com.gvteam.sisimpresion3d.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class UserViewModel : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return@launch

                val profile = SupabaseClient.client.from("profiles")
                    .select { filter { eq("id", userId) } }
                    .decodeSingleOrNull<UserProfile>()

                _userProfile.value = profile
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateProfile(updatedProfile: UserProfile) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updateData = buildJsonObject {
                    put("full_name", updatedProfile.fullName)
                    put("info", updatedProfile.info ?: "")
                    put("is_external", updatedProfile.isExternal)
                    put("control_number", updatedProfile.controlNumber)
                    put("career", updatedProfile.career)
                    put("semester", updatedProfile.semester)
                    put("avatar_url", updatedProfile.avatarUrl)
                }

                SupabaseClient.client.from("profiles").update(updateData) {
                    filter { eq("id", updatedProfile.id) }
                }

                _userProfile.value = updatedProfile
                fetchUserProfile()

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadAvatar(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUser = SupabaseClient.client.auth.currentUserOrNull() ?: return@launch
                val userId = currentUser.id
                val fileName = "$userId/avatar.jpg"

                val itemBytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                }

                if (itemBytes != null) {
                    val bucket = SupabaseClient.client.storage.from("avatars")

                    bucket.upload(fileName, itemBytes) {
                        upsert = true
                    }

                    val publicUrl = bucket.publicUrl(fileName)
                    val versionedUrl = "$publicUrl?v=${System.currentTimeMillis()}"

                    val currentProfile = _userProfile.value
                    if (currentProfile != null) {
                        val updatedProfile = currentProfile.copy(avatarUrl = versionedUrl)
                        updateProfile(updatedProfile)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}