package com.gvteam.sisimpresion3d.ui.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gvteam.sisimpresion3d.ui.BottomScreen
import com.gvteam.sisimpresion3d.ui.components.MaterialCard
import com.gvteam.sisimpresion3d.ui.components.MaterialConsumptionDialog
import com.gvteam.sisimpresion3d.ui.components.PrinterCard
import com.gvteam.sisimpresion3d.viewmodel.MaterialViewModel
import com.gvteam.sisimpresion3d.viewmodel.PrinterViewModel
import com.gvteam.sisimpresion3d.viewmodel.UserViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class,
    ExperimentalSharedTransitionApi::class
)
@Composable
fun MainScreen(
    printerViewModel: PrinterViewModel,
    materialViewModel: MaterialViewModel,
    userViewModel: UserViewModel,
    onNavigateToProfile: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val screens = listOf(
        BottomScreen.Home,
        BottomScreen.Insumos,
        BottomScreen.Costos
    )
    val pagerState = rememberPagerState(pageCount = { screens.size })
    val scope = rememberCoroutineScope()
    val hazeState = remember { HazeState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val userProfile by userViewModel.userProfile.collectAsState()
    val userPhoto = userProfile?.avatarUrl

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.hazeEffect(
                    state = hazeState,
                    style = HazeMaterials.ultraThin()
                ),
                title = {
                    Text(
                        "Impresión 3D - ISC",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { onNavigateToProfile() }
                    ) {
                        if (userPhoto != null) {
                            with(sharedTransitionScope) {
                                AsyncImage(
                                    model = userPhoto,
                                    contentDescription = "Perfil",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .sharedElement(
                                            sharedContentState = rememberSharedContentState(key = "profile_image"),
                                            animatedVisibilityScope = animatedVisibilityScope
                                        )
                                        .clip(CircleShape)
                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.hazeEffect(
                    state = hazeState,
                    style = HazeMaterials.ultraThin()
                ),
                containerColor = Color.Transparent,
                tonalElevation = 0.dp
            ) {
                screens.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
                .hazeSource(state = hazeState)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> PrintersContent(printerViewModel, innerPadding)
                    1 -> InsumosContent(materialViewModel, innerPadding)
                    2 -> CostosContent(innerPadding)
                }
            }
        }
    }
}

@Composable
fun PrintersContent(viewModel: PrinterViewModel, paddingValues: PaddingValues) {
    val printers by viewModel.printers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            isLoading -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            errorMessage != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Error: $errorMessage", color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.fetchPrinters() }) { Text("Reintentar") }
                }
            }

            printers.isEmpty() -> Text(
                "No hay impresoras",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding() + 16.dp,
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(printers) { printer ->
                        PrinterCard(printer = printer)
                    }
                }
            }
        }
    }
}

@Composable
fun InsumosContent(viewModel: MaterialViewModel, paddingValues: PaddingValues) {
    val materials by viewModel.materials.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedMaterial by remember { mutableStateOf<com.gvteam.sisimpresion3d.model.Material?>(null) }

    fun onMaterialClick(material: com.gvteam.sisimpresion3d.model.Material) {
        selectedMaterial = material
        showDialog = true
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            isLoading && materials.isEmpty() -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            errorMessage != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Error: $errorMessage", color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.fetchMaterials() }) { Text("Reintentar") }
                }
            }
            materials.isEmpty() -> Text(
                "No hay insumos registrados",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding() + 16.dp,
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(materials) { material ->
                        Box(modifier = Modifier.clickable { onMaterialClick(material) }) {
                            MaterialCard(material = material)
                        }
                    }
                }
            }
        }

        if (showDialog && selectedMaterial != null) {
            MaterialConsumptionDialog(
                material = selectedMaterial!!,
                onDismiss = { showDialog = false },
                onConfirm = { amountConsumed ->
                    viewModel.consumeMaterial(selectedMaterial!!, amountConsumed)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun CostosContent(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Text("Costos", color = MaterialTheme.colorScheme.onBackground)
    }
}