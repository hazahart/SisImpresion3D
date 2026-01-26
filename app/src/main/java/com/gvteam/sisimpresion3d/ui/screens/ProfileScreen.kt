package com.gvteam.sisimpresion3d.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.gvteam.sisimpresion3d.viewmodel.UserViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import java.io.File

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class,
    ExperimentalHazeMaterialsApi::class
)
@Composable
fun ProfileScreen(
    userViewModel: UserViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val context = LocalContext.current
    val userProfile by userViewModel.userProfile.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()

    val hazeState = remember { HazeState() }

    var showPhotoSheet by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showSchoolSheet by remember { mutableStateOf(false) }

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val careers = listOf(
        "Ing. en Sistemas Computacionales", "Ing. Mecatrónica", "Ing. Industrial",
        "Ing. Electrónica", "Ing. Mecánica", "Ing. Gestión Empresarial",
        "Ing. Ambiental", "Ing. Bioquímica", "Lic. Administración"
    )
    val semesters = (1..12).map { "Semestre $it" } + "Semestre 13+"

    val cropImageLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uri = result.uriContent
            if (uri != null) {
                userViewModel.uploadAvatar(context, uri)
                showPhotoSheet = false
            }
        } else {
            val exception = result.error
            if (exception != null) {
                Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun launchCrop(uri: Uri) {
        val cropOptions = CropImageOptions(
            cropShape = CropImageView.CropShape.OVAL,
            fixAspectRatio = true,
            aspectRatioX = 1,
            aspectRatioY = 1,
            guidelines = CropImageView.Guidelines.ON,
            outputCompressFormat = android.graphics.Bitmap.CompressFormat.JPEG
        )

        val contractOptions = CropImageContractOptions(
            uri = uri,
            cropImageOptions = cropOptions
        )

        cropImageLauncher.launch(contractOptions)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) launchCrop(uri) }
    )
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success -> if (success && tempCameraUri != null) launchCrop(tempCameraUri!!) }
    )
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val uri = createImageUriForProfile(context); tempCameraUri =
                    uri; cameraLauncher.launch(
                    uri
                )
            } else {
                Toast.makeText(context, "Se necesita permiso de cámara", Toast.LENGTH_SHORT).show()
            }
        }
    )

    if (showNameDialog && userProfile != null) {
        var tempName by remember { mutableStateOf(userProfile?.fullName ?: "") }
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Editar Nombre") },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("Nombre Completo") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (tempName.isNotBlank()) {
                        userViewModel.updateProfile(userProfile!!.copy(fullName = tempName)); showNameDialog =
                            false
                    }
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNameDialog = false
                }) { Text("Cancelar") }
            }
        )
    }

    if (showInfoDialog && userProfile != null) {
        var tempInfo by remember { mutableStateOf(userProfile?.info ?: "") }
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Editar Info") },
            text = {
                OutlinedTextField(
                    value = tempInfo,
                    onValueChange = { tempInfo = it },
                    label = { Text("Info (Estado)") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    userViewModel.updateProfile(
                        userProfile!!.copy(
                            info = tempInfo
                        )
                    ); showInfoDialog = false
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showInfoDialog = false
                }) { Text("Cancelar") }
            }
        )
    }

    if (showSchoolSheet && userProfile != null) {
        ModalBottomSheet(
            onDismissRequest = { showSchoolSheet = false },
            containerColor = Color.Transparent,
            dragHandle = null
        ) {
            Box(
                modifier = Modifier
                    .hazeEffect(state = hazeState, style = HazeMaterials.thin())
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp, bottom = 48.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BottomSheetDefaults.DragHandle()
                    Text(
                        "Información Escolar",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    var tempControlNumber by remember(userProfile) {
                        mutableStateOf(
                            userProfile?.controlNumber ?: ""
                        )
                    }
                    var tempCareer by remember(userProfile) {
                        mutableStateOf(
                            userProfile?.career ?: ""
                        )
                    }
                    var tempSemester by remember(userProfile) {
                        mutableStateOf(
                            userProfile?.semester ?: ""
                        )
                    }
                    var isExternal by remember(userProfile) {
                        mutableStateOf(
                            userProfile?.isExternal ?: false
                        )
                    }

                    var careerExpanded by remember { mutableStateOf(false) }
                    var semesterExpanded by remember { mutableStateOf(false) }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.5f
                            )
                        ), shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Soy Externo / Servicio Social",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Desactiva esto para ingresar tus datos.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Switch(checked = isExternal, onCheckedChange = { isExternal = it })
                        }
                    }

                    if (!isExternal) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = tempControlNumber,
                            onValueChange = { tempControlNumber = it },
                            label = { Text("Número de Control") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ExposedDropdownMenuBox(
                            expanded = careerExpanded,
                            onExpandedChange = { careerExpanded = !careerExpanded }) {
                            OutlinedTextField(
                                value = tempCareer,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Carrera") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = careerExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = careerExpanded,
                                onDismissRequest = { careerExpanded = false }) {
                                careers.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item) },
                                        onClick = { tempCareer = item; careerExpanded = false })
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        ExposedDropdownMenuBox(
                            expanded = semesterExpanded,
                            onExpandedChange = { semesterExpanded = !semesterExpanded }) {
                            OutlinedTextField(
                                value = tempSemester,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Semestre") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = semesterExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = semesterExpanded,
                                onDismissRequest = { semesterExpanded = false }) {
                                semesters.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item) },
                                        onClick = { tempSemester = item; semesterExpanded = false })
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            if (!isExternal && (tempControlNumber.isBlank() || tempCareer.isBlank() || tempSemester.isBlank())) {
                                Toast.makeText(
                                    context,
                                    "Completa todos los campos escolares",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val updated = userProfile!!.copy(
                                    isExternal = isExternal,
                                    controlNumber = if (isExternal) null else tempControlNumber,
                                    career = if (isExternal) null else tempCareer,
                                    semester = if (isExternal) null else tempSemester
                                )
                                userViewModel.updateProfile(updated)
                                showSchoolSheet = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Cambios")
                    }
                }
            }
        }
    }

    if (showPhotoSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoSheet = false },
            containerColor = Color.Transparent,
            dragHandle = null
        ) {
            Box(
                modifier = Modifier
                    .hazeEffect(state = hazeState, style = HazeMaterials.thin())
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(bottom = 32.dp, top = 16.dp)) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) { BottomSheetDefaults.DragHandle() }
                    Text(
                        "Cambiar foto de perfil",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                    ListItem(
                        headlineContent = { Text("Cámara") },
                        leadingContent = { Icon(Icons.Default.CameraAlt, null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            val permission =
                                Manifest.permission.CAMERA; if (ContextCompat.checkSelfPermission(
                                context,
                                permission
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            val uri = createImageUriForProfile(context); tempCameraUri =
                                uri; cameraLauncher.launch(uri)
                        } else {
                            permissionLauncher.launch(permission)
                        }
                        })
                    ListItem(
                        headlineContent = { Text("Galería") },
                        leadingContent = { Icon(Icons.Default.Image, null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        })
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.hazeEffect(state = hazeState, style = HazeMaterials.thin())
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier
                .fillMaxWidth()
                .padding(padding))
        }

        Box(modifier = Modifier
            .fillMaxSize()
            .hazeSource(state = hazeState)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(160.dp)) {
                        val photoUrl = userProfile?.avatarUrl
                        if (photoUrl != null) {
                            with(sharedTransitionScope) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .sharedElement(
                                            sharedContentState = rememberSharedContentState(key = "profile_image"),
                                            animatedVisibilityScope = animatedVisibilityScope
                                        )
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFCFD8DC), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    null,
                                    modifier = Modifier.size(90.dp),
                                    tint = Color.White
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = (-4).dp, y = (-4).dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable { showPhotoSheet = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                ProfileListItem(
                    icon = Icons.Default.Person,
                    label = "Nombre",
                    value = userProfile?.fullName ?: "Cargando...",
                    subtext = "Toca para editar tu nombre.",
                    showEditIcon = true,
                    onClick = { showNameDialog = true })
                HorizontalDivider(
                    modifier = Modifier.padding(start = 72.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                ProfileListItem(
                    icon = Icons.Default.Info,
                    label = "Info",
                    value = userProfile?.info ?: "Sin info",
                    subtext = "Toca para editar tu mensaje de estado.",
                    showEditIcon = true,
                    onClick = { showInfoDialog = true })
                HorizontalDivider(
                    modifier = Modifier.padding(start = 72.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                val userInfo =
                    if (userProfile?.isExternal == true) "Externo / Servicio Social" else "${userProfile?.career ?: "Carrera"} • ${userProfile?.semester ?: "Semestre"}"
                ProfileListItem(
                    icon = Icons.Default.Business,
                    label = "Datos Escolares",
                    value = userInfo,
                    showEditIcon = true,
                    onClick = { showSchoolSheet = true })
                HorizontalDivider(
                    modifier = Modifier.padding(start = 72.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                ProfileListItem(
                    icon = Icons.Default.Email,
                    label = "Correo electrónico",
                    value = userProfile?.email ?: "",
                    showEditIcon = false
                )
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                ListItem(
                    headlineContent = {
                        Text(
                            "Cerrar sesión",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    leadingContent = {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    modifier = Modifier.clickable { onLogout() },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun ProfileListItem(
    icon: ImageVector,
    label: String,
    value: String,
    subtext: String? = null,
    showEditIcon: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 18.dp, horizontal = 16.dp), verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(24.dp)
        )
        Spacer(modifier = Modifier.width(32.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtext != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    subtext,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
        if (showEditIcon) {
            Icon(
                Icons.Default.Edit,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(24.dp)
            )
        }
    }
}

fun createImageUriForProfile(context: Context): Uri {
    val directory = File(context.cacheDir, "profile_images")
    directory.mkdirs()
    val file = File.createTempFile("profile_edit_", ".jpg", directory)
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}