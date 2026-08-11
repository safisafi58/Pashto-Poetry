package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Comment
import com.example.data.model.Poem
import com.example.ui.components.RtlLayout
import com.example.ui.theme.PashtoCrimson
import com.example.ui.theme.PashtoGold
import com.example.ui.viewmodel.PoetryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoemDetailScreen(
    poemId: String,
    viewModel: PoetryViewModel,
    userName: String,
    onBackClick: () -> Unit
) {
    val poems by viewModel.allApprovedPoems.collectAsState()
    val poem = poems.find { it.id == poemId }

    val commentsFlow = remember(poemId) { viewModel.getCommentsForPoem(poemId) }
    val comments by commentsFlow.collectAsState(initial = emptyList())

    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val playingPoemId by viewModel.currentPlayingPoemId.collectAsState()

    var newCommentText by remember { mutableStateOf("") }
    val context = LocalContext.current

    if (poem == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val heartColor by animateColorAsState(
        targetValue = if (poem.isFavoriteByMe) PashtoCrimson else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "heartColor"
    )

    val likeColor by animateColorAsState(
        targetValue = if (poem.isLikedByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "likeColor"
    )

    RtlLayout {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(poem.title, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Recite TTS audio button
                        IconButton(
                            onClick = { viewModel.speakPoem(poem.id, poem.content) },
                            modifier = Modifier.testTag("audio_recite_btn")
                        ) {
                            Icon(
                                imageVector = if (isSpeaking && playingPoemId == poem.id) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                                contentDescription = "Recite Poem",
                                tint = if (isSpeaking && playingPoemId == poem.id) PashtoGold else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Copy poem button
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Poem", "${poem.title}\n\n${poem.content}\n\n— ${poem.poetName}")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "شعر کاپي شو", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy")
                        }

                        // Share poem button
                        IconButton(onClick = {
                            val shareIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, "${poem.title}\n\n${poem.content}\n\n— ${poem.poetName}\nد پښتو شعرونو له اپلیکیشن څخه")
                                type = "text/plain"
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "شعر شریک کړئ"))
                        }) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Poet Badge
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Text(
                                text = "شاعر: ${poem.poetName}",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Full Poem Content Card (Parchment Calligraphy Style)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = poem.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "⚜ ⚜ ⚜",
                                style = MaterialTheme.typography.labelMedium,
                                color = PashtoGold
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Verse lines
                            poem.content.lines().forEach { line ->
                                if (line.isNotBlank()) {
                                    Text(
                                        text = line.trim(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontSize = 18.sp,
                                        lineHeight = 32.sp,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            Spacer(modifier = Modifier.height(12.dp))

                            // Toolbar: Likes & Favorites
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { viewModel.toggleLike(poem.id, poem.isLikedByMe) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (poem.isLikedByMe) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                        contentDescription = "Like",
                                        tint = likeColor
                                    )
                                    Text(text = "خوښول (${poem.likesCount})", color = likeColor, fontWeight = FontWeight.Bold)
                                }

                                Row(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { viewModel.toggleFavorite(poem.id, poem.isFavoriteByMe) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (poem.isFavoriteByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = heartColor
                                    )
                                    Text(text = "ساتل (${poem.favoritesCount})", color = heartColor, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Comments Section Header
                item {
                    Text(
                        text = "نظرونه او تبصرې (${comments.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Add Comment Box
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = newCommentText,
                                onValueChange = { newCommentText = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("comment_input"),
                                placeholder = { Text("خپل نظر یا ليدلوری دلته ولیکئ...") },
                                maxLines = 3,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    if (newCommentText.isNotBlank()) {
                                        viewModel.addComment(poem.id, newCommentText, userName)
                                        newCommentText = ""
                                        Toast.makeText(context, "نظر اضافه شو", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .testTag("submit_comment_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("نظر واستوئ")
                            }
                        }
                    }
                }

                // Comment List
                items(comments, key = { it.id }) { comment ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = comment.userName.take(1),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column {
                                    Text(
                                        text = comment.userName,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = comment.commentText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            if (comment.userId == viewModel.currentUserId.value) {
                                IconButton(
                                    onClick = { viewModel.deleteComment(comment.id, poem.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete comment",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
