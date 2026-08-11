package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Poem
import com.example.data.model.PoemCategory
import com.example.data.model.Poet
import com.example.ui.components.PashtoPoemCard
import com.example.ui.components.RtlLayout
import com.example.ui.theme.PashtoGold
import com.example.ui.viewmodel.PoetryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PoetryViewModel,
    onPoemClick: (String) -> Unit,
    onPoetClick: (String) -> Unit,
    onAddPoemClick: () -> Unit,
    onAdminDashboardClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val filteredPoems by viewModel.filteredPoems.collectAsState()
    val featuredPoems by viewModel.featuredPoems.collectAsState()
    val poets by viewModel.allPoets.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val context = LocalContext.current

    RtlLayout {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "پښتو شعرونه",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 22.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onProfileClick) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onAdminDashboardClick,
                            modifier = Modifier.testTag("admin_panel_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Panel",
                                tint = PashtoGold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddPoemClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_poem_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Poem")
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_poem_input"),
                        placeholder = { Text("په شعرونو، سرلیک یا شاعرانو کې لټون...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        },
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }

                // Category Filter Pills
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(PoemCategory.entries.toTypedArray()) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { viewModel.selectedCategory.value = category },
                                label = { Text(category.pashtoName, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                // ViewPager / Hero Carousel Banner
                if (searchQuery.isBlank()) {
                    item {
                        val pagerState = rememberPagerState(pageCount = { 4 })

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp)
                            ) { page ->
                                when (page) {
                                    0 -> {
                                        // Slide 0: Featured Poem
                                        val poem = featuredPoems.firstOrNull() ?: filteredPoems.firstOrNull()
                                        if (poem != null) {
                                            HeroSlideCard(
                                                tagText = "د ورځې غوره شعر",
                                                title = poem.title,
                                                subtitle = poem.poetName,
                                                excerpt = poem.content.lines().firstOrNull() ?: "",
                                                buttonText = "شعر لوستل",
                                                onClick = { onPoemClick(poem.id) }
                                            )
                                        }
                                    }

                                    1 -> {
                                        // Slide 1: Classical Pashto Wisdom / Proverbs
                                        HeroSlideCard(
                                            tagText = "د پښتو متلونه او ادبي حکمت",
                                            title = "کوهی مه کنه د بل په لار کې...",
                                            subtitle = "د رحمان بابا او سترو پښتنو حکیمانو نصیحتونه",
                                            excerpt = "په خپلو فکرونو او خبرو کې صداقت او عقلانیت وساتئ.",
                                            buttonText = "نور پندونه",
                                            onClick = { viewModel.selectedCategory.value = PoemCategory.ISLAMIC }
                                        )
                                    }

                                    2 -> {
                                        // Slide 2: Epic & Patriotic Poetry
                                        HeroSlideCard(
                                            tagText = "ملي ننګ او غیرت",
                                            title = "د خټکو د دستار سړی...",
                                            subtitle = "خوشحال خان خټک او حماسي اشعار",
                                            excerpt = "د افغاني هویت، وطنپالنې او تاریخ تر ټولو جېګې ترانې.",
                                            buttonText = "حماسي اشعار",
                                            onClick = { viewModel.selectedCategory.value = PoemCategory.PATRIOTIC }
                                        )
                                    }

                                    3 -> {
                                        // Slide 3: Add Poem Call-to-Action
                                        HeroSlideCard(
                                            tagText = "شاعران او مینوال",
                                            title = "خپل خوندور شعرونه له خلکو سره شریک کړئ!",
                                            subtitle = "د پښتو شعرونو خزانې ته خپله نوي کلامونه اضافه کړئ",
                                            excerpt = "تاسو کولی شئ خپل احساسات په لیکلې بڼه دلته خپاره کړئ.",
                                            buttonText = "شعر خپور کړه",
                                            onClick = onAddPoemClick
                                        )
                                    }
                                }
                            }

                            // ViewPager Dots Indicator
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(4) { index ->
                                    val isSelected = pagerState.currentPage == index
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .size(if (isSelected) 10.dp else 6.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) PashtoGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                    )
                                }
                            }
                        }
                    }
                }

                // Famous Poets Horizontal Section
                if (searchQuery.isBlank()) {
                    item {
                        Column {
                            Text(
                                text = "د پښتو ژبې ستر شاعران",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(poets) { poet ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .width(80.dp)
                                            .clickable { onPoetClick(poet.id) }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = poet.name.take(1),
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = poet.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section Title for Feed
                item {
                    Text(
                        text = if (searchQuery.isNotBlank()) "د لټون پایلې" else "نوې او مشهورې غزلې",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Poems List Feed
                if (filteredPoems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "هیڅ شعر ونه موندل شو.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(filteredPoems, key = { it.id }) { poem ->
                        PashtoPoemCard(
                            poem = poem,
                            onPoemClick = { onPoemClick(poem.id) },
                            onLikeClick = { viewModel.toggleLike(poem.id, poem.isLikedByMe) },
                            onFavoriteClick = { viewModel.toggleFavorite(poem.id, poem.isFavoriteByMe) },
                            onShareClick = {
                                val shareIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, "${poem.title}\n\n${poem.content}\n\n— ${poem.poetName}\nد پښتو شعرونو له اپلیکیشن څخه")
                                    type = "text/plain"
                                }
                                context.startActivity(android.content.Intent.createChooser(shareIntent, "شعر شریک کړئ"))
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}

@Composable
private fun HeroSlideCard(
    tagText: String,
    title: String,
    subtitle: String,
    excerpt: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_pashto_poetry_1786439522886),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.88f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = PashtoGold,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = tagText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(buttonText, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PashtoGold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (excerpt.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = excerpt,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
