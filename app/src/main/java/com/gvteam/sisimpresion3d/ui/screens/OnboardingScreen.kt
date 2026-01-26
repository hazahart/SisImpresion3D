package com.gvteam.sisimpresion3d.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gvteam.sisimpresion3d.data.SupabaseClient
import com.gvteam.sisimpresion3d.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser = remember { SupabaseClient.client.auth.currentUserOrNull() }
    val pagerState = rememberPagerState(pageCount = { 3 })

    var fullName by remember {
        mutableStateOf(
            currentUser?.userMetadata?.get("full_name")?.toString()?.removeSurrounding("\"") ?: ""
        )
    }
    var info by remember { mutableStateOf("¡Hola! Estoy usando SisImpresión 3D.") }

    var isExternal by remember { mutableStateOf(false) }
    var controlNumber by remember {
        mutableStateOf(
            currentUser?.email?.split("@")?.firstOrNull()?.filter { it.isDigit() } ?: "")
    }
    var selectedCareer by remember { mutableStateOf("") }
    var selectedSemester by remember { mutableStateOf("") }

    var localAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showPhotoOptions by remember { mutableStateOf(false) }

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    var careerExpanded by remember { mutableStateOf(false) }
    var semesterExpanded by remember { mutableStateOf(false) }

    val careers = listOf(
        "Ing. en Sistemas Computacionales", "Ing. Mecatrónica", "Ing. Industrial",
        "Ing. Electrónica", "Ing. Mecánica", "Ing. Gestión Empresarial",
        "Ing. Ambiental", "Ing. Bioquímica", "Lic. Administración"
    )
    val semesters = (1..12).map { "Semestre $it" } + "Semestre 13+"

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) localAvatarUri = uri
            showPhotoOptions = false
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempCameraUri != null) {
                localAvatarUri = tempCameraUri
            }
            showPhotoOptions = false
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val uri = createImageUri(context)
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            } else {
                Toast.makeText(context, "Se necesita permiso de cámara", Toast.LENGTH_SHORT).show()
            }
        }
    )

    fun validatePage1(): Boolean {
        if (!isExternal && (controlNumber.isBlank() || selectedCareer.isBlank() || selectedSemester.isBlank())) {
            return false
        }
        return true
    }

    fun finishOnboarding() {
        if (!validatePage1()) {
            Toast.makeText(context, "Faltan datos escolares. Regresando...", Toast.LENGTH_SHORT)
                .show()
            scope.launch { pagerState.animateScrollToPage(1) }
            return
        }

        if (fullName.isBlank()) {
            Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            isLoading = true
            try {
                var finalAvatarUrl = currentUser?.userMetadata?.get("avatar_url")?.toString()
                    ?.removeSurrounding("\"")

                if (localAvatarUri != null) {
                    val userId = currentUser?.id ?: return@launch
                    val fileName = "$userId/avatar_${System.currentTimeMillis()}.jpg"

                    val itemBytes = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(localAvatarUri!!)
                            ?.use { it.readBytes() }
                    }

                    if (itemBytes != null) {
                        val bucket = SupabaseClient.client.storage.from("avatars")
                        bucket.upload(fileName, itemBytes) {
                            upsert = true
                        }
                        finalAvatarUrl = bucket.publicUrl(fileName)
                    }
                }

                val profile = UserProfile(
                    id = currentUser?.id ?: "",
                    fullName = fullName,
                    info = info,
                    email = currentUser?.email ?: "",
                    avatarUrl = finalAvatarUrl,
                    isExternal = isExternal,
                    controlNumber = if (isExternal) null else controlNumber,
                    career = if (isExternal) null else selectedCareer,
                    semester = if (isExternal) null else selectedSemester
                )

                SupabaseClient.client.from("profiles").upsert(profile)
                onFinished()

            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    if (showPhotoOptions) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoOptions = false },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    "Foto de perfil",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                ListItem(
                    headlineContent = { Text("Cámara") },
                    leadingContent = {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.clickable {
                        val permission = Manifest.permission.CAMERA
                        if (ContextCompat.checkSelfPermission(
                                context,
                                permission
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            val uri = createImageUri(context)
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        } else {
                            permissionLauncher.launch(permission)
                        }
                    }
                )
                ListItem(
                    headlineContent = { Text("Galería") },
                    leadingContent = {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.clickable {
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                )
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    AnimatedVisibility(
                        visible = pagerState.currentPage > 0,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        IconButton(onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    pagerState.currentPage - 1
                                )
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { iteration ->
                        val color =
                            if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        Box(modifier = Modifier
                            .clip(CircleShape)
                            .background(color)
                            .size(10.dp))
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            when (pagerState.currentPage) {
                                0 -> pagerState.animateScrollToPage(1)
                                1 -> if (validatePage1()) pagerState.animateScrollToPage(2) else Toast.makeText(
                                    context,
                                    "Completa los datos escolares",
                                    Toast.LENGTH_SHORT
                                ).show()

                                2 -> finishOnboarding()
                            }
                        }
                    },
                    enabled = !isLoading,
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(if (pagerState.currentPage == 2) "Finalizar" else "Siguiente")
                        if (pagerState.currentPage != 2) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            userScrollEnabled = true
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (page) {
                    0 -> {
                        Text(text = "👋", fontSize = 60.sp)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Bienvenido",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Configura tu perfil para empezar a gestionar tus impresiones 3D de manera fácil y rápida.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    1 -> {
                        Text(
                            text = "Datos Escolares",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.5f
                                )
                            ), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Soy Externo / Servicio Social",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "No soy estudiante activo.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Switch(checked = isExternal, onCheckedChange = { isExternal = it })
                            }
                        }
                        AnimatedVisibility(
                            visible = !isExternal,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(24.dp))
                                OutlinedTextField(
                                    value = controlNumber,
                                    onValueChange = { controlNumber = it },
                                    label = { Text("Número de Control") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Business,
                                            contentDescription = null
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                ExposedDropdownMenuBox(
                                    expanded = careerExpanded,
                                    onExpandedChange = { careerExpanded = !careerExpanded }) {
                                    OutlinedTextField(
                                        value = selectedCareer,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Carrera") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = careerExpanded)
                                        },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = careerExpanded,
                                        onDismissRequest = { careerExpanded = false }) {
                                        careers.forEach { item ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = item
                                                    )
                                                },
                                                onClick = {
                                                    selectedCareer = item; careerExpanded = false
                                                })
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                ExposedDropdownMenuBox(
                                    expanded = semesterExpanded,
                                    onExpandedChange = { semesterExpanded = !semesterExpanded }) {
                                    OutlinedTextField(
                                        value = selectedSemester,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Semestre") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = semesterExpanded)
                                        },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = semesterExpanded,
                                        onDismissRequest = { semesterExpanded = false }) {
                                        semesters.forEach { item ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = item
                                                    )
                                                },
                                                onClick = {
                                                    selectedSemester = item; semesterExpanded =
                                                    false
                                                })
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        Text(
                            text = "Tu Identidad",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(40.dp))
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { showPhotoOptions = true },
                                contentAlignment = Alignment.Center
                            ) {
                                if (localAvatarUri != null) {
                                    AsyncImage(
                                        model = localAvatarUri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    val googlePhoto =
                                        currentUser?.userMetadata?.get("avatar_url")?.toString()
                                            ?.removeSurrounding("\"")
                                    if (googlePhoto != null) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context).data(googlePhoto)
                                                .crossfade(true).build(),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.AddAPhoto,
                                            contentDescription = null,
                                            modifier = Modifier.size(40.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 4.dp, y = 4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Nombre Completo") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = info,
                            onValueChange = { info = it },
                            label = { Text("Info (Estado)") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Ej. ¡Hola! Estoy usando la app") }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Este mensaje será visible en tu perfil.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

fun createImageUri(context: Context): Uri {
    val directory = File(context.cacheDir, "my_images")
    directory.mkdirs()
    val file = File.createTempFile("selected_image_", ".jpg", directory)
    val authority = "${context.packageName}.provider"
    return FileProvider.getUriForFile(context, authority, file)
}