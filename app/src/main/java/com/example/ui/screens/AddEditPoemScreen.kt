package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.PoemCategory
import com.example.ui.components.RtlLayout
import com.example.ui.viewmodel.PoetryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPoemScreen(
    viewModel: PoetryViewModel,
    authorName: String,
    isAdmin: Boolean = false,
    onBackClick: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var poetName by remember { mutableStateOf(authorName) }
    var selectedCategory by remember { mutableStateOf(PoemCategory.GHAZAL) }
    var poemContent by remember { mutableStateOf("") }

    val context = LocalContext.current

    RtlLayout {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("نوی شعر اضافه کړئ", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("د شعر سرلیک (موضوع)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_poem_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Poet Name
                OutlinedTextField(
                    value = poetName,
                    onValueChange = { poetName = it },
                    label = { Text("د شاعر نوم") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_poem_poet_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Category Chips
                Column {
                    Text(
                        text = "د شعر صنف / برخه غوره کړئ:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(PoemCategory.entries.filter { it != PoemCategory.ALL }) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(category.pashtoName) },
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }
                }

                // Content
                OutlinedTextField(
                    value = poemContent,
                    onValueChange = { poemContent = it },
                    label = { Text("د شعر بشپړ متن (بیتونه په نویو لښتو کې ولیکئ)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .testTag("add_poem_content_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (title.isBlank() || poemContent.isBlank()) {
                            Toast.makeText(context, "لطفاً سرلیک او متن دواړه ولیکئ", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addNewPoem(
                                title = title,
                                content = poemContent,
                                category = selectedCategory.id,
                                poetName = poetName,
                                authorName = authorName,
                                isAdmin = isAdmin
                            )
                            val msg = if (isAdmin) "شعر خپور شو!" else "شعر واستول شو او له تایید وروسته به خپور شي."
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            onBackClick()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_poem_btn"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("شعر شريک او خپور کړئ", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
