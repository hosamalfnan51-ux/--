@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.zIndex
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.example.data.repository.Hadith
import com.example.data.repository.HadithRepository
import com.example.data.repository.AdhkarRepository
import com.example.data.repository.DhikrItem
import com.example.data.repository.DuaRepository
import com.example.data.repository.DuaItem
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.HifzPlan
import com.example.data.model.KhatmaRoom
import com.example.data.model.Surah
import com.example.data.model.Verse
import com.example.data.repository.QuranRepository
import com.squareup.moshi.Moshi
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TajweedGreen
import com.example.ui.theme.TajweedRed
import com.example.ui.theme.TajweedYellow
import com.example.ui.viewmodel.ChatMessage
import com.example.ui.viewmodel.QuranViewModel
import com.example.ui.viewmodel.RecitationEvaluation
import com.example.ui.viewmodel.SemanticSearchResultItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "تم تفعيل صلاحية الميكروفون بنجاح", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "تطلب ميزة معلم التلاوة صلاحية الميكروفون للتحليل", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(
                    onRequestPermission = {
                        if (ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.RECORD_AUDIO
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    onRequestPermission: () -> Unit,
    viewModel: QuranViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(0) }
    var showBookmarksDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Navigation and Eye-Care/Night Mode
    val isNightMode = viewModel.isNightMode

    MyApplicationTheme(darkTheme = isNightMode) {
        Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_scaffold"),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = if (viewModel.isEnglishLanguage) "QuranWay" else "طريق القرآن",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.isNightMode = !viewModel.isNightMode }) {
                        Icon(
                            imageVector = if (viewModel.isNightMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "تغيير مظهر العين",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    // Language Switcher Button (تبديل اللغة بسهولة بلمسة واحدة)
                    FilterChip(
                        selected = viewModel.isEnglishLanguage,
                        onClick = { viewModel.isEnglishLanguage = !viewModel.isEnglishLanguage },
                        label = {
                            Text(
                                text = if (viewModel.isEnglishLanguage) "العربية 🌐" else "EN 🌐",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            labelColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    // About App Button (حول التطبيق)
                    IconButton(onClick = { viewModel.showAboutDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "حول التطبيق",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = { showBookmarksDialog = true }) {
                        Icon(
                            imageVector = if (viewModel.bookmarksList.isNotEmpty()) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "العلامات المرجعية",
                            tint = if (viewModel.bookmarksList.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    }
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "إعدادات القارئ",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        },
        bottomBar = {
            val isEng = viewModel.isEnglishLanguage
            val tabs = listOf(
                NavigationItem(if (isEng) "Quran" else "المصحف", Icons.Default.Book, Icons.Outlined.Book),
                NavigationItem(if (isEng) "Adhkar" else "الأذكار", Icons.Default.VolunteerActivism, Icons.Outlined.VolunteerActivism),
                NavigationItem(if (isEng) "Duas" else "الأدعية", Icons.Default.Favorite, Icons.Outlined.FavoriteBorder),
                NavigationItem(if (isEng) "Hadith" else "الأحاديث", Icons.Default.MenuBook, Icons.Outlined.MenuBook),
                NavigationItem(if (isEng) "Recitation" else "معلم التلاوة", Icons.Default.Mic, Icons.Outlined.Mic),
                NavigationItem(if (isEng) "Hifz Planner" else "مدرب الحفظ", Icons.Default.Alarm, Icons.Outlined.Alarm),
                NavigationItem(if (isEng) "AI Tafsir" else "التدبر الذكي", Icons.Default.QuestionAnswer, Icons.Outlined.QuestionAnswer),
                NavigationItem(if (isEng) "Khatma Rooms" else "الختمات", Icons.Default.Groups, Icons.Outlined.Groups)
            )

            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 6.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val isSelected = currentTab == index
                        FilterChip(
                            selected = isSelected,
                            onClick = { currentTab = index },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(20.dp),
                            border = null,
                            modifier = Modifier.testTag("tab_button_$index")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "tab_navigation"
            ) { targetTab ->
                when (targetTab) {
                    0 -> MushafScreen(viewModel)
                    1 -> AdhkarScreen(viewModel)
                    2 -> DuaScreen(viewModel)
                    3 -> HadithScreen(viewModel)
                    4 -> AIRecitationAndSearchScreen(viewModel, onRequestPermission, onNavigateToMushaf = { currentTab = 0 })
                    5 -> HifzPlannerScreen(viewModel)
                    6 -> AITafsirChatScreen(viewModel)
                    7 -> KhatmaRoomsScreen(viewModel)
                }
            }

            if (viewModel.showAboutDialog) {
                AboutAppDialog(onDismiss = { viewModel.showAboutDialog = false })
            }

            if (showBookmarksDialog) {
                BookmarksDialog(
                    viewModel = viewModel,
                    onDismiss = { showBookmarksDialog = false },
                    onJumpToVerse = { surahId, verseNumber ->
                        showBookmarksDialog = false
                        viewModel.jumpToVerseInMushaf(surahId, verseNumber) { currentTab = 0 }
                    }
                )
            }

            if (showSettingsDialog) {
                SettingsDialog(
                    viewModel = viewModel,
                    onDismiss = { showSettingsDialog = false }
                )
            }
        }
    }
}
}

data class NavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

// ==========================================
// SCREEN 1: المصحف الشريف (Mushaf UI/UX)
// ==========================================
@Composable
fun MushafScreen(viewModel: QuranViewModel) {
    val surahsList = QuranRepository.offlineChapters
    var selectedWordDetails by remember { mutableStateOf<String?>(null) }
    var selectedWordArabic by remember { mutableStateOf("") }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var surahExpanded by remember { mutableStateOf(false) }
    var ayahExpanded by remember { mutableStateOf(false) }
    var selectedAyahNumber by remember { mutableStateOf(1) }

    val activeSurah = viewModel.selectedSurah

    LaunchedEffect(activeSurah) {
        selectedAyahNumber = 1
    }

    LaunchedEffect(viewModel.pendingScrollAyahNumber) {
        viewModel.pendingScrollAyahNumber?.let { targetAyah ->
            val surahId = activeSurah?.id ?: 1
            val scrollIndex = if (surahId != 1 && surahId != 9) targetAyah else targetAyah - 1
            listState.animateScrollToItem(maxOf(0, scrollIndex))
            selectedAyahNumber = targetAyah
            viewModel.pendingScrollAyahNumber = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // Horizontally scrollable Surahs chip selector
        Text(
            text = "اختر السورة الكريمة للتلاوة والتدبر",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.End)
                .padding(bottom = 8.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = true
        ) {
            items(surahsList) { surah ->
                val isSelected = viewModel.selectedSurah?.id == surah.id
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectSurah(surah) },
                    label = {
                        Text(
                            text = surah.nameArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("surah_chip_${surah.id}")
                )
            }
        }

        // Narrations & Settings Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Settings Controls: Narration Selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    listOf("حفص", "ورش", "قالون").forEach { narration ->
                        val isSelected = viewModel.selectedNarration == narration
                        TextButton(
                            onClick = { viewModel.selectedNarration = narration },
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                            ),
                            modifier = Modifier
                                .height(32.dp)
                                .padding(horizontal = 2.dp)
                        ) {
                            Text(
                                text = narration,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                Text(
                    text = "رواية المصحف:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Main Display Section of Verses
        if (activeSurah == null) {
            // Placeholder onboarding beautiful screen
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = "Quran Logo",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "طريق القرآن الذكي",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "اختر سورة مباركة لتبدأ رحلة التدبر والتلاوة برواية حفص أو ورش أو قالون مع رصد فوري للتجويد",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Chapter Title Visual Banner
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "سُورَةُ ${activeSurah.nameArabic}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "${activeSurah.revelationPlace} • ${activeSurah.versesCount} آية",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Divider(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Interactive Surah and Ayah Navigation Dropdowns
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Surah Selector
                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("surah_nav_dropdown")
                ) {
                    Card(
                        onClick = { surahExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "قائمة السور",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "السورة: ${activeSurah.nameArabic}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = surahExpanded,
                        onDismissRequest = { surahExpanded = false },
                        modifier = Modifier
                            .width(180.dp)
                            .heightIn(max = 300.dp)
                    ) {
                        surahsList.forEach { surah ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = surah.nameArabic,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                onClick = {
                                    viewModel.selectSurah(surah)
                                    surahExpanded = false
                                }
                            )
                        }
                    }
                }

                // Ayah Selector
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ayah_nav_dropdown")
                ) {
                    Card(
                        onClick = { ayahExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "قائمة الآيات",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "الآية: $selectedAyahNumber",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = ayahExpanded,
                        onDismissRequest = { ayahExpanded = false },
                        modifier = Modifier
                            .width(120.dp)
                            .heightIn(max = 300.dp)
                    ) {
                        (1..activeSurah.versesCount).forEach { ayahNum ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = ayahNum.toString(),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                onClick = {
                                    selectedAyahNumber = ayahNum
                                    ayahExpanded = false
                                    coroutineScope.launch {
                                        val scrollIndex = if (activeSurah.id != 1 && activeSurah.id != 9) ayahNum else ayahNum - 1
                                        listState.animateScrollToItem(maxOf(0, scrollIndex))
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (viewModel.isVersesLoading) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Bismillah display for surahs except Tawbah (chapter 9) and Fatihah (chapter 1)
                    if (activeSurah.id != 1 && activeSurah.id != 9) {
                        item {
                            Text(
                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }

                    items(viewModel.versesList) { verse ->
                        val isPlaying = viewModel.playingVerseId == verse.id && viewModel.isAudioPlaying
                        val isBookmarked = viewModel.isBookmarked(activeSurah.id, verse.verseNumber)
                        VerseItemCard(
                            verse = verse,
                            isPlaying = isPlaying,
                            isBookmarked = isBookmarked,
                            onPlayClick = { viewModel.playAudio(verse) },
                            onWordClick = { word ->
                                selectedWordArabic = word
                                selectedWordDetails = getWordSemanticDetail(word)
                            },
                            onTafsirClick = {
                                viewModel.selectedTafsirVerse = verse
                                viewModel.getAITafsirForAyah(
                                    surahName = activeSurah.nameArabic,
                                    verseNumber = verse.verseNumber,
                                    verseText = verse.textUthmani
                                )
                            },
                            onBookmarkClick = {
                                viewModel.toggleBookmark(
                                    activeSurah.id,
                                    activeSurah.nameArabic,
                                    verse.verseNumber,
                                    verse.textUthmani
                                )
                            }
                        )
                    }
                }
            }

            // Bottom Audio Player Bar
            if (viewModel.playingVerseId != null) {
                Spacer(modifier = Modifier.height(8.dp))
                BottomAudioPlayerBar(viewModel = viewModel, activeSurahName = activeSurah.nameArabic)
            }
        }
    }

    // Animated Slide-Out Tafsir Sidebar Overlay
        AnimatedVisibility(
            visible = viewModel.showTafsirSidebar,
            enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .fillMaxWidth(0.85f)
                .widthIn(max = 380.dp)
                .background(MaterialTheme.colorScheme.surface)
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)))
                .zIndex(10f)
        ) {
            TafsirSidebarContent(
                viewModel = viewModel,
                onClose = { viewModel.showTafsirSidebar = false }
            )
        }
    }

    // Word Details Dialog (Interactive Long-press / click on every word)
    if (selectedWordDetails != null) {
        Dialog(onDismissRequest = { selectedWordDetails = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "مفردات التدبر التفصيلية",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = selectedWordArabic,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = selectedWordDetails ?: "",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { selectedWordDetails = null },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "إغلاق")
                    }
                }
            }
        }
    }
}

@Composable
fun VerseItemCard(
    verse: Verse,
    isPlaying: Boolean,
    isBookmarked: Boolean = false,
    onPlayClick: () -> Unit,
    onWordClick: (String) -> Unit,
    onTafsirClick: () -> Unit,
    onBookmarkClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = if (isPlaying) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Main Authentic Continuous Uthmani Verse Text with End Ornament
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${verse.textUthmani} ﴿${verse.verseNumber}﴾",
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start,
                        style = TextStyle(textDirection = TextDirection.ContentOrRtl),
                        lineHeight = 40.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Interactive Word Analysis Chips (In Native RTL)
                    Text(
                        text = "مفردات الآية الكريمة (انقر للتفسير والتدبر):",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        val words = verse.textUthmani.split(" ")
                        words.forEach { word ->
                            Box(
                                modifier = Modifier
                                    .padding(end = 6.dp, bottom = 6.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                    .border(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .clickable { onWordClick(word) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = word,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Serif,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Translation
            Text(
                text = verse.translation,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPlayClick) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = "الاستماع للآية الكريمة",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onTafsirClick) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "التفسير التفاعلي بالذكاء الاصطناعي",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onBookmarkClick) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "حفظ القراءة",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Text(
                    text = "آية ${verse.verseNumber}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun TafsirSidebarContent(viewModel: QuranViewModel, onClose: () -> Unit) {
    val verse = viewModel.selectedTafsirVerse
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
            }
            Text(
                text = "التدبر والتفسير الذكي",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Divider(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

        if (verse == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "يرجى تحديد آية من المصحف لعرض تفسيرها التدبري الذكي.", color = MaterialTheme.colorScheme.secondary, textAlign = TextAlign.Center)
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "سورة ${viewModel.selectedSurah?.nameArabic ?: ""} - آية ${verse.verseNumber}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.align(Alignment.End)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = verse.textUthmani,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (viewModel.isTafsirLoading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "جارٍ استحضار الدلالات والتفاسير المعتمدة بالذكاء الاصطناعي...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Text(
                        text = viewModel.aiTafsirText ?: "لا يوجد تفسير متاح حالياً.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val surahName = viewModel.selectedSurah?.nameArabic ?: ""
                            viewModel.getAITafsirForAyah(surahName, verse.verseNumber, verse.textUthmani)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "إعادة التوليد", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "تحديث / إعادة المحاولة بالذكاء الاصطناعي 🔄", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable androidx.compose.foundation.layout.FlowRowScope.() -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        content = content
    )
}

// Local offline static dictionary for word-by-word Quran meaning
fun getWordSemanticDetail(word: String): String {
    val clean = word.replace(Regex("[\\p{Punct}]"), "").trim()
    return when {
        clean.contains("الحمد") -> "الحمد: الثناء بالجميل على جهة التبجيل والتعظيم لله عز وجل وحده."
        clean.contains("رب") -> "رب: المالك، السيد، المصلح، المعبود، المتصرف في خلقه بالنعم الإيجاد والإمداد."
        clean.contains("العالمين") -> "العالمين: جمع عالَم، وهم كل ما سوى الله تعالى من الإنس والجن والملائكة والجماد."
        clean.contains("الرحمن") -> "الرحمن: ذو الرحمة الواسعة الشاملة لجميع الخلائق في الدنيا المؤمن والكافر."
        clean.contains("الرحيم") -> "الرحيم: ذو الرحمة الواصلة الخاصة بعباده المؤمنين في الآخرة دون سواهم."
        clean.contains("مالك") -> "مالك: المتصرف المطلق بالسلطان يوم القيامة حيث لا يملك أحد لأحد نفعاً."
        clean.contains("الدين") -> "الدين: الجزاء والحساب، وتأتي بمعنى الانقياد والطاعة لله."
        clean.contains("نعبد") -> "نعبد: نخصك بالذل والخشوع والطاعة والمحبة والتذلل المطلق."
        clean.contains("نستعين") -> "نستعين: نطلب منك وحدك الإعانة والتوفيق في جميع أمور دنيانا وديننا."
        clean.contains("اهدنا") -> "اهدنا: ارشدنا، ووفقنا، وثبتنا على المنهج المستقيم والعمل الصالح."
        clean.contains("الصراط") -> "الصراط: الطريق الواضح السهل الذي لا اعوجاج فيه."
        clean.contains("المستقيم") -> "المستقيم: المعتدل الذي لا انحراف فيه وهو الإسلام والقرآن."
        clean.contains("أحد") -> "أحد: الواحد الفرد الصمد الذي لا شريك له ولا مثيل في ذاته وصفاته."
        clean.contains("الصمد") -> "الصمد: السيد الذي كمل في سؤدده وتقصده جميع الخلائق في حوائجها."
        else -> "الكلمة المباركة: تحمل دلالات بيانية عظيمة مستمدة من بلاغة النظم القرآني الفريد في سياق الآية الكريمة."
    }
}

// ==========================================
// SCREEN 2: معلم التلاوة والبحث الدلالي (AI Recitation & Search)
// ==========================================
@Composable
fun AIRecitationAndSearchScreen(
    viewModel: QuranViewModel,
    onRequestPermission: () -> Unit,
    onNavigateToMushaf: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCoachVerseText by remember { mutableStateOf("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ") }
    var showTajweedLegend by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Feature 1: البحث الدلالي الذكي
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "البحث الدلالي الذكي (AI)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.End)
                    )
                    Text(
                        text = "ابحث بالمعنى أو الفكرة مثل 'آيات الطمأنينة' أو 'الصبر'",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.align(Alignment.End),
                        textAlign = TextAlign.Right
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("semantic_search_input"),
                        placeholder = { Text("أدخل موضوع البحث (مثال: فضل العلم)") },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.performSemanticSearch(searchQuery) }) {
                                Icon(imageVector = Icons.Default.Search, contentDescription = "بحث")
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { viewModel.performSemanticSearch(searchQuery) }),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                searchQuery = "آيات تبث الطمأنينة"
                                viewModel.performSemanticSearch(searchQuery)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("آيات الطمأنينة", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Button(
                            onClick = {
                                searchQuery = "الصبر ومواجهة الابتلاء"
                                viewModel.performSemanticSearch(searchQuery)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("الصبر والابتلاء", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Semantic Search results display
        if (viewModel.isSemanticSearchLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        } else if (viewModel.semanticSearchResults.isNotEmpty()) {
            item {
                Text(
                    text = "نتائج البحث الدلالي الذكي:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            items(viewModel.semanticSearchResults) { result ->
                SemanticSearchResultCard(
                    result = result,
                    onJumpClick = {
                        viewModel.jumpToVerseInMushaf(result.surahId, result.verseNumber) {
                            onNavigateToMushaf()
                        }
                    }
                )
            }
        }

        // Feature 2: معلم التلاوة الذكي
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "معلم التلاوة الذكي (Coach)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.End)
                    )
                    Text(
                        text = "اقرأ بصوتك وسيقوم الذكاء الاصطناعي برصد أحكام التجويد ومخارج الحروف فوراً",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.align(Alignment.End),
                        textAlign = TextAlign.Right
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Text of the selected verse for coaching
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                    ) {
                        Text(
                            text = selectedCoachVerseText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Custom Verses selection for coaching
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "اختر الآية الكريمة للتجربة:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        reverseLayout = true
                    ) {
                        val demoVerses = listOf(
                            "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                            "قُلْ هُوَ اللَّهُ أَحَدٌ",
                            "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ"
                        )
                        items(demoVerses) { txt ->
                            FilterChip(
                                selected = selectedCoachVerseText == txt,
                                onClick = { selectedCoachVerseText = txt },
                                label = { Text(txt, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Microphone interaction button
                    if (viewModel.isRecordingEvaluation) {
                        // Live wave animation
                        Text(text = "جارٍ تحليل مخارج الحروف وقواعد التجويد بذكاء...", fontSize = 12.sp, color = TajweedYellow)
                        Spacer(modifier = Modifier.height(8.dp))
                        AnimatedWaveform()
                    } else if (viewModel.isRecordingAudio) {
                        // Recording state
                        Text(text = "التسجيل مستمر... اضغط على الزر للإيقاف والتحليل", fontSize = 12.sp, color = TajweedRed, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                viewModel.stopRecitationRecordingAndEvaluate(selectedCoachVerseText)
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = TajweedRed),
                            modifier = Modifier
                                .size(72.dp)
                                .testTag("coach_stop_button")
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = "إيقاف وتوجيه للذكاء الاصطناعي", modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "جارٍ تسجيل تلاوتك الفعلية...", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    } else {
                        Button(
                            onClick = {
                                onRequestPermission()
                                viewModel.startRecitationRecording(selectedCoachVerseText)
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .size(72.dp)
                                .testTag("coach_record_button")
                        ) {
                            Icon(imageVector = Icons.Default.Mic, contentDescription = "تسجيل", modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "انقر للبدء بتسجيل تلاوتك الفردية", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    }

                    // Display Recitation Analysis Evaluation
                    viewModel.recitationEvaluation?.let { evaluation ->
                        Spacer(modifier = Modifier.height(16.dp))
                        RecitationEvaluationResultCard(evaluation)
                    }
                }
            }
        }
    }
}

@Composable
fun SemanticSearchResultCard(
    result: SemanticSearchResultItem,
    onJumpClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${result.surahName} • آية ${result.verseNumber}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "تطابق دلالي ذكي",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = result.textUthmani,
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = result.relevanceReason,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onJumpClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(imageVector = Icons.Default.Book, contentDescription = "قراءة", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "عرض الآية بالمصحف الشريف", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun RecitationEvaluationResultCard(evaluation: RecitationEvaluation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (evaluation.overallScore >= 90) TajweedGreen.copy(alpha = 0.15f)
                            else TajweedYellow.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${evaluation.overallScore}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (evaluation.overallScore >= 90) TajweedGreen else TajweedYellow
                    )
                }

                Text(
                    text = "تقرير معلم التلاوة الذكي",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Color coded feedback word-by-word
            Text(
                text = "الرصد الصوتي التجويدي:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.align(Alignment.End)
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                evaluation.feedbackWords.forEach { item ->
                    val color = when (item.colorCode) {
                        "green" -> TajweedGreen
                        "yellow" -> TajweedYellow
                        "red" -> TajweedRed
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .background(color.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = item.word,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                        item.tajweedRule?.let { rule ->
                            Text(
                                text = rule,
                                fontSize = 8.sp,
                                color = color,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = evaluation.generalFeedback,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun AnimatedWaveform() {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    Row(
        modifier = Modifier
            .height(40.dp)
            .fillMaxWidth(0.6f),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val count = 12
        for (i in 0 until count) {
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = (400..800).random(), easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$i"
            )

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(scale)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

// ==========================================
// SCREEN 3: مدرب الحفظ المرن (Smart Hifz Planner)
// ==========================================
@Composable
fun HifzPlannerScreen(viewModel: QuranViewModel) {
    val plans by viewModel.hifzPlans.collectAsState()
    val allProgress by viewModel.allProgress.collectAsState()
    
    val totalMemorized = plans.sumOf { maxOf(0, it.currentProgressAyah - it.startAyah + 1) }
    val masteredCount = allProgress.count { it.status == 2 }
    val underReviewCount = allProgress.count { it.status == 1 }
    val dueCount = plans.sumOf { plan ->
        val planProg = allProgress.filter { it.planId == plan.id }
        (plan.startAyah..plan.currentProgressAyah).count { ayahId ->
            val prog = planProg.find { it.ayahId == ayahId }
            prog == null || prog.nextReviewDate <= System.currentTimeMillis()
        }
    }

    var showCreateDialog by remember { mutableStateOf(false) }

    // Dialog inputs
    var title by remember { mutableStateOf("") }
    var surahId by remember { mutableStateOf(67) } // Default Al-Mulk
    var startAyah by remember { mutableStateOf(1) }
    var endAyah by remember { mutableStateOf(30) }
    var daysDuration by remember { mutableStateOf(10) }
    var dailyAmount by remember { mutableStateOf(3) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_plan_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة خطة")
            }
        },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "مدرب الحفظ والمراجعة المرن",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "يقوم الذكاء الاصطناعي بتنظيم تلاواتك وإعادة الجدولة دون إرهاق عند التوقف",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Beautiful top-level stats visualizer
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "لوحة إنجازات الحفظ والمراجعة المتباعدة",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.End)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Mastered Stats
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = "مُتقَن 🟢", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = masteredCount.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TajweedGreen)
                        }
                        
                        // Under Review Stats
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = "مراجعة 🟡", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = underReviewCount.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TajweedYellow)
                        }
                        
                        // Due Stats
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = "مستحق 🔴", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = dueCount.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TajweedRed)
                        }

                        // Total Stats
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = "حفظ 📖", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = totalMemorized.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            FilterChip(
                selected = viewModel.isTestMemoryModeEnabled,
                onClick = { viewModel.isTestMemoryModeEnabled = !viewModel.isTestMemoryModeEnabled },
                label = {
                    Text(
                        text = if (viewModel.isTestMemoryModeEnabled) "وضع الاختبار (إخفاء الكلمات) 👁️" else "اختبار الحفظ الذاتي 👁️‍🗨️",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (viewModel.isTestMemoryModeEnabled) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "اختبار الحفظ",
                        modifier = Modifier.size(16.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (plans.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "لا توجد خطط حفظ نشطة حالياً. اضغط على الزر + في الأسفل لإضافة خطة حفظ مرنة.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Adaptive Coach Advice Banner
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "توجيه المدرب المرن: في حال واجهت يوماً شاقاً، لا بأس بالاكتفاء بنصف الورد اليومي وسأقوم بإعادة جدولة الورد تلقائياً لتجنب تراكم الحفظ.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.TipsAndUpdates,
                                    contentDescription = "نصيحة",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    items(plans) { plan ->
                        HifzPlanProgressCard(plan, viewModel)
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        Dialog(onDismissRequest = { showCreateDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "تخصيص خطة حفظ مرنة جديدة", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان الخطة (مثال: حفظ سورة الملك)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Surah Selection drop mock
                    OutlinedTextField(
                        value = surahId.toString(),
                        onValueChange = { surahId = it.toIntOrNull() ?: 67 },
                        label = { Text("رقم السورة الكريمة (1-114)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = startAyah.toString(),
                            onValueChange = { startAyah = it.toIntOrNull() ?: 1 },
                            label = { Text("من آية") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = endAyah.toString(),
                            onValueChange = { endAyah = it.toIntOrNull() ?: 30 },
                            label = { Text("إلى آية") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = daysDuration.toString(),
                            onValueChange = { daysDuration = it.toIntOrNull() ?: 10 },
                            label = { Text("المدة (أيام)") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = dailyAmount.toString(),
                            onValueChange = { dailyAmount = it.toIntOrNull() ?: 3 },
                            label = { Text("الورد اليومي (آية)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showCreateDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء")
                        }

                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    viewModel.createHifzPlan(title, surahId, startAyah, endAyah, daysDuration, dailyAmount)
                                    showCreateDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Text("إنشاء الخطة")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HifzPlanProgressCard(plan: HifzPlan, viewModel: QuranViewModel) {
    val totalAyahs = plan.endAyah - plan.startAyah + 1
    val progressCount = maxOf(0, plan.currentProgressAyah - plan.startAyah + 1)
    val percentage = if (totalAyahs > 0) progressCount.toFloat() / totalAyahs.toFloat() else 0f

    val allProgress by viewModel.allProgress.collectAsState()
    val planProgress = allProgress.filter { it.planId == plan.id }

    val dueVerses = (plan.startAyah..plan.currentProgressAyah).filter { ayahId ->
        val progress = planProgress.find { it.ayahId == ayahId }
        progress == null || progress.nextReviewDate <= System.currentTimeMillis()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.deletePlan(plan.id) }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف الخطة", tint = TajweedRed.copy(alpha = 0.7f))
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = plan.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(text = "سورة ${plan.surahName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = percentage,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "${(percentage * 100).toInt()}% مكتمل", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                Text(text = "$progressCount من $totalAyahs آية", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action: log daily progress
            if (!plan.isCompleted) {
                val nextAyah = plan.currentProgressAyah + 1
                Button(
                    onClick = { viewModel.logHifzProgress(plan, nextAyah, 0) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("log_progress_button_${plan.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "تم", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "أنجزت حفظ الآية $nextAyah اليوم")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "الحمد لله! اكتملت الخطة بنجاح 🎉",
                        fontWeight = FontWeight.Bold,
                        color = TajweedGreen,
                        fontSize = 13.sp
                    )
                }
            }

            // Smart Spaced Repetition Panel inside plan card
            if (dueVerses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "المراجعة المتباعدة الذكية (مستحق الآن):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.End)
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    reverseLayout = true
                ) {
                    items(dueVerses) { ayahNum ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                            modifier = Modifier.width(240.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val progress = planProgress.find { it.ayahId == ayahNum }
                                    val reps = progress?.repetitions ?: 0
                                    Text(
                                        text = "تكرار: $reps",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = "آية $ayahNum",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(6.dp))

                                if (viewModel.isTestMemoryModeEnabled) {
                                    var isRevealed by remember { mutableStateOf(false) }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .background(
                                                if (isRevealed) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                                else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable { isRevealed = !isRevealed }
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isRevealed) "الآية $ayahNum من سورة ${plan.surahName}" else "انقر لكشف نص الآية 🙈 ➔ 👁️",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                                
                                // Display evaluation rating buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Button(
                                        onClick = { viewModel.reviewAyah(plan.id, plan.surahId, ayahNum, 2) },
                                        colors = ButtonDefaults.buttonColors(containerColor = TajweedGreen),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("أتقنت", fontSize = 10.sp, color = Color.White)
                                    }
                                    Button(
                                        onClick = { viewModel.reviewAyah(plan.id, plan.surahId, ayahNum, 1) },
                                        colors = ButtonDefaults.buttonColors(containerColor = TajweedYellow),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("بصعوبة", fontSize = 10.sp, color = Color.Black)
                                    }
                                    Button(
                                        onClick = { viewModel.reviewAyah(plan.id, plan.surahId, ayahNum, 0) },
                                        colors = ButtonDefaults.buttonColors(containerColor = TajweedRed),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("نسيت", fontSize = 10.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 4: مساعد التدبر الحواري (Tafsir Chatbot)
// ==========================================
@Composable
fun AITafsirChatScreen(viewModel: QuranViewModel) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    var inputQuery by remember { mutableStateOf("") }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    // Scroll to bottom when message log changes
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "مساعد التدبر الحواري (AI)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "تفسير موثوق ومفصل للآيات والكلمات بالذكاء الاصطناعي",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Chat Log
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatMessages) { msg ->
                ChatBubble(msg)
            }

            if (viewModel.isChatLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.padding(end = 48.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "مساعد التدبر يتأمل معاني التفسير...", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Preset Quick Prompts
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = true
        ) {
            val presets = listOf(
                "ما معنى الصمد في سورة الإخلاص؟",
                "ما سبب نزول سورة الملك؟",
                "ما المقصود بالفلق؟"
            )
            items(presets) { queryText ->
                FilterChip(
                    selected = false,
                    onClick = {
                        inputQuery = queryText
                        viewModel.sendChatMessage(queryText)
                        inputQuery = ""
                    },
                    label = { Text(queryText, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Query input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    viewModel.sendChatMessage(inputQuery)
                    inputQuery = ""
                },
                modifier = Modifier
                    .testTag("send_chat_button")
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "إرسال",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = { Text("اطرح تساؤلاً حول معاني آية أو سورة...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input"),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    viewModel.sendChatMessage(inputQuery)
                    inputQuery = ""
                }),
                singleLine = true
            )
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val bubbleColor = if (msg.isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (msg.isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val arrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
    val paddingSide = if (msg.isUser) Modifier.padding(start = 48.dp) else Modifier.padding(end = 48.dp)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = arrangement
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (msg.isUser) 16.dp else 4.dp,
                bottomEnd = if (msg.isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            modifier = paddingSide
        ) {
            Text(
                text = msg.text,
                color = textColor,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

// ==========================================
// SCREEN 5: غرف الختمات الجماعية (Global Khatmas)
// ==========================================
@Composable
fun KhatmaRoomsScreen(viewModel: QuranViewModel) {
    val rooms by viewModel.khatmaRooms.collectAsState()
    var showCreateRoomDialog by remember { mutableStateOf(false) }

    // Dialog Input
    var title by remember { mutableStateOf("") }
    var durationDays by remember { mutableStateOf(30) }
    var selectedKhatmaForGrid by remember { mutableStateOf<KhatmaRoom?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateRoomDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "بدء ختمة جديدة")
            }
        },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "غرف الختمات القرآنية الجماعية",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "أطلق خطة تلاوة جماعية ووزع الأجزاء لمتابعة الختمة في تواصل عالمي مبارك",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.showKhatmaDuaaDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            ) {
                Icon(imageVector = Icons.Default.MenuBook, contentDescription = "دعاء الختم", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "دعاء ختم القرآن الكريم 🤲", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }

            if (viewModel.showKhatmaDuaaDialog) {
                KhatmaDuaaDialog(onDismiss = { viewModel.showKhatmaDuaaDialog = false })
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (rooms.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "لا توجد ختمات نشطة حالياً. انقر على الزر لإطلاق أول ختمة جماعية.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(rooms) { room ->
                        KhatmaRoomCard(
                            room = room,
                            onViewGridClick = { selectedKhatmaForGrid = room }
                        )
                    }
                }
            }
        }
    }

    if (showCreateRoomDialog) {
        Dialog(onDismissRequest = { showCreateRoomDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "إنشاء غرفة ختمة جديدة", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان الختمة المباركة") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = durationDays.toString(),
                        onValueChange = { durationDays = it.toIntOrNull() ?: 30 },
                        label = { Text("المدة المستهدفة (أيام)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showCreateRoomDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء")
                        }

                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    viewModel.createKhatmaRoom(title, "أنت", durationDays)
                                    showCreateRoomDialog = false
                                    title = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Text("إطلاق الختمة")
                        }
                    }
                }
            }
        }
    }

    // Juz Grid Bottom Sheet / Dialog for Claiming Sections
    selectedKhatmaForGrid?.let { room ->
        Dialog(onDismissRequest = { selectedKhatmaForGrid = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "جدول تقسيم الختمة (30 جزءاً)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "اختر جزءاً غير محجوز لتلتزم بقراءته",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simple representation parsing json claimed juz
                    val moshi = Moshi.Builder().build()
                    val type = com.squareup.moshi.Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
                    val adapter = moshi.adapter<Map<String, String>>(type)
                    val claimedMap = try {
                        adapter.fromJson(room.claimedJuzListJson) ?: emptyMap()
                    } catch (e: Exception) {
                        emptyMap()
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(30) { index ->
                            val juzNum = index + 1
                            val claimant = claimedMap[juzNum.toString()]
                            val isClaimed = claimant != null

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .clickable(!isClaimed) {
                                        viewModel.claimJuzInKhatma(room, juzNum, "أنت")
                                        // Refresh current dialog reference
                                        selectedKhatmaForGrid = null
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isClaimed) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isClaimed) MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(text = "الجزء $juzNum", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = claimant ?: "غير محجوز",
                                        fontSize = 10.sp,
                                        color = if (isClaimed) MaterialTheme.colorScheme.primary else TajweedGreen,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { selectedKhatmaForGrid = null },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("العودة")
                    }
                }
            }
        }
    }
}

@Composable
fun KhatmaRoomCard(room: KhatmaRoom, onViewGridClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${room.participantCount} مشارك عالمي",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = room.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(text = "بواسطة ${room.creatorName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = room.progressPercentage / 100f,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "${room.progressPercentage.toInt()}% مكتمل", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                Text(text = "تبقى ${room.targetDays} أيام", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onViewGridClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.GridOn, contentDescription = "الأجزاء", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "عرض تقسيم الأجزاء والمشاركة")
            }
        }
    }
}

// ==========================================
// HELPER DIALOGS & COMPONENTS
// ==========================================
@Composable
fun BookmarksDialog(
    viewModel: QuranViewModel,
    onDismiss: () -> Unit,
    onJumpToVerse: (Int, Int) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "العلامات المرجعية وآخر قراءة",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Last Read Banner
                val lastRead = viewModel.lastReadBookmark
                if (lastRead != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "موضع آخر قراءة 📌",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "سورة ${lastRead.surahName} - آية ${lastRead.verseNumber}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = lastRead.textUthmani,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Serif,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { onJumpToVerse(lastRead.surahId, lastRead.verseNumber) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                            ) {
                                Text("الانتقال لآخر قراءة", fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = "قائمة الآيات المحفوظة (${viewModel.bookmarksList.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.align(Alignment.End)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (viewModel.bookmarksList.isEmpty()) {
                    Text(
                        text = "لا توجد علامات مرجعية محفوظة حالياً. انقر على أيقونة العلامة المرجعية بجانب أي آية لحفظها.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.bookmarksList) { item ->
                            Card(
                                onClick = { onJumpToVerse(item.surahId, item.verseNumber) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { viewModel.toggleBookmark(item.surahId, item.surahName, item.verseNumber, item.textUthmani) }
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = TajweedRed)
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "${item.surahName} • آية ${item.verseNumber}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = item.textUthmani,
                                            fontSize = 13.sp,
                                            fontFamily = FontFamily.Serif,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إغلاق")
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(
    viewModel: QuranViewModel,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "إعدادات القراءة والإنصات",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "اختر القارئ المفضل للإنصات والتكرار:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.align(Alignment.End)
                )

                Spacer(modifier = Modifier.height(8.dp))

                val reciters = listOf(
                    "الشيخ مشاري العفاسي",
                    "الشيخ محمود خليل الحصري",
                    "الشيخ عبد الباسط عبد الصمد",
                    "الشيخ محمد صديق المنشاوي"
                )

                reciters.forEach { reciter ->
                    val isSelected = viewModel.selectedReciter == reciter
                    Card(
                        onClick = { viewModel.selectedReciter = reciter },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSelected) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = "محدد", tint = MaterialTheme.colorScheme.primary)
                            } else {
                                Spacer(modifier = Modifier.width(24.dp))
                            }
                            Text(
                                text = reciter,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("حفظ وإغلاق")
                }
            }
        }
    }
}

@Composable
fun BottomAudioPlayerBar(
    viewModel: QuranViewModel,
    activeSurahName: String
) {
    val currentVerseId = viewModel.playingVerseId ?: return
    val currentVerse = viewModel.versesList.find { it.id == currentVerseId } ?: return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.playPreviousVerse() }) {
                    Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "الآية السابقة", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                IconButton(
                    onClick = { viewModel.isAudioPlaying = !viewModel.isAudioPlaying },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = if (viewModel.isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "تشغيل/إيقاف",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                IconButton(onClick = { viewModel.playNextVerse() }) {
                    Icon(imageVector = Icons.Default.SkipNext, contentDescription = "الآية التالية", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${viewModel.selectedReciter} • $activeSurahName (آية ${currentVerse.verseNumber})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = currentVerse.textUthmani,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun KhatmaDuaaDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "دعاء ختم القرآن الكريم المبارك",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "صَدَقَ اللهُ العَظِيمُ الَّذِي لاَ إِلَهَ إِلاَّ هُوَ الحَيُّ القَيُّومُ..\n\n" +
                                "اللَّهُمَّ ارْحَمْنِي بِالقُرْآنِ وَاجْعَلْهُ لِي إِمَاماً وَنُوراً وَهُدًى وَرَحْمَةً..\n\n" +
                                "اللَّهُمَّ ذَكِّرْنِي مِنْهُ مَا نَسِيتُ وَعَلِّمْنِي مِنْهُ مَا جَهِلْتُ وَارْزُقْنِي تِلاَوَتَهُ آنَاءَ اللَّيْلِ وَأَطْرَافَ النَّهَارِ وَاجْعَلْهُ لِي حُجَّةً يَا رَبَّ العَالَمِينَ..\n\n" +
                                "اللَّهُمَّ أَصْلِحْ لِي دِينِي الَّذِي هُوَ عِصْمَةُ أَمْرِي، وَأَصْلِحْ لِي دُنْيَايَ الَّتِي فِيهَا مَعَاشِي، وَأَصْلِحْ لِي آخِرَتِي الَّتِي فِيهَا مَعَادِي، وَاجْعَلِ الحَيَاةَ زِيَادَةً لِي فِي كُلِّ خَيْرٍ وَاجْعَلِ المَوْتَ رَاحَةً لِي مِنْ كُلِّ شَرٍّ..\n\n" +
                                "اللَّهُمَّ اجْعَلْ خَيْرَ عُمْرِي آخِرَهُ وَخَيْرَ عَمَلِي خَوَاتِمَهُ وَخَيْرَ أَيَّامِي يَوْمَ أَلْقَاكَ فِيهِ.. آمين يا رب العالمين.",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Serif,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("تقبل الله منا ومنكم")
                }
            }
        }
    }
}

@Composable
fun HadithScreen(viewModel: QuranViewModel) {
    val context = LocalContext.current
    val chapters = HadithRepository.chapters
    val filteredList = viewModel.filteredHadiths

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "قسم الأحاديث النبوية الصحيحة 📖",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "موسوعة ٥٠٠ حديث شريف صحيح مقسمة على الأبواب الفقهية والتربوية مع الشرح والفوائد",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "Hadith Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = viewModel.hadithSearchQuery,
            onValueChange = { viewModel.hadithSearchQuery = it },
            placeholder = { Text("ابحث في نص الحديث، الراوي، أو الشرح...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
            trailingIcon = {
                if (viewModel.hadithSearchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.hadithSearchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "مسح")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Category / Chapter Filter Chips
        Text(text = "اختر الباب الفقهي / الموضوع:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(chapters) { chapter ->
                val isSelected = viewModel.selectedHadithChapter == chapter
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectedHadithChapter = chapter },
                    label = { Text(chapter, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "الأحاديث المعروضة (${filteredList.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            if (viewModel.favoriteHadithIds.isNotEmpty()) {
                Text(
                    text = "❤️ المفضلة: ${viewModel.favoriteHadithIds.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لم يتم العثور على أحاديث تطابق كلمة البحث الحالية.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredList, key = { it.id }) { hadith ->
                    val isFavorite = viewModel.favoriteHadithIds.contains(hadith.id)
                    HadithItemCard(
                        hadith = hadith,
                        isFavorite = isFavorite,
                        onFavoriteClick = { viewModel.toggleHadithFavorite(hadith.id) },
                        onCopyClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Hadith", "${hadith.text}\n(${hadith.narrator})\nالمصدر: ${hadith.chapter}")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ الحديث الشريف إلى الحافظة بنجاح ✨", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HadithItemCard(
    hadith: Hadith,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onCopyClick: () -> Unit
) {
    var showExplanation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Chapter Header Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "📌 ${hadith.chapter}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "حديث #${hadith.id}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hadith Text
            Text(
                text = "« ${hadith.text} »",
                fontSize = 17.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 28.sp,
                textAlign = TextAlign.Start,
                style = TextStyle(textDirection = TextDirection.ContentOrRtl),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Narrator Tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "صحيح",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "التخريج والدرجة: ${hadith.narrator}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            if (showExplanation) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "💡 الفائدة وشرح الحديث الشريف:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = hadith.explanation,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { showExplanation = !showExplanation }) {
                    Icon(
                        imageVector = if (showExplanation) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "شرح",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showExplanation) "إخفاء الشرح" else "الشرح والفوائد 💡",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCopyClick) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "نسخ الحديث",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "المفضلة",
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// قسم الأذكار الشامل (حصن المسلم)
// ==========================================
@Composable
fun AdhkarScreen(viewModel: QuranViewModel) {
    val context = LocalContext.current
    val categories = AdhkarRepository.categories
    val isEnglish = viewModel.isEnglishLanguage

    // Filtering logic
    val filteredAdhkar = remember(
        viewModel.selectedAdhkarCategory,
        viewModel.adhkarSearchQuery
    ) {
        AdhkarRepository.adhkarList.filter { dhikr ->
            val matchCategory = viewModel.selectedAdhkarCategory == "الكل" || dhikr.category == viewModel.selectedAdhkarCategory
            val matchQuery = viewModel.adhkarSearchQuery.isEmpty() ||
                    dhikr.text.contains(viewModel.adhkarSearchQuery, ignoreCase = true) ||
                    dhikr.rewardOrVirtue.contains(viewModel.adhkarSearchQuery, ignoreCase = true) ||
                    dhikr.reference.contains(viewModel.adhkarSearchQuery, ignoreCase = true)
            matchCategory && matchQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEnglish) "Fortress of the Muslim • Adhkar 📿" else "حصن المسلم - موسوعة الأذكار 📿",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isEnglish) "Morning, Evening, Prayer, Mosque, Home, Sleep & Daily Protection Adhkar with interactive counter"
                        else "أذكار الصباح والمساء، قبل وبعد الصلاة، المسجد، المنزل، النوم، الطعام والسفر مع عداد إلكتروني تفاعلي",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.VolunteerActivism,
                    contentDescription = "Adhkar Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Field
        OutlinedTextField(
            value = viewModel.adhkarSearchQuery,
            onValueChange = { viewModel.adhkarSearchQuery = it },
            placeholder = { Text(if (isEnglish) "Search Adhkar text or virtues..." else "ابحث في نص الذكر، الفضل، أو المناسبة...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (viewModel.adhkarSearchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.adhkarSearchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Category Chips
        Text(
            text = if (isEnglish) "Select Category:" else "اختر القسم الفقهي للأذكار:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                val isSelected = viewModel.selectedAdhkarCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectedAdhkarCategory = category },
                    label = { Text(category, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isEnglish) "Displayed Adhkar (${filteredAdhkar.size})" else "الأذكار المعروضة (${filteredAdhkar.size})",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredAdhkar.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isEnglish) "No Adhkar found for current search query." else "لم يتم العثور على أذكار تطابق كلمة البحث الحالية.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredAdhkar, key = { it.id }) { dhikr ->
                    DhikrItemCard(
                        dhikr = dhikr,
                        viewModel = viewModel,
                        onCopyClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Dhikr", "${dhikr.text}\n(${dhikr.rewardOrVirtue})\nالمصدر: ${dhikr.reference}")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ الذكر الشريف إلى الحافظة بنجاح ✨", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DhikrItemCard(
    dhikr: DhikrItem,
    viewModel: QuranViewModel,
    onCopyClick: () -> Unit
) {
    val remainingCount = viewModel.getDhikrRemainingCount(dhikr.id, dhikr.count)
    val isCompleted = remainingCount == 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            1.dp,
            if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Category Badge & Reference
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "📌 ${dhikr.category}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = dhikr.reference,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dhikr Main Text
            Text(
                text = dhikr.text,
                fontSize = 17.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 28.sp,
                textAlign = TextAlign.Start,
                style = TextStyle(textDirection = TextDirection.ContentOrRtl),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Virtue / Reward Card
            if (dhikr.rewardOrVirtue.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "الفضل",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = dhikr.rewardOrVirtue,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Counter & Action Buttons Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCopyClick) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "نسخ الذكر",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (remainingCount < dhikr.count) {
                        TextButton(onClick = { viewModel.resetDhikrCount(dhikr.id, dhikr.count) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "إعادة", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("إعادة", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // Main Counter Button
                    Button(
                        onClick = { viewModel.decrementDhikrCount(dhikr.id, dhikr.count) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (isCompleted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isCompleted) {
                            Icon(Icons.Default.Check, contentDescription = "مكتمل", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "تم بحمد الله ✨", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text(
                                text = "التكرار المتبقي: $remainingCount / ${dhikr.count}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// قسم الأدعية الشاملة (القرآن والسنة)
// ==========================================
@Composable
fun DuaScreen(viewModel: QuranViewModel) {
    val context = LocalContext.current
    val categories = DuaRepository.categories
    val isEnglish = viewModel.isEnglishLanguage

    // Filtering logic
    val filteredDuas = remember(
        viewModel.selectedDuaCategory,
        viewModel.duaSearchQuery
    ) {
        DuaRepository.duaList.filter { dua ->
            val matchCategory = viewModel.selectedDuaCategory == "الكل" || dua.category == viewModel.selectedDuaCategory
            val matchQuery = viewModel.duaSearchQuery.isEmpty() ||
                    dua.title.contains(viewModel.duaSearchQuery, ignoreCase = true) ||
                    dua.text.contains(viewModel.duaSearchQuery, ignoreCase = true) ||
                    dua.reference.contains(viewModel.duaSearchQuery, ignoreCase = true)
            matchCategory && matchQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEnglish) "Comprehensive Duas & Supplications 🤲" else "موسوعة الأدعية القرآنية والنبوية 🤲",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isEnglish) "Complete authentic supplications from the Holy Quran, Prophet's Sunnah, and comprehensive prayers"
                        else "جميع الأدعية المباركة التي وردت في القرآن الكريم والسنة النبوية الشريفة وأدعية الشفاء والرزق والفرج",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Dua Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Field
        OutlinedTextField(
            value = viewModel.duaSearchQuery,
            onValueChange = { viewModel.duaSearchQuery = it },
            placeholder = { Text(if (isEnglish) "Search Duas by subject, text or source..." else "ابحث في عنوان الدعاء، النص، أو المصدر...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (viewModel.duaSearchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.duaSearchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Category Chips
        Text(
            text = if (isEnglish) "Select Category:" else "اختر تصنيف الدعاء:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                val isSelected = viewModel.selectedDuaCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectedDuaCategory = category },
                    label = { Text(category, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isEnglish) "Displayed Supplications (${filteredDuas.size})" else "الأدعية المعروضة (${filteredDuas.size})",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredDuas.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isEnglish) "No supplications found for current search." else "لم يتم العثور على أدعية تطابق كلمة البحث الحالية.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredDuas, key = { it.id }) { dua ->
                    DuaItemCard(
                        dua = dua,
                        isEnglish = isEnglish,
                        onCopyClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Dua", "${dua.title}\n${dua.text}\nالمصدر: ${dua.reference}")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ الدعاء الشريف إلى الحافظة بنجاح ✨", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DuaItemCard(
    dua: DuaItem,
    isEnglish: Boolean,
    onCopyClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Category & Reference
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "📖 ${dua.title}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = dua.reference,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Dua Text
            Text(
                text = dua.text,
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 30.sp,
                textAlign = TextAlign.Start,
                style = TextStyle(textDirection = TextDirection.ContentOrRtl),
                modifier = Modifier.fillMaxWidth()
            )

            // English Translation if available or English mode
            if (dua.translationEnglish.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = dua.translationEnglish,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCopyClick) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "نسخ الدعاء",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// نافذة حول التطبيق والمعلومات الإيمانية
// ==========================================
@Composable
fun AboutAppDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Emblem Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = "QuranWay",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "طريق القرآن • QuranWay",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "تطبيق إسلامي كامل وشامل للقرآن الكريم والأذكار والأدعية",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

                Spacer(modifier = Modifier.height(16.dp))

                // Creator & Founder Info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = "المؤسس", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "مؤسس وصانع التطبيق بواسطة:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "حسام حسين أحمد توفيق",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = "التواصل", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "للتواصل:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "01015059150",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = "الموقع", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "الموقع:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "قنا / نجع حمادي / المصالحه",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dedication & Dua Text
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "دعاء وإهداء التطبيق 🤲",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "اسأل الله العظيم رب العرش العظيم أن يجعل هذا العمل خالص لوجهه الكريم وأن يجعله نافعًا للجميع وأن يجعله في ميزان حسناتي\n\n" +
                                    "اللهم صلي وسلم وبارك على سيدنا محمد وعلى آله وصحبه وسلم تسليما كثيرا طيبا مباركا فيه\n\n" +
                                    "اللهم ارحم أمواتنا وأموات المسلمين اجمعين\n\n" +
                                    "هذا التطبيق صدقه جاريه علي كل من أحب .",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("إغلاق Window", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
