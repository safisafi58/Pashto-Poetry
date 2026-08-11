package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.components.RtlLayout
import com.example.ui.theme.PashtoGold
import com.example.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    adminViewModel: AdminViewModel,
    onBackClick: () -> Unit
) {
    val pendingPoems by adminViewModel.pendingPoems.collectAsState()
    val allPoems by adminViewModel.allPoems.collectAsState()
    val allComments by adminViewModel.allComments.collectAsState()
    val adminUsers by adminViewModel.adminUsers.collectAsState()
    val allPoets by adminViewModel.allPoets.collectAsState()
    val adminStats by adminViewModel.adminStats.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Admins, 1: Quick Add, 2: Approvals, 3: All Poems, 4: Comments, 5: DB Config
    val context = LocalContext.current

    // Dialog control states
    var showAddAdminDialog by remember { mutableStateOf(false) }
    var showAddPoetDialog by remember { mutableStateOf(false) }
    var showAddPoemDialog by remember { mutableStateOf(false) }

    var urlInput by remember { mutableStateOf(adminViewModel.supabaseUrlState.value) }
    var keyInput by remember { mutableStateOf(adminViewModel.supabaseKeyState.value) }
    val isTesting by adminViewModel.isTestingConnection.collectAsState()
    val connectionResult by adminViewModel.connectionResult.collectAsState()

    RtlLayout {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = PashtoGold.copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = PashtoGold,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("د اډمین درانه مدیریت پینل", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Text("د پښتو شعر او ادبي خپرونو اډمین سیسټم", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Admin Overview Analytics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminStatCard("تایید شوي اډمینان", "${adminUsers.size}", Icons.Default.SupervisorAccount, Modifier.weight(1f))
                    AdminStatCard("ټول شعرونه", "${adminStats.totalPoems}", Icons.Default.MenuBook, Modifier.weight(1f))
                    AdminStatCard("د تایید انتظار", "${adminStats.pendingApprovalCount}", Icons.Default.HourglassTop, Modifier.weight(1f))
                    AdminStatCard("ټولې خوښونې", "${adminStats.totalLikes}", Icons.Default.ThumbUp, Modifier.weight(1f))
                }

                // Main Admin Navigation Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("د اډمینانو لیست (${adminUsers.size})")
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("نوي اضافه کول")
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.RecentActors, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("د شاعرانو مدیریت (${allPoets.size})")
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("د تایید انتظار (${pendingPoems.size})") }
                    )
                    Tab(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        text = { Text("ټول شعرونه (${allPoems.size})") }
                    )
                    Tab(
                        selected = selectedTab == 5,
                        onClick = { selectedTab = 5 },
                        text = { Text("نظرونه (${allComments.size})") }
                    )
                    Tab(
                        selected = selectedTab == 6,
                        onClick = { selectedTab = 6 },
                        text = { Text("سوبابېس Supabase") }
                    )
                    Tab(
                        selected = selectedTab == 7,
                        onClick = { selectedTab = 7 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تلګرام Telegram")
                            }
                        }
                    )
                }

                // Tab Content View
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (selectedTab) {
                        0 -> {
                            // Admins List & Management Section
                            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("د اډمینانو او مدیرانو لیست (Admins)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text("د اپلیکیشن راجستر شوي مسؤلین او کره کتونکي مدیران", style = MaterialTheme.typography.bodySmall)
                                        }

                                        Button(
                                            onClick = { showAddAdminDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = PashtoGold)
                                        ) {
                                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.Black)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("نوی اډمین", color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                if (adminUsers.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("تر اوسه بل اډمین ندی راجستر شوی.")
                                    }
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        items(adminUsers, key = { it.id }) { admin ->
                                            AdminUserItemCard(
                                                admin = admin,
                                                onDeleteClick = {
                                                    adminViewModel.deleteUser(admin.id)
                                                    Toast.makeText(context, "${admin.name} له اډمینانو لیرې شو", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // Quick Add Hub: Add Poem, Add Poet, Add Admin
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text("د کارونو او موادو چټک اضافه کول", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                                QuickAddActionCard(
                                    title = "شعر اضافه کول (Add Poem)",
                                    description = "مستقیم نوی شعر د هر شاعر په نوم خپور او شاعري خزانې ته اضافه کړئ.",
                                    icon = Icons.Default.PostAdd,
                                    buttonText = "نوی شعر اضافه کړه",
                                    onClick = { showAddPoemDialog = true }
                                )

                                QuickAddActionCard(
                                    title = "شاعر اضافه کول (Add Poet)",
                                    description = "د پښتو ژبې نوی تایید شوی شاعر له لسیزې او سوانحو سره راجستر کړئ.",
                                    icon = Icons.Default.PersonAddAlt1,
                                    buttonText = "نوی شاعر اضافه کړه",
                                    onClick = { showAddPoetDialog = true }
                                )

                                QuickAddActionCard(
                                    title = "اډمین جوړول (Add Admin)",
                                    description = "نوی مدیر راجستر کړئ ترڅو له تاسو سره د شعرونو په تایید او تنظیم کې مرسته وکړي.",
                                    icon = Icons.Default.AdminPanelSettings,
                                    buttonText = "نوی اډمین جوړ کړه",
                                    onClick = { showAddAdminDialog = true }
                                )
                            }
                        }

                        2 -> {
                            // Poets Management Section
                            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("د ثبت شویو شاعرانو لیست", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text("د پښتو ټول شامل شاعران او هغوی مربوطه ادبي دورې", style = MaterialTheme.typography.bodySmall)
                                        }

                                        Button(
                                            onClick = { showAddPoetDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Icon(Icons.Default.PersonAddAlt1, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("نوی شاعر", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                if (allPoets.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("تر اوسه کوم شاعر ندی ثبت شوی.")
                                    }
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        items(allPoets, key = { it.id }) { poet ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(14.dp),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = PashtoGold.copy(alpha = 0.25f),
                                                            modifier = Modifier.size(46.dp)
                                                        ) {
                                                            Box(contentAlignment = Alignment.Center) {
                                                                Text(
                                                                    text = poet.name.take(1),
                                                                    style = MaterialTheme.typography.titleMedium,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = PashtoGold
                                                                )
                                                            }
                                                        }

                                                        Spacer(modifier = Modifier.width(12.dp))

                                                        Column {
                                                            Text(poet.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                            Text("دوره: ${poet.era}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                                            if (poet.bio.isNotBlank()) {
                                                                Text(poet.bio, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                            }
                                                        }
                                                    }

                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = MaterialTheme.colorScheme.surfaceVariant
                                                    ) {
                                                        Text(
                                                            "${poet.poemCount} شعرونه",
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        3 -> {
                            // Pending Poems Approval List
                            if (pendingPoems.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("هیڅ نوی شعر د تایید په انتظار کې نشته.", style = MaterialTheme.typography.bodyLarge)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(pendingPoems, key = { it.id }) { poem ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text(poem.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                Text("شاعر: ${poem.poetName}", style = MaterialTheme.typography.labelMedium, color = PashtoGold)
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(poem.content, maxLines = 4, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
                                                Spacer(modifier = Modifier.height(12.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.End,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    OutlinedButton(
                                                        onClick = { adminViewModel.deletePoem(poem.id) },
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                                    ) {
                                                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("ردول / حذف")
                                                    }

                                                    Spacer(modifier = Modifier.width(10.dp))

                                                    Button(
                                                        onClick = {
                                                            adminViewModel.approvePoem(poem.id)
                                                            Toast.makeText(context, "شعر تایید او خپور شو!", Toast.LENGTH_SHORT).show()
                                                        }
                                                    ) {
                                                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("تایید او خپرول")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        4 -> {
                            // All Poems List with Delete & Feature Toggle
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(allPoems, key = { it.id }) { poem ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(poem.title, fontWeight = FontWeight.Bold)
                                                Text("${poem.poetName} • ${poem.category}", style = MaterialTheme.typography.bodySmall)
                                            }

                                            Row {
                                                IconButton(onClick = { adminViewModel.toggleFeaturePoem(poem.id) }) {
                                                    Icon(
                                                        imageVector = if (poem.isFeatured) Icons.Default.Star else Icons.Default.StarBorder,
                                                        contentDescription = "Feature",
                                                        tint = if (poem.isFeatured) PashtoGold else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                IconButton(onClick = { adminViewModel.deletePoem(poem.id) }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete",
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        5 -> {
                            // Comments Moderation
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(allComments, key = { it.id }) { comment ->
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(comment.userName, fontWeight = FontWeight.Bold)
                                                Text(comment.commentText, style = MaterialTheme.typography.bodyMedium)
                                            }

                                            IconButton(onClick = { adminViewModel.deleteComment(comment.id, comment.poemId) }) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        6 -> {
                            // Supabase Database Connection Panel
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Text("د سوبابېس Supabase ډېټابېس اړيکه", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                                        OutlinedTextField(
                                            value = urlInput,
                                            onValueChange = { urlInput = it },
                                            label = { Text("Supabase URL") },
                                            modifier = Modifier.fillMaxWidth().testTag("supabase_url_input"),
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = keyInput,
                                            onValueChange = { keyInput = it },
                                            label = { Text("Supabase Anon Key") },
                                            modifier = Modifier.fillMaxWidth().testTag("supabase_key_input"),
                                            singleLine = true
                                        )

                                        Button(
                                            onClick = { adminViewModel.testSupabaseConnection(urlInput, keyInput) },
                                            modifier = Modifier.fillMaxWidth().testTag("test_supabase_btn")
                                        ) {
                                            if (isTesting) {
                                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                            } else {
                                                Text("د اړيکې ازمویل او خوندي کول")
                                            }
                                        }

                                        connectionResult?.let { result ->
                                            val resultText = if (result) "له Supabase سره بريالۍ اړيکه ټينګه شوه!" else "له Supabase سره اړيکه ونه شوه. لطفاً URL او Key وڅېړئ."
                                            val resultColor = if (result) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                            Text(text = resultText, color = resultColor, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        7 -> {
                            // Telegram Integration Section
                            val botToken by adminViewModel.telegramBotTokenState.collectAsState()
                            val channelId by adminViewModel.telegramChannelIdState.collectAsState()
                            val posts by adminViewModel.telegramPosts.collectAsState()
                            val isFetching by adminViewModel.isFetchingTelegram.collectAsState()

                            var tokenInput by remember(botToken) { mutableStateOf(botToken) }
                            var channelInput by remember(channelId) { mutableStateOf(channelId) }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Send, contentDescription = null, tint = PashtoGold, modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("د تلګرام چینل تنظیم او بوټ نښلول", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        }

                                        Text(
                                            "دلته کولی شئ د خپل تلګرام Bot Token او د چینل نوم (@channel) داخل کړئ ترڅو د تلګرام چینل ټول شعرونه مستقیم په اپلیکیشن کې وښودل شي.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        OutlinedTextField(
                                            value = tokenInput,
                                            onValueChange = { tokenInput = it },
                                            label = { Text("Telegram Bot Token") },
                                            placeholder = { Text("123456789:ABCdef...") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = channelInput,
                                            onValueChange = { channelInput = it },
                                            label = { Text("Telegram Channel Username / ID") },
                                            placeholder = { Text("@pashto_poetry") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    adminViewModel.saveTelegramConfig(tokenInput, channelInput)
                                                    Toast.makeText(context, "د تلګرام تنظیمات خوندي شول!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("خوندي کول او نښلول")
                                            }

                                            OutlinedButton(
                                                onClick = { adminViewModel.fetchTelegramPosts(tokenInput, channelInput) }
                                            ) {
                                                if (isFetching) {
                                                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                                } else {
                                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("تازه کول")
                                                }
                                            }
                                        }
                                    }
                                }

                                Text("د تلګرام چینل فعالې خپرونې (${posts.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                                if (posts.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                        Text("هیڅ تلګرام پوسټ ونه موندل شو. د ترلاسه کولو بټن کېکاږئ.", style = MaterialTheme.typography.bodyMedium)
                                    }
                                } else {
                                    posts.forEach { post ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(post.channelName, fontWeight = FontWeight.Bold, color = PashtoGold)
                                                    Text(post.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(post.text, style = MaterialTheme.typography.bodyMedium)
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

        // --- DIALOGS FOR QUICK ADDING EVERYTHING ---

        // 1. Add Admin Dialog
        if (showAddAdminDialog) {
            AddAdminDialog(
                onDismiss = { showAddAdminDialog = false },
                onAddAdmin = { name, email, bio, botToken, channelId ->
                    adminViewModel.addAdminUser(name, email, bio, botToken, channelId)
                    Toast.makeText(context, "$name په بریالیتوب سره د اډمین په توګه راجستر شو", Toast.LENGTH_SHORT).show()
                    showAddAdminDialog = false
                }
            )
        }

        // 2. Add Poet Dialog
        if (showAddPoetDialog) {
            AddPoetDialog(
                onDismiss = { showAddPoetDialog = false },
                onAddPoet = { name, era, bio ->
                    adminViewModel.addPoet(name, era, bio)
                    Toast.makeText(context, "شاعر $name په بریالیتوب سره اضافه شو", Toast.LENGTH_SHORT).show()
                    showAddPoetDialog = false
                }
            )
        }

        // 3. Add Poem Dialog
        if (showAddPoemDialog) {
            AddPoemDialog(
                poetsList = allPoets.map { it.name },
                onDismiss = { showAddPoemDialog = false },
                onAddPoem = { title, poetName, category, content ->
                    val poetId = allPoets.find { it.name == poetName }?.id ?: "poet_rahman_baba"
                    adminViewModel.addPoemDirectly(title, poetId, poetName, category, content)
                    Toast.makeText(context, "شعر خپور او اضافه شو!", Toast.LENGTH_SHORT).show()
                    showAddPoemDialog = false
                }
            )
        }
    }
}

@Composable
private fun AdminUserItemCard(admin: UserProfile, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    shape = CircleShape,
                    color = PashtoGold.copy(alpha = 0.25f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = PashtoGold,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(admin.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                "ارشد اډمین",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(admin.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (admin.bio.isNotBlank()) {
                        Text(admin.bio, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (!admin.telegramChannelId.isNullOrBlank()) {
                        Text("تلګرام: ${admin.telegramChannelId}", style = MaterialTheme.typography.labelSmall, color = PashtoGold, fontWeight = FontWeight.Bold)
                    }
                }
            }

            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Remove Admin", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun QuickAddActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(18.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(2.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = onClick, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(buttonText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AdminStatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
private fun AddAdminDialog(
    onDismiss: () -> Unit,
    onAddAdmin: (name: String, email: String, bio: String, telegramBotToken: String?, telegramChannelId: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var botToken by remember { mutableStateOf("") }
    var channelId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("نوی اډمین او د تلګرام ټوکن راجستر کړه", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("د اډمین نوم") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("ایمیل (Email)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("دندې / د مسؤلیت برخه") },
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider()
                Text("د تلګرام نښلول (Telegram Integration)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = PashtoGold)
                OutlinedTextField(
                    value = botToken,
                    onValueChange = { botToken = it },
                    label = { Text("Telegram Bot Token") },
                    placeholder = { Text("123456789:ABCdef...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = channelId,
                    onValueChange = { channelId = it },
                    label = { Text("Telegram Channel Username / ID") },
                    placeholder = { Text("@pashto_poetry") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAddAdmin(
                            name,
                            email,
                            bio,
                            botToken.ifBlank { null },
                            channelId.ifBlank { null }
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("ثبت کړه")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("بیخي")
            }
        }
    )
}

@Composable
private fun AddPoetDialog(onDismiss: () -> Unit, onAddPoet: (name: String, era: String, bio: String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var era by remember { mutableStateOf("معاصره دوره") }
    var bio by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("نوی شاعر راجستر کړه", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("د شاعر نوم") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = era,
                    onValueChange = { era = it },
                    label = { Text("دوره / عصر") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("د شاعر ژوندلیک او پېژندنه") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onAddPoet(name, era, bio) },
                enabled = name.isNotBlank()
            ) {
                Text("شاعر اضافه کړه")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("بیخي")
            }
        }
    )
}

@Composable
private fun AddPoemDialog(
    poetsList: List<String>,
    onDismiss: () -> Unit,
    onAddPoem: (title: String, poetName: String, category: String, content: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var poetName by remember { mutableStateOf(poetsList.firstOrNull() ?: "عبدالرحمان بابا") }
    var category by remember { mutableStateOf("ghazal") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("مستقیم شعر اضافه کول", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("د شعر عنوان / نوم") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = poetName,
                    onValueChange = { poetName = it },
                    label = { Text("د شاعر نوم") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("د شعر متن او ابیات") },
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank() && content.isNotBlank()) onAddPoem(title, poetName, category, content) },
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Text("خپور کړه")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("بیخي")
            }
        }
    )
}
