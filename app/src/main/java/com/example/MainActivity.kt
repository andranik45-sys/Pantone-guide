package com.example

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.MyApplicationTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sqrt

fun changeLanguage(context: android.content.Context, langTag: String) {
    if (android.os.Build.VERSION.SDK_INT >= 33) {
        val localeManager = androidx.core.content.ContextCompat.getSystemService(context, android.app.LocaleManager::class.java)
        localeManager?.applicationLocales = android.os.LocaleList.forLanguageTags(langTag)
    } else {
        val locale = java.util.Locale(langTag)
        java.util.Locale.setDefault(locale)
        val config = android.content.res.Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        
        var currentContext = context
        while (currentContext is android.content.ContextWrapper) {
            if (currentContext is android.app.Activity) {
                currentContext.recreate()
                break
            }
            currentContext = currentContext.baseContext
        }
    }
}


data class RecipeItem(val inkName: String, val percentage: Float)

data class PantoneColor(
    val name: String,
    val hexColor: Long,
    val baseRecipe: List<RecipeItem>,
    val foilRecipe: List<RecipeItem>
)

val BasePantoneLibrary = listOf(
    PantoneColor("Pantone Yellow C", 0xFFFFE800, listOf(RecipeItem("Yellow", 100.0f)), listOf(RecipeItem("Yellow", 80.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone Yellow 012 C", 0xFFFFD700, listOf(RecipeItem("Yellow 012", 100.0f)), listOf(RecipeItem("Yellow 012", 80.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone Orange 021 C", 0xFFFF5722, listOf(RecipeItem("Orange 021", 100.0f)), listOf(RecipeItem("Orange 021", 80.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone Warm Red C", 0xFFF9423A, listOf(RecipeItem("Warm Red", 100.0f)), listOf(RecipeItem("Warm Red", 80.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone Red 032 C", 0xFFEF3340, listOf(RecipeItem("Red 032", 100.0f)), listOf(RecipeItem("Red 032", 80.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone Rubine Red C", 0xFFCE0058, listOf(RecipeItem("Rubine Red", 100.0f)), listOf(RecipeItem("Rubine Red", 80.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone Rhodamine Red C", 0xFFE10098, listOf(RecipeItem("Rhodamine Red", 100.0f)), listOf(RecipeItem("Rhodamine Red", 80.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone Purple C", 0xFFBB29BB, listOf(RecipeItem("Purple", 100.0f)), listOf(RecipeItem("Purple", 80.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone Violet C", 0xFF440099, listOf(RecipeItem("Violet", 100.0f)), listOf(RecipeItem("Violet", 80.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone Blue 072 C", 0xFF10069F, listOf(RecipeItem("Blue 072", 100.0f)), listOf(RecipeItem("Blue 072", 80.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone Reflex Blue C", 0xFF001489, listOf(RecipeItem("Reflex Blue", 100.0f)), listOf(RecipeItem("Reflex Blue", 80.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone Process Blue C", 0xFF0085CA, listOf(RecipeItem("Process Blue", 100.0f)), listOf(RecipeItem("Process Blue", 80.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone Green C", 0xFF00AB84, listOf(RecipeItem("Green", 100.0f)), listOf(RecipeItem("Green", 80.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone Black C", 0xFF2D2926, listOf(RecipeItem("Black", 100.0f)), listOf(RecipeItem("Black", 80.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone Opaque White", 0xFFFFFFFF, listOf(RecipeItem("Opaque White", 100.0f)), listOf(RecipeItem("Opaque White", 100.0f))),
    PantoneColor("Pantone Transparent White", 0xFFEBEBEB, listOf(RecipeItem("Trans. Wt.", 100.0f)), listOf(RecipeItem("Trans. Wt.", 100.0f))),
    PantoneColor("Pantone 109 C", 0xFFFFD100, listOf(RecipeItem("Yellow 012", 99.0f), RecipeItem("Warm Red", 1.0f)), listOf(RecipeItem("Yellow 012", 79.0f), RecipeItem("Warm Red", 1.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone 116 C", 0xFFFFCD00, listOf(RecipeItem("Yellow 012", 95.0f), RecipeItem("Warm Red", 5.0f)), listOf(RecipeItem("Yellow 012", 75.0f), RecipeItem("Warm Red", 5.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone 123 C", 0xFFFFC72C, listOf(RecipeItem("Yellow 012", 85.0f), RecipeItem("Warm Red", 15.0f)), listOf(RecipeItem("Yellow 012", 68.0f), RecipeItem("Warm Red", 12.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone 130 C", 0xFFF2A900, listOf(RecipeItem("Yellow 012", 80.0f), RecipeItem("Warm Red", 20.0f)), listOf(RecipeItem("Yellow 012", 64.0f), RecipeItem("Warm Red", 16.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone 151 C", 0xFFFF8200, listOf(RecipeItem("Yellow 012", 70.0f), RecipeItem("Warm Red", 30.0f)), listOf(RecipeItem("Yellow 012", 56.0f), RecipeItem("Warm Red", 24.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone 165 C", 0xFFFF671F, listOf(RecipeItem("Yellow 012", 50.0f), RecipeItem("Warm Red", 50.0f)), listOf(RecipeItem("Yellow 012", 40.0f), RecipeItem("Warm Red", 40.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone 185 C", 0xFFE4002B, listOf(RecipeItem("Warm Red", 50.0f), RecipeItem("Rubine Red", 50.0f)), listOf(RecipeItem("Warm Red", 40.0f), RecipeItem("Rubine Red", 40.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone 200 C", 0xFFBA0C2F, listOf(RecipeItem("Rubine Red", 50.0f), RecipeItem("Yellow 012", 25.0f), RecipeItem("Black", 25.0f)), listOf(RecipeItem("Rubine Red", 40.0f), RecipeItem("Yellow 012", 20.0f), RecipeItem("Black", 20.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone 202 C", 0xFF862633, listOf(RecipeItem("Rubine Red", 40.0f), RecipeItem("Warm Red", 40.0f), RecipeItem("Black", 20.0f)), listOf(RecipeItem("Rubine Red", 32.0f), RecipeItem("Warm Red", 32.0f), RecipeItem("Black", 16.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone 212 C", 0xFFF5409E, listOf(RecipeItem("Rhodamine Red", 60.0f), RecipeItem("Trans. Wt.", 40.0f)), listOf(RecipeItem("Rhodamine Red", 48.0f), RecipeItem("Opaque White", 52.0f))),
    PantoneColor("Pantone 240 C", 0xFFD83089, listOf(RecipeItem("Rubine Red", 70.0f), RecipeItem("Rhodamine Red", 30.0f)), listOf(RecipeItem("Rubine Red", 56.0f), RecipeItem("Rhodamine Red", 24.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone 258 C", 0xFF8C4799, listOf(RecipeItem("Purple", 70.0f), RecipeItem("Process Blue", 30.0f)), listOf(RecipeItem("Purple", 56.0f), RecipeItem("Process Blue", 24.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone 266 C", 0xFF753BBD, listOf(RecipeItem("Violet", 60.0f), RecipeItem("Purple", 40.0f)), listOf(RecipeItem("Violet", 48.0f), RecipeItem("Purple", 32.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone 279 C", 0xFF418FDE, listOf(RecipeItem("Process Blue", 60.0f), RecipeItem("Reflex Blue", 15.0f), RecipeItem("Trans. Wt.", 25.0f)), listOf(RecipeItem("Process Blue", 48.0f), RecipeItem("Reflex Blue", 12.0f), RecipeItem("Opaque White", 40.0f))),
    PantoneColor("Pantone 286 C", 0xFF0032A0, listOf(RecipeItem("Reflex Blue", 50.0f), RecipeItem("Process Blue", 50.0f)), listOf(RecipeItem("Reflex Blue", 40.0f), RecipeItem("Process Blue", 40.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone 289 C", 0xFF0C2340, listOf(RecipeItem("Reflex Blue", 75.0f), RecipeItem("Black", 25.0f)), listOf(RecipeItem("Reflex Blue", 60.0f), RecipeItem("Black", 20.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone 300 C", 0xFF005EB8, listOf(RecipeItem("Process Blue", 80.0f), RecipeItem("Reflex Blue", 20.0f)), listOf(RecipeItem("Process Blue", 64.0f), RecipeItem("Reflex Blue", 16.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone 313 C", 0xFF0092BC, listOf(RecipeItem("Process Blue", 75.0f), RecipeItem("Green", 25.0f)), listOf(RecipeItem("Process Blue", 60.0f), RecipeItem("Green", 20.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone 320 C", 0xFF009CA6, listOf(RecipeItem("Process Blue", 50.0f), RecipeItem("Green", 50.0f)), listOf(RecipeItem("Process Blue", 40.0f), RecipeItem("Green", 40.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone 347 C", 0xFF009A44, listOf(RecipeItem("Green", 80.0f), RecipeItem("Yellow 012", 20.0f)), listOf(RecipeItem("Green", 64.0f), RecipeItem("Yellow 012", 16.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone 349 C", 0xFF046A38, listOf(RecipeItem("Green", 75.0f), RecipeItem("Yellow 012", 15.0f), RecipeItem("Black", 10.0f)), listOf(RecipeItem("Green", 60.0f), RecipeItem("Yellow 012", 12.0f), RecipeItem("Black", 8.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone 376 C", 0xFF84BD00, listOf(RecipeItem("Yellow", 75.0f), RecipeItem("Green", 25.0f)), listOf(RecipeItem("Yellow", 60.0f), RecipeItem("Green", 20.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone 412 C", 0xFF3D3935, listOf(RecipeItem("Black", 80.0f), RecipeItem("Orange 021", 20.0f)), listOf(RecipeItem("Black", 64.0f), RecipeItem("Orange 021", 16.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone 430 C", 0xFF7C878E, listOf(RecipeItem("Black", 50.0f), RecipeItem("Trans. Wt.", 50.0f)), listOf(RecipeItem("Black", 40.0f), RecipeItem("Opaque White", 60.0f))),
    PantoneColor("Pantone 469 C", 0xFF583626, listOf(RecipeItem("Orange 021", 40.0f), RecipeItem("Black", 60.0f)), listOf(RecipeItem("Orange 021", 32.0f), RecipeItem("Black", 48.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone 485 C", 0xFFDA291C, listOf(RecipeItem("Warm Red", 75.0f), RecipeItem("Yellow 012", 25.0f)), listOf(RecipeItem("Warm Red", 60.0f), RecipeItem("Yellow 012", 20.0f), RecipeItem("Opaque White", 20.0f))),
    PantoneColor("Pantone 871 C (Gold)", 0xFF84754E, listOf(RecipeItem("Gold Paste", 80.0f), RecipeItem("Trans. Wt.", 20.0f)), listOf(RecipeItem("Gold Paste", 100.0f))),
    PantoneColor("Pantone 877 C (Silver)", 0xFF8A8D8F, listOf(RecipeItem("Silver Paste", 80.0f), RecipeItem("Trans. Wt.", 20.0f)), listOf(RecipeItem("Silver Paste", 100.0f)))
)

val PantoneLibrary: List<PantoneColor> by lazy {
    val list = BasePantoneLibrary.toMutableList()
    val existingNames = list.map { it.name }.toSet()
    for (i in 100..7547) {
        val name = "Pantone $i C"
        if (name !in existingNames) {
            list.add(generatePantone(i))
        }
    }
    list
}

fun generatePantone(number: Int): PantoneColor {
    val random = java.util.Random(number.toLong())
    val r = random.nextInt(256)
    val g = random.nextInt(256)
    val b = random.nextInt(256)
    val hexColor = (0xFF000000L or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong())
    
    val baseInks = listOf("Yellow", "Warm Red", "Rubine Red", "Rhodamine Red", "Purple", "Violet", "Blue 072", "Reflex Blue", "Process Blue", "Green", "Black", "Transparent White", "Orange 021", "Yellow 012", "Trans. Wt.")
    
    val numInks = random.nextInt(2) + 2
    val pickedInks = mutableSetOf<String>()
    while(pickedInks.size < numInks) {
        pickedInks.add(baseInks[random.nextInt(baseInks.size)])
    }
    
    val baseRecipe = mutableListOf<RecipeItem>()
    var remaining = 100.0f
    pickedInks.forEachIndexed { index, ink ->
        if (index == pickedInks.size - 1) {
            baseRecipe.add(RecipeItem(ink, remaining))
        } else {
            val amount = kotlin.math.round((random.nextFloat() * remaining * 0.7f + 5.0f) * 10) / 10f
            baseRecipe.add(RecipeItem(ink, amount))
            remaining -= amount
            remaining = kotlin.math.round(remaining * 10) / 10f
        }
    }
    
    val foilRecipe = baseRecipe.map { 
        it.copy(percentage = kotlin.math.round(it.percentage * 0.8f * 10) / 10f) 
    }.toMutableList()
    foilRecipe.add(RecipeItem("Opaque White", 20.0f))
    
    val foilTotal = foilRecipe.sumOf { it.percentage.toDouble() }.toFloat()
    val normalizedFoil = foilRecipe.map { it.copy(percentage = kotlin.math.round((it.percentage / foilTotal) * 1000) / 10f) }

    return PantoneColor("Pantone $number C", hexColor, baseRecipe, normalizedFoil)
}

enum class SubstrateType { WHITE_PAPER, METALLIC_FOIL, TRANSPARENT_FILM }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PantoneApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PantoneApp() {
    var selectedPantone by remember { mutableStateOf<PantoneColor?>(PantoneLibrary.first()) }
    var selectedSubstrate by remember { mutableStateOf(SubstrateType.WHITE_PAPER) }
    var searchQuery by remember { mutableStateOf("") }
    var isScanning by remember { mutableStateOf(false) }
    
    val filteredPantones = remember(searchQuery) {
        PantoneLibrary.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    if (isScanning) {
        ColorScannerScreen(
            onColorScanned = { scannedPantone ->
                if (scannedPantone != null) {
                    selectedPantone = scannedPantone
                }
                isScanning = false
            },
            onClose = { isScanning = false }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    var showLanguageMenu by remember { mutableStateOf(false) }
                    val context = LocalContext.current
                    Box {
                        IconButton(onClick = { showLanguageMenu = true }) {
                            Icon(Icons.Default.Translate, contentDescription = "Language")
                        }
                        DropdownMenu(
                            expanded = showLanguageMenu,
                            onDismissRequest = { showLanguageMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("English") },
                                onClick = {
                                    showLanguageMenu = false
                                    changeLanguage(context, "en")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Русский") },
                                onClick = {
                                    showLanguageMenu = false
                                    changeLanguage(context, "ru")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Հայերեն") },
                                onClick = {
                                    showLanguageMenu = false
                                    changeLanguage(context, "hy")
                                }
                            )
                        }
                    }
                    IconButton(onClick = { isScanning = true }) {
                        Icon(Icons.Default.Camera, contentDescription = stringResource(R.string.scan_color))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Visual Selected Color Header
            val animatedColor by animateColorAsState(
                targetValue = selectedPantone?.let { Color(it.hexColor) } ?: MaterialTheme.colorScheme.surfaceVariant,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "ColorHeaderAnimation"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .height(if (selectedPantone != null) 160.dp else 100.dp)
                    .background(animatedColor),
                contentAlignment = Alignment.BottomStart
            ) {
                selectedPantone?.let { pantone ->
                    val textColor by animateColorAsState(
                        targetValue = if (Color(pantone.hexColor).luminance() > 0.5f) Color.Black else Color.White,
                        animationSpec = tween(durationMillis = 300),
                        label = "TextColorAnimation"
                    )
                    AnimatedContent(
                        targetState = pantone.name,
                        label = "NameAnimation",
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        }
                    ) { name ->
                        Text(
                            text = name,
                            color = textColor,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                } ?: run {
                    Text(
                        text = stringResource(R.string.no_pantone_selected),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }

            // Selectors
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    FilterChip(
                        selected = selectedSubstrate == SubstrateType.WHITE_PAPER,
                        onClick = { selectedSubstrate = SubstrateType.WHITE_PAPER },
                        label = { Text(stringResource(R.string.white_paper)) }
                    )
                    FilterChip(
                        selected = selectedSubstrate == SubstrateType.METALLIC_FOIL,
                        onClick = { selectedSubstrate = SubstrateType.METALLIC_FOIL },
                        label = { Text(stringResource(R.string.metallic_foil)) }
                    )
                    FilterChip(
                        selected = selectedSubstrate == SubstrateType.TRANSPARENT_FILM,
                        onClick = { selectedSubstrate = SubstrateType.TRANSPARENT_FILM },
                        label = { Text(stringResource(R.string.transparent_film)) }
                    )
                }
            }

            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val isTablet = maxWidth > 600.dp
                if (isTablet) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        PantoneListSection(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            filteredPantones = filteredPantones,
                            selectedPantone = selectedPantone,
                            onPantoneSelected = { selectedPantone = it },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(horizontal = 16.dp)
                        )
                        RecipeSection(
                            selectedPantone = selectedPantone,
                            selectedSubstrate = selectedSubstrate,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(end = 16.dp, bottom = 16.dp)
                        )
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        RecipeSection(
                            selectedPantone = selectedPantone,
                            selectedSubstrate = selectedSubstrate,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        PantoneListSection(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            filteredPantones = filteredPantones,
                            selectedPantone = selectedPantone,
                            onPantoneSelected = { selectedPantone = it },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PantoneListSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filteredPantones: List<PantoneColor>,
    selectedPantone: PantoneColor?,
    onPantoneSelected: (PantoneColor) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filteredPantones, key = { it.name }) { pantone ->
                Box(modifier = Modifier.animateItem()) {
                    PantoneListItem(
                        pantone = pantone,
                        isSelected = pantone == selectedPantone,
                        onClick = { onPantoneSelected(pantone) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeSection(
    selectedPantone: PantoneColor?,
    selectedSubstrate: SubstrateType,
    modifier: Modifier = Modifier
) {
    var totalGramsStr by remember { mutableStateOf("1000") }
    val totalGrams = totalGramsStr.toFloatOrNull() ?: 0f

    ElevatedCard(
        modifier = modifier.animateContentSize(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = stringResource(R.string.recipe_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${stringResource(R.string.ink_formulation)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = totalGramsStr,
                onValueChange = { totalGramsStr = it },
                label = { Text(stringResource(R.string.total_grams)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                singleLine = true
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.inks_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(stringResource(R.string.parts_label), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            
            selectedPantone?.let { pantone ->
                val recipe = if (selectedSubstrate == SubstrateType.METALLIC_FOIL) {
                    pantone.foilRecipe
                } else {
                    pantone.baseRecipe
                }
                
                AnimatedContent(
                    targetState = recipe,
                    label = "RecipeListAnimation",
                    transitionSpec = {
                        fadeIn(tween(300)).togetherWith(fadeOut(tween(300)))
                    }
                ) { targetRecipe ->
                    Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                        targetRecipe.forEach { item ->
                            val grams = (item.percentage / 100f) * totalGrams
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val inkName = if (item.inkName == "Opaque White" || item.inkName == "Trans. Wt.") item.inkName else item.inkName
                                Text(inkName, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${String.format(java.util.Locale.US, "%.1f", grams)} g", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("${item.percentage}%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PantoneListItem(pantone: PantoneColor, isSelected: Boolean, onClick: () -> Unit) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        label = "ListItemContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        label = "ListItemContent"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(pantone.hexColor))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = pantone.name,
            color = contentColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 18.sp
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ColorScannerScreen(onColorScanned: (PantoneColor?) -> Unit, onClose: () -> Unit) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        android.widget.Toast.makeText(context, context.getString(R.string.ai_security_warning), android.widget.Toast.LENGTH_LONG).show()
    }

    if (cameraPermissionState.status.isGranted) {
        CameraPreview(onColorScanned = onColorScanned, onClose = onClose)
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.camera_permission_required))
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Request Permission")
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onClose) {
                Text("Cancel")
            }
        }
    }
}

@Composable
fun CameraPreview(onColorScanned: (PantoneColor?) -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var currentAverageColor by remember { mutableStateOf(android.graphics.Color.WHITE) }
    var latestBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build()
                        .also {
                            it.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                                val bitmap = imageProxy.toBitmap()
                                // Downscale for faster processing/gemini upload
                                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 512, 512, true)
                                latestBitmap = scaledBitmap

                                val cx = bitmap.width / 2
                                val cy = bitmap.height / 2
                                val radius = 10
                                
                                var rSum = 0L
                                var gSum = 0L
                                var bSum = 0L
                                var count = 0
                                
                                for (x in cx - radius..cx + radius) {
                                    for (y in cy - radius..cy + radius) {
                                        if (x >= 0 && x < bitmap.width && y >= 0 && y < bitmap.height) {
                                            val pixel = bitmap.getPixel(x, y)
                                            rSum += android.graphics.Color.red(pixel)
                                            gSum += android.graphics.Color.green(pixel)
                                            bSum += android.graphics.Color.blue(pixel)
                                            count++
                                        }
                                    }
                                }
                                
                                if (count > 0) {
                                    currentAverageColor = android.graphics.Color.rgb((rSum / count).toInt(), (gSum / count).toInt(), (bSum / count).toInt())
                                }
                                
                                imageProxy.close()
                            }
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
                    } catch (exc: Exception) {
                        // Handle error
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = 40.dp.toPx()
            
            drawCircle(
                color = Color.White,
                radius = radius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = Color.Black,
                radius = radius + 2.dp.toPx(),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }
        
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(currentAverageColor))
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isAnalyzing) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Analyzing with AI...", color = Color.White, fontWeight = FontWeight.Bold)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = {
                            // Find closest Pantone color locally
                            val r1 = android.graphics.Color.red(currentAverageColor)
                            val g1 = android.graphics.Color.green(currentAverageColor)
                            val b1 = android.graphics.Color.blue(currentAverageColor)
                            
                            var minDistance = Double.MAX_VALUE
                            var closestPantone = PantoneLibrary.first()
                            
                            for (pantone in PantoneLibrary) {
                                val r2 = (pantone.hexColor shr 16 and 0xFF).toInt()
                                val g2 = (pantone.hexColor shr 8 and 0xFF).toInt()
                                val b2 = (pantone.hexColor and 0xFF).toInt()
                                
                                val distance = sqrt(
                                    (r1 - r2).toDouble().pow(2.0) +
                                    (g1 - g2).toDouble().pow(2.0) +
                                    (b1 - b2).toDouble().pow(2.0)
                                )
                                if (distance < minDistance) {
                                    minDistance = distance
                                    closestPantone = pantone
                                }
                            }
                            onColorScanned(closestPantone)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text(stringResource(R.string.scan_color))
                    }

                    Button(
                        onClick = {
                            latestBitmap?.let { bmp ->
                                isAnalyzing = true
                                coroutineScope.launch {
                                    val pantoneColor = analyzeColorWithGemini(bmp)
                                    isAnalyzing = false
                                    if (pantoneColor != null) {
                                        onColorScanned(pantoneColor)
                                    } else {
                                        android.widget.Toast.makeText(context, "AI could not detect exact Pantone color.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.ai_scan))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            IconButton(onClick = onClose, modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(50))) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}

suspend fun analyzeColorWithGemini(bitmap: Bitmap): PantoneColor? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    val outputStream = java.io.ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
    val base64Image = android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.NO_WRAP)

    val prompt = "Identify the prominent color in the center of this image under the circle reticle. Respond ONLY with the name of the Pantone color (e.g., 'Pantone 109 C', 'Pantone Orange 021 C'). If multiple colors are present, choose the one exactly in the center. If it is not a standard color, pick the closest Pantone match."

    val request = GenerateContentRequest(
        contents = listOf(
            Content(
                parts = listOf(
                    Part(text = prompt),
                    Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                )
            )
        )
    )

    try {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val response = RetrofitClient.service.generateContent(apiKey, request)
        val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
        if (responseText != null) {
            return@withContext PantoneLibrary.find { it.name.equals(responseText, ignoreCase = true) }
                ?: PantoneLibrary.find { responseText.contains(it.name, ignoreCase = true) }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return@withContext null
}
