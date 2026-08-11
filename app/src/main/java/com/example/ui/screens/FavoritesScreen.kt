package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.PashtoPoemCard
import com.example.ui.components.RtlLayout
import com.example.ui.viewmodel.PoetryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: PoetryViewModel,
    onPoemClick: (String) -> Unit
) {
    val favoritePoems by viewModel.favoritePoems.collectAsState()
    val context = LocalContext.current

    RtlLayout {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("خوښ شوي شعرونه (ساتل شوي)", fontWeight = FontWeight.Bold) }
                )
            }
        ) { paddingValues ->
            if (favoritePoems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "تر اوسه مو هیڅ شعر ندی خوښ کړی.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(favoritePoems, key = { it.id }) { poem ->
                        PashtoPoemCard(
                            poem = poem,
                            onPoemClick = { onPoemClick(poem.id) },
                            onLikeClick = { viewModel.toggleLike(poem.id, poem.isLikedByMe) },
                            onFavoriteClick = { viewModel.toggleFavorite(poem.id, poem.isFavoriteByMe) },
                            onShareClick = {
                                val shareIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, "${poem.title}\n\n${poem.content}\n\n— ${poem.poetName}")
                                    type = "text/plain"
                                }
                                context.startActivity(android.content.Intent.createChooser(shareIntent, "شعر شریک کړئ"))
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            }
        }
    }
}
