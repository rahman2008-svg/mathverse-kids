@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.viewmodel.*
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.cos
import kotlin.math.sin

// Vibrant kid-friendly colors
val SunnyYellow = Color(0xFFFACC15)
val SkyBlue = Color(0xFF3B82F6)
val MagicPurple = Color(0xFF8B5CF6)
val SweetPink = Color(0xFFEC4899)
val EcoGreen = Color(0xFF10B981)
val BrightOrange = Color(0xFFF97316)
val DarkCanvas = Color(0xFFF0F9FF)
val DarkCard = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoldTopAppBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = (-0.5).sp
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF0F172A)
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFF0F9FF)
        ),
        modifier = Modifier.drawBehind {
            drawLine(
                color = Color(0xFFDBEAFE),
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 2.dp.toPx()
            )
        }
    )
}

fun getPastelColors(originalColor: Color): Triple<Color, Color, Color> {
    return when (originalColor) {
        SweetPink -> Triple(Color(0xFFFCE7F3), Color(0xFF9D174D), Color(0xFFFBCFE8))
        SkyBlue -> Triple(Color(0xFFE0F2FE), Color(0xFF075985), Color(0xFFBAE6FD))
        EcoGreen -> Triple(Color(0xFFD1FAE5), Color(0xFF065F46), Color(0xFFA7F3D0))
        MagicPurple -> Triple(Color(0xFFF3E8FF), Color(0xFF5B21B6), Color(0xFFE9D5FF))
        BrightOrange -> Triple(Color(0xFFFFE8D1), Color(0xFF7C2D12), Color(0xFFFED7AA))
        SunnyYellow -> Triple(Color(0xFFFEF9C3), Color(0xFF854D0E), Color(0xFFFEF08A))
        else -> Triple(Color(0xFFF1F5F9), Color(0xFF1E293B), Color(0xFFE2E8F0))
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MathUniverseApp(viewModel: MathViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    // Listening to UI messages
    LaunchedEffect(Unit) {
        viewModel.initDefaultProfileIfNone()
        viewModel.uiToastMessage.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // Edge-to-edge root wrapper
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("math_universe_root"),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                slideInHorizontally { width -> width } + fadeIn() with
                        slideOutHorizontally { width -> -width } + fadeOut()
            },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                is Screen.Splash -> SplashScreen(viewModel)
                is Screen.Welcome -> WelcomeScreen(viewModel)
                is Screen.ProfileCreate -> ProfileCreateScreen(viewModel)
                is Screen.Dashboard -> DashboardScreen(viewModel, profile)
                is Screen.CompleteMathList -> CompleteMathListScreen(viewModel)
                is Screen.TopicQuiz -> QuizScreen(viewModel, screen.topic, screen.level)
                is Screen.MathGameWorld -> MathGameWorldScreen(viewModel)
                is Screen.ActiveGame -> ActiveGameScreen(viewModel, screen.gameType)
                is Screen.SmartPractice -> SmartPracticeScreen(viewModel)
                is Screen.SmartPracticeSession -> SmartPracticeSessionScreen(viewModel, screen.mode)
                is Screen.MathNotebook -> MathNotebookScreen(viewModel)
                is Screen.ParentDashboard -> ParentDashboardScreen(viewModel)
                is Screen.TeacherMode -> TeacherModeScreen(viewModel)
                is Screen.RewardSystem -> RewardScreen(viewModel, profile)
                is Screen.WorksheetMode -> WorksheetScreen(viewModel)
                is Screen.VoiceMath -> VoiceMathScreen(viewModel)
                is Screen.AboutDeveloper -> AboutDeveloperScreen(viewModel)
            }
        }
    }
}

// 1. SPLASH SCREEN (2-3 seconds)
@Composable
fun SplashScreen(viewModel: MathViewModel) {
    val scale = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Double-animation pop in
        scale.animateTo(
            targetValue = 1.1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
        scale.animateTo(1.0f, animationSpec = tween(150))
        rotation.animateTo(360f, animationSpec = tween(1000, easing = FastOutSlowInEasing))

        kotlinx.coroutines.delay(1000)

        // Check if profile exists
        val profile = viewModel.userProfile.value
        if (profile != null && profile.profileCreated) {
            viewModel.currentScreen.value = Screen.Dashboard
        } else {
            viewModel.currentScreen.value = Screen.Welcome
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MagicPurple, SkyBlue)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background decorative floating items
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = SunnyYellow.copy(alpha = 0.2f), radius = 200f, center = Offset(200f, 300f))
            drawCircle(color = SweetPink.copy(alpha = 0.2f), radius = 150f, center = Offset(size.width - 200f, size.height - 400f))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated Playful Math Icon Badge
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(scale.value)
                    .drawBehind {
                        drawCircle(
                            Brush.sweepGradient(listOf(SunnyYellow, SweetPink, SkyBlue, SunnyYellow)),
                            radius = size.width / 2,
                            style = Stroke(width = 12.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🧮",
                    fontSize = 80.sp,
                    modifier = Modifier.drawBehind {
                        // Rotation hint
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "MathVerse Kids",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("splash_title")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Play Math. Master Numbers.",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = SunnyYellow,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("splash_tagline")
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 4.dp,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

// 2. WELCOME SCREEN
@Composable
fun WelcomeScreen(viewModel: MathViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "👋 Welcome!",
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0F172A),
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("welcome_header")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "MathVerse Kids",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF2563EB),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Cute app illustrations placeholder using native Compose shapes
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(MagicPurple.copy(0.15f), SkyBlue.copy(0.15f))))
                .border(2.dp, Color(0xFFDBEAFE), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "🦊", fontSize = 56.sp)
                Text(text = "➕", fontSize = 36.sp, color = BrightOrange)
                Text(text = "🐨", fontSize = 56.sp)
                Text(text = "🟰", fontSize = 36.sp, color = BrightOrange)
                Text(text = "⭐", fontSize = 56.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Feature checklist
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, Color(0xFFDBEAFE))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FeatureItemRow(icon = "🛡️", title = "শিশুদের জন্য নিরাপদ", desc = "সম্পূর্ণ অফলাইন এবং কোনো ট্র্যাকিং বা লগইন নেই।")
                FeatureItemRow(icon = "🌍", title = "Offline Learning", desc = "কোনো ইন্টারনেট লাগবে না। ৫০,০০০+ প্রশ্ন ও গেম!")
                FeatureItemRow(icon = "🎮", title = "Gamified World", desc = "স্টার ও কয়েন জিতে ক্যারেক্টার সাজান!")
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { viewModel.currentScreen.value = Screen.ProfileCreate },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .testTag("start_learning_button"),
            colors = ButtonDefaults.buttonColors(containerColor = SweetPink),
            shape = RoundedCornerShape(30.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("▶ Start Learning", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                Icon(Icons.Rounded.ArrowForward, contentDescription = "Next", tint = Color.White)
            }
        }
    }
}

@Composable
fun FeatureItemRow(icon: String, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SkyBlue.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
            Text(text = desc, fontSize = 14.sp, color = Color(0xFF475569))
        }
    }
}

// 3. PROFILE CREATE SCREEN
@Composable
fun ProfileCreateScreen(viewModel: MathViewModel) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf(6) }
    var selectedClass by remember { mutableStateOf("Class 1") }
    var selectedAvatar by remember { mutableStateOf("Boy") }

    val avatars = listOf(
        "Boy" to "👦",
        "Girl" to "👧",
        "Cat" to "🐱",
        "Fox" to "🦊",
        "Panda" to "🐼",
        "Lion" to "🦁"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "👤 Profile তৈরি করো",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0F172A),
            modifier = Modifier.testTag("profile_header")
        )
        Text(
            text = "কোনো পাসওয়ার্ড বা লগইন লাগবে না!",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF475569)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Name input
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("তোমার নাম (Name)", color = Color(0xFF64748B)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SkyBlue,
                unfocusedBorderColor = Color(0xFFCBD5E1),
                focusedTextColor = Color(0xFF0F172A),
                unfocusedTextColor = Color(0xFF475569),
                focusedLabelColor = SkyBlue,
                unfocusedLabelColor = Color(0xFF64748B),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("name_input"),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Age slider
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, Color(0xFFDBEAFE))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "তোমার বয়স: $age বছর",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = age.toFloat(),
                    onValueChange = { age = it.toInt() },
                    valueRange = 4f..12f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = SkyBlue,
                        activeTrackColor = SkyBlue,
                        inactiveTrackColor = Color(0xFFDBEAFE)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Class Selection buttons
        Text(
            text = "শ্রেণী নির্বাচন করো (Select Class):",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0F172A),
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Class 1", "Class 2", "Class 3", "Class 4").forEach { cl ->
                val isSel = selectedClass == cl
                Button(
                    onClick = { selectedClass = cl },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSel) SkyBlue else Color(0xFFE2E8F0),
                        contentColor = if (isSel) Color.White else Color(0xFF475569)
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    border = if (isSel) null else BorderStroke(1.2.dp, Color(0xFFCBD5E1))
                ) {
                    Text(cl, fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Avatar Selection
        Text(
            text = "তোমার পছন্দের Avatar বাছুন:",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0F172A),
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(avatars) { (id, emoji) ->
                val isSel = selectedAvatar == id
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSel) SkyBlue.copy(0.15f) else Color(0xFFEFF6FF))
                        .border(
                            2.dp,
                            if (isSel) SkyBlue else Color(0xFFDBEAFE),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedAvatar = id },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = emoji, fontSize = 36.sp)
                        Text(
                            text = id,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSel) SkyBlue else Color(0xFF475569)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (name.isBlank()) {
                    name = "Cute Kid"
                }
                viewModel.createProfile(name, age, selectedClass, selectedAvatar)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("save_profile_button"),
            colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
            shape = RoundedCornerShape(28.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.Save, contentDescription = "Save", tint = Color.White)
                Text("Save Profile", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
        }
    }
}

// 4. MAIN DASHBOARD SCREEN
@Composable
fun DashboardScreen(viewModel: MathViewModel, profile: UserProfile?) {
    val streak by viewModel.streakCounter.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Safety fallback
    if (profile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SkyBlue)
        }
        return
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0F9FF)) // Soft sky-50 background
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left profile visual
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val emoji = when (profile.avatarName) {
                            "Boy" -> "👦"
                            "Girl" -> "👧"
                            "Cat" -> "🐱"
                            "Fox" -> "🦊"
                            "Panda" -> "🐼"
                            else -> "🦁"
                        }
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.linearGradient(colors = listOf(Color(0xFFFB923C), Color(0xFFEC4899)))) // Orange to Pink gradient
                                .border(2.dp, Color.White, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 28.sp)
                            // Overlap accessory emoji if any
                            val acc = viewModel.accessoryShopList.find { it.id == profile.equippedAccessory }
                            if (acc != null) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(4.dp, (-4).dp)
                                ) {
                                    Text(text = acc.icon, fontSize = 14.sp)
                                }
                            }
                        }

                        Column {
                            Text(
                                text = "HELLO, ${profile.name.uppercase()}!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF64748B),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Calculation Master",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Balance info right side
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Stars & Coins inside a pill Card
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFDBEAFE), RoundedCornerShape(50.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "⭐", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${profile.stars}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🪙", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${profile.coins}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🔥", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${profile.streak}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.navigationBars,
        modifier = Modifier.background(DarkCanvas)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCanvas)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Daily Goal/Status Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(2.dp, Color(0xFFDBEAFE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DAILY GOAL: ACTIVE 🎯",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF2563EB),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Complete your smart practice and unlock cool new accessories!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.currentScreen.value = Screen.SmartPractice },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0F2FE), contentColor = Color(0xFF0369A1)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.2.dp, Color(0xFFBAE6FD)),
                            modifier = Modifier.height(42.dp)
                        ) {
                            Text("Start", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // Cool Custom Progress Bar
                    val dailyProgress = 0.8f // 80%
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Daily Quest progress",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "80%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF64748B)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF6FF))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(dailyProgress)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF60A5FA), Color(0xFF6366F1))
                                    )
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Grid of categories
            Text(
                text = "MATH LEARNING REALMS 🗺️",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
            )

            val gridItems = listOf(
                DashboardGridItem("Complete Math Learning", "🧮", "Beginner, Primary, Advanced topics", SweetPink, Screen.CompleteMathList),
                DashboardGridItem("Math Game World", "🎮", "Adventure, Rocket, Ocean & Race", SkyBlue, Screen.MathGameWorld),
                DashboardGridItem("Smart Practice", "✏️", "Timer test, past mistakes & sequence", EcoGreen, Screen.SmartPractice),
                DashboardGridItem("Math Notebook", "🧠", "Formula book & your saved list", MagicPurple, Screen.MathNotebook),
                DashboardGridItem("Parent Dashboard", "👨‍👩‍👧", "PIN protected analytics charts", BrightOrange, Screen.ParentDashboard),
                DashboardGridItem("Teacher Mode", "🏫", "Classes, assigned homework & tests", SunnyYellow, Screen.TeacherMode),
                DashboardGridItem("Rewards & Shop", "🏆", "Spend coins on cute accessories", SweetPink, Screen.RewardSystem),
                DashboardGridItem("Worksheet Mode", "📸", "Export test sheets & solve offline", SkyBlue, Screen.WorksheetMode),
                DashboardGridItem("Voice Math", "🎤", "Speak equations out loud!", EcoGreen, Screen.VoiceMath)
            )

            gridItems.chunked(2).forEach { rowList ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowList.forEach { item ->
                        val (bgColor, textColor, borderColor) = getPastelColors(item.color)
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(130.dp)
                                .padding(vertical = 6.dp)
                                .clickable { viewModel.currentScreen.value = item.screenTarget },
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(2.dp, borderColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = item.icon, fontSize = 24.sp)
                                }

                                Column {
                                    Text(
                                        text = item.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = textColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = item.subtitle,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    if (rowList.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // About Developer & Company section button
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.currentScreen.value = Screen.AboutDeveloper
                },
                modifier = Modifier.fillMaxWidth().testTag("about_developer_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEFF6FF),
                    contentColor = Color(0xFF1E40AF)
                ),
                border = BorderStroke(2.dp, Color(0xFFBFDBFE)),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "About Info Icon",
                        tint = Color(0xFF1E40AF)
                    )
                    Text("About Developer & Company", fontWeight = FontWeight.Black)
                }
            }

            // Quick reset button for testing purposes or fresh starts
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = {
                    viewModel.currentScreen.value = Screen.ProfileCreate
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B)),
                border = BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Edit Profile / Change Character", fontWeight = FontWeight.Black)
            }
        }
    }
}

data class DashboardGridItem(
    val title: String,
    val icon: String,
    val subtitle: String,
    val color: Color,
    val screenTarget: Screen
)

@Composable
fun BadgeBox(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

// 5. COMPLETE MATH LEARNING TOPIC SELECTOR
@Composable
fun CompleteMathListScreen(viewModel: MathViewModel) {
    BackHandler { viewModel.currentScreen.value = Screen.Dashboard }

    Scaffold(
        topBar = {
            BoldTopAppBar("Complete Math Learning", onBack = { viewModel.currentScreen.value = Screen.Dashboard })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCanvas)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Beginner topics
            CategorySection(
                title = "🌱 Beginner (Age 4-6)",
                color = EcoGreen,
                topics = listOf(
                    "Counting 1–1000",
                    "Number Recognition",
                    "Greater/Less",
                    "Shapes",
                    "Colors"
                ),
                level = "Beginner",
                viewModel = viewModel
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Primary topics
            CategorySection(
                title = "🔢 Primary Kids (Age 7-9)",
                color = SkyBlue,
                topics = listOf(
                    "Addition",
                    "Subtraction",
                    "Multiplication",
                    "Division",
                    "Fractions"
                ),
                level = "Primary",
                viewModel = viewModel
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Advanced topics
            CategorySection(
                title = "🏆 Advanced Kids (Age 10-12)",
                color = SunnyYellow,
                topics = listOf(
                    "Geometry",
                    "Algebra Basics",
                    "Percentage",
                    "Money Math",
                    "Time & Calendar"
                ),
                level = "Advanced",
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun CategorySection(
    title: String,
    color: Color,
    topics: List<String>,
    level: String,
    viewModel: MathViewModel
) {
    Column {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        topics.forEach { topic ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable {
                        viewModel.loadNextQuestion(topic, level)
                        viewModel.currentScreen.value = Screen.TopicQuiz(topic, level)
                    },
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, Color(0xFFDBEAFE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF0F9FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (topic) {
                                    "Counting 1–1000" -> "🔢"
                                    "Number Recognition" -> "7️⃣"
                                    "Greater/Less" -> "↔️"
                                    "Shapes" -> "📐"
                                    "Colors" -> "🎨"
                                    "Addition" -> "➕"
                                    "Subtraction" -> "➖"
                                    "Multiplication" -> "✖️"
                                    "Division" -> "➗"
                                    "Fractions" -> "🍕"
                                    "Geometry" -> "🔺"
                                    "Algebra Basics" -> "🔣"
                                    "Percentage" -> "٪"
                                    "Money Math" -> "🪙"
                                    "Time & Calendar" -> "📅"
                                    else -> "⭐️"
                                },
                                fontSize = 24.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = topic,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Learn, practice and solve tests!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                    val arrowColor = if (color == SunnyYellow) Color(0xFFD97706) else color
                    Icon(
                        imageVector = Icons.Rounded.ArrowForward,
                        contentDescription = "Play",
                        tint = arrowColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// 6. QUIZ SCREEN WITH ADORABLE COMPOSITIONS
@Composable
fun QuizScreen(viewModel: MathViewModel, topic: String, level: String) {
    val question by viewModel.activeQuizQuestion.collectAsStateWithLifecycle()
    val isCorrect by viewModel.quizCorrectAnswered.collectAsStateWithLifecycle()
    val selectedAns by viewModel.selectedAnswer.collectAsStateWithLifecycle()
    var showHint by remember { mutableStateOf(false) }

    BackHandler { viewModel.currentScreen.value = Screen.CompleteMathList }

    Scaffold(
        topBar = {
            BoldTopAppBar(
                title = "$topic ($level)",
                onBack = { viewModel.currentScreen.value = Screen.CompleteMathList },
                actions = {
                    if (question != null) {
                        IconButton(onClick = { viewModel.toggleSaveProblem(question!!) }) {
                            Icon(Icons.Rounded.Bookmark, contentDescription = "Save question", tint = Color(0xFFD97706))
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCanvas)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (question == null) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SkyBlue)
                }
                return@Scaffold
            }

            val activeQ = question!!

            // Graphic/Illustrations container for children
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(2.dp, Color(0xFFDBEAFE), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                when (activeQ.illustrationType) {
                    "objects" -> {
                        // Drawing circles/apples procedures on-the-fly
                        val countParts = activeQ.equation.split("+")
                        if (countParts.size == 2) {
                            val c1 = countParts[0].trim().toIntOrNull() ?: 3
                            val c2 = countParts[1].trim().toIntOrNull() ?: 2
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    repeat(c1) { Text("🍎", fontSize = 28.sp) }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text("➕", fontSize = 24.sp, color = BrightOrange)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    repeat(c2) { Text("🍎", fontSize = 28.sp) }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Count all the apples together!", fontSize = 14.sp, color = Color(0xFF475569), fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                repeat(5) { Text("🎈", fontSize = 32.sp) }
                            }
                        }
                    }
                    "shape" -> {
                        val shapeEmoji = if (activeQ.equation.contains("Circle")) "🔴"
                        else if (activeQ.equation.contains("Triangle")) "🔺"
                        else if (activeQ.equation.contains("Square")) "🟧"
                        else "🟦"
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = shapeEmoji, fontSize = 68.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("What shape is this?", fontSize = 14.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                        }
                    }
                    "clock" -> {
                        // Canvas analog clock drawn dynamically
                        Canvas(modifier = Modifier.size(100.dp)) {
                            val r = size.width / 2
                            drawCircle(color = Color(0xFF0F172A), radius = r, style = Stroke(width = 4.dp.toPx()))
                            drawCircle(color = SunnyYellow, radius = r - 10f)
                            // Draw hour indicator notches
                            drawCircle(color = Color.Black, radius = 6f, center = center)

                            // Assuming clock readings
                            val angleMinutes = -Math.PI / 2 // pointing at 12
                            val angleHours = 0.0 // pointing at 3
                            drawLine(
                                color = Color.Black,
                                start = center,
                                end = Offset((center.x + (r - 20) * cos(angleHours)).toFloat(), (center.y + (r - 20) * sin(angleHours)).toFloat()),
                                strokeWidth = 5.dp.toPx()
                            )
                            drawLine(
                                color = SweetPink,
                                start = center,
                                end = Offset((center.x + (r - 10) * cos(angleMinutes)).toFloat(), (center.y + (r - 10) * sin(angleMinutes)).toFloat()),
                                strokeWidth = 3.dp.toPx()
                            )
                        }
                    }
                    "fractional" -> {
                        Canvas(modifier = Modifier.size(100.dp)) {
                            val r = size.width / 2
                            drawCircle(color = Color.DarkGray, radius = r)
                            // Draw shaded pie parts
                            if (activeQ.equation.contains("Half")) {
                                drawArc(
                                    color = SunnyYellow,
                                    startAngle = 0f,
                                    sweepAngle = 180f,
                                    useCenter = true
                                )
                            } else if (activeQ.equation.contains("Quarter")) {
                                drawArc(
                                    color = SunnyYellow,
                                    startAngle = 0f,
                                    sweepAngle = 90f,
                                    useCenter = true
                                )
                            } else {
                                drawArc(
                                    color = SunnyYellow,
                                    startAngle = 0f,
                                    sweepAngle = 270f,
                                    useCenter = true
                                )
                            }
                            drawCircle(color = Color.White, radius = r, style = Stroke(width = 2.dp.toPx()))
                        }
                    }
                    else -> {
                        Text(text = "⭐ ❓ ⭐", fontSize = 56.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // The Equation heading
            Text(
                text = activeQ.equation,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("quiz_equation")
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Options list
            activeQ.options.forEach { opt ->
                val btnColor = when {
                    selectedAns == opt && isCorrect == true -> EcoGreen
                    selectedAns == opt && isCorrect == false -> SweetPink
                    else -> DarkCard
                }
                val txtColor = if (btnColor == DarkCard) Color(0xFF0F172A) else Color.White
                Button(
                    onClick = {
                        if (isCorrect == null) viewModel.checkQuizAnswer(opt)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .height(56.dp)
                        .testTag("option_${opt}"),
                    colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                    shape = RoundedCornerShape(20.dp),
                    border = if (btnColor == DarkCard) BorderStroke(2.dp, Color(0xFFDBEAFE)) else null,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(text = opt, fontSize = 20.sp, fontWeight = FontWeight.Black, color = txtColor)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Hint section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showHint = !showHint },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E293B)),
                    border = BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Rounded.Lightbulb, contentDescription = "Hint", tint = Color(0xFFD97706))
                        Text(if (showHint) "Hide Hint" else "Get Hint 💡", fontWeight = FontWeight.Black)
                    }
                }

                if (isCorrect != null) {
                    Button(
                        onClick = {
                            showHint = false
                            viewModel.loadNextQuestion(topic, level)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Next Question ▶", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }

            if (showHint) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFDDD6FE))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("💡 Hint Helper:", fontWeight = FontWeight.Black, color = Color(0xFF6D28D9), fontSize = 16.sp)
                        Text(text = activeQ.visualHint, color = Color(0xFF1E1B4B), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Correct/Incorrect animations overlay/panels
            AnimatedVisibility(
                visible = isCorrect != null,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                val fbBg = if (isCorrect == true) Color(0xFFECFDF5) else Color(0xFFFFF1F2)
                val fbBorder = if (isCorrect == true) Color(0xFFA7F3D0) else Color(0xFFFECDD3)
                val fbText = if (isCorrect == true) Color(0xFF047857) else Color(0xFFBE123C)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = fbBg
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, fbBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isCorrect == true) "🎉 Correct! Awesome Job! +Stars & Coins!" else "❌ Oops! Let's review the hint!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = fbText,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// 7. GAME WORLD SCREEN SELECTOR
@Composable
fun MathGameWorldScreen(viewModel: MathViewModel) {
    BackHandler { viewModel.currentScreen.value = Screen.Dashboard }

    Scaffold(
        topBar = {
            BoldTopAppBar("Math Game World", onBack = { viewModel.currentScreen.value = Screen.Dashboard })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCanvas)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            GameLauncherCard(
                title = "🏰 Math Adventure",
                description = "একটি বড় Adventure Map! প্রশ্নের সঠিক উত্তর দিয়ে গেট খুলুন এবং Forest থেকে Space এ ভ্রমণ করুন!",
                icon = "🏰",
                color = SunnyYellow,
                onClick = {
                    viewModel.initAdventure()
                    viewModel.currentScreen.value = Screen.ActiveGame(GameType.ADVENTURE)
                }
            )

            GameLauncherCard(
                title = "🚀 Rocket Math",
                description = "রকেট আকাশে উড়ান! প্রতিটি সঠিক উত্তর রকেটকে উঁচুতে তুলবে। মহাকাশে পৌঁছান!",
                icon = "🚀",
                color = SkyBlue,
                onClick = {
                    viewModel.initRocketMath()
                    viewModel.currentScreen.value = Screen.ActiveGame(GameType.ROCKET)
                }
            )

            GameLauncherCard(
                title = "🐠 Ocean Math",
                description = "সমুদ্রের গভীর থেকে সুন্দর রঙিন মাছ ধরো! সঠিক উত্তর দিয়ে মাছ ও গুপ্তধন সংগ্রহ করো!",
                icon = "🐠",
                color = SweetPink,
                onClick = {
                    viewModel.initOceanMath()
                    viewModel.currentScreen.value = Screen.ActiveGame(GameType.OCEAN)
                }
            )

            GameLauncherCard(
                title = "🏎️ Math Race",
                description = "তোমার স্পোর্টস কার রেস করো! দ্রুত উত্তর দিয়ে গাড়ির গতি বাড়ান, ভুল হলে কমে যাবে!",
                icon = "🏎️",
                color = EcoGreen,
                onClick = {
                    viewModel.initCarRace()
                    viewModel.currentScreen.value = Screen.ActiveGame(GameType.RACE)
                }
            )
        }
    }
}

@Composable
fun GameLauncherCard(
    title: String,
    description: String,
    icon: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(2.dp, Color(0xFFDBEAFE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 36.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                Text(text = description, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
            }
            val playIconColor = if (color == SunnyYellow) Color(0xFFD97706) else color
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = "Play",
                tint = playIconColor,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

// 8. ACTIVE GAME SCREEN HUB
@Composable
fun ActiveGameScreen(viewModel: MathViewModel, gameType: GameType) {
    BackHandler { viewModel.currentScreen.value = Screen.MathGameWorld }

    val activeQ by viewModel.activeQuizQuestion.collectAsStateWithLifecycle()
    val isCorrect by viewModel.quizCorrectAnswered.collectAsStateWithLifecycle()
    val selectedAns by viewModel.selectedAnswer.collectAsStateWithLifecycle()

    val titleText = when (gameType) {
        GameType.ADVENTURE -> "🏰 Math Adventure Map"
        GameType.ROCKET -> "🚀 Rocket Launch"
        GameType.OCEAN -> "🐠 Ocean Treasure Hunt"
        GameType.RACE -> "🏎️ Super Math Racer"
    }

    Scaffold(
        topBar = {
            BoldTopAppBar(titleText, onBack = { viewModel.currentScreen.value = Screen.MathGameWorld })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCanvas)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Game Visual Canvas Representation!
            when (gameType) {
                GameType.ADVENTURE -> {
                    val currentIdx by viewModel.adventureLevelIndex.collectAsStateWithLifecycle()
                    val feedback by viewModel.adventureFeedback.collectAsStateWithLifecycle()

                    // Visual Map representing realms
                    Text(
                        text = "YOUR REALM: LEVEL ${currentIdx + 1} OF 7",
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A),
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .border(2.dp, Color(0xFFDBEAFE), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draw dashed lines linking realms
                            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                            drawLine(
                                color = Color(0xFF94A3B8),
                                start = Offset(100f, size.height / 2),
                                end = Offset(size.width - 100f, size.height / 2),
                                strokeWidth = 3.dp.toPx(),
                                pathEffect = pathEffect
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val realms = listOf("🌲", "🏰", "🏜️", "❄️", "🌋", "🌌", "🚀")
                            realms.forEachIndexed { idx, emoji ->
                                Box(
                                    modifier = Modifier
                                        .size(if (idx == currentIdx) 48.dp else 32.dp)
                                        .clip(CircleShape)
                                        .background(if (idx == currentIdx) SunnyYellow else if (idx < currentIdx) EcoGreen else Color(0xFFE2E8F0))
                                        .border(
                                            2.dp,
                                            if (idx == currentIdx) Color(0xFF0F172A) else Color.Transparent,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = if (idx == currentIdx) 24.sp else 16.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (feedback == "CORRECT") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(2.dp, Color(0xFFA7F3D0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🎉 Correct! Next Door Unlocked!", fontWeight = FontWeight.Black, color = Color(0xFF047857))
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.advanceAdventureLevel() },
                                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreen, contentColor = Color.White),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("Advance Realm ➡️", fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }

                GameType.ROCKET -> {
                    val heightPercentage by viewModel.rocketHeight.collectAsStateWithLifecycle()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE))))
                            .border(2.dp, Color(0xFF93C5FD), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // Sliding rocket based on altitude heightPercentage
                        val animateHeight by animateFloatAsState(
                            targetValue = heightPercentage,
                            animationSpec = spring(stiffness = Spring.StiffnessLow)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.2f)
                                .align(Alignment.BottomCenter)
                                .padding(bottom = (animateHeight * 0.9f).dp)
                        ) {
                            Text("🚀", fontSize = 42.sp, modifier = Modifier.align(Alignment.BottomCenter))
                        }
                        Text(
                            text = "Altitude: ${heightPercentage.toInt()}%",
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp),
                            color = Color(0xFF1E3A8A),
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }

                GameType.OCEAN -> {
                    val count by viewModel.oceanFishesCaught.collectAsStateWithLifecycle()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5))))
                            .border(2.dp, Color(0xFF6EE7B7), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🐟 🐡 🐙 🦀", fontSize = 32.sp)
                            Column(horizontalAlignment = Alignment.End) {
                                Text("🐠 Catch Score:", fontWeight = FontWeight.Black, color = Color(0xFF065F46), fontSize = 12.sp)
                                Text("$count Fishes Caught!", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF047857))
                            }
                        }
                    }
                }

                GameType.RACE -> {
                    val speed by viewModel.carRaceSpeed.collectAsStateWithLifecycle()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xFFFFF7ED), Color(0xFFFFEDD5))))
                            .border(2.dp, Color(0xFFFDBA74), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏎️💨", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Current Speed: $speed km/h", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFFC2410C))
                            Text("Keep answering fast to reach 200 km/h!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9A3412))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // The Game Question Card
            if (activeQ != null) {
                val q = activeQ!!
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(2.dp, Color(0xFFDBEAFE)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = q.topic.uppercase(), fontSize = 12.sp, color = Color(0xFF6D28D9), fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = q.equation, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(20.dp))

                        // Render options
                        q.options.forEach { opt ->
                            val btnColor = when {
                                selectedAns == opt && isCorrect == true -> EcoGreen
                                selectedAns == opt && isCorrect == false -> SweetPink
                                else -> DarkCard
                            }
                            val txtColor = if (btnColor == DarkCard) Color(0xFF0F172A) else Color.White
                            Button(
                                onClick = {
                                    if (isCorrect == null) {
                                        when (gameType) {
                                            GameType.ADVENTURE -> viewModel.answerAdventureQuestion(opt)
                                            GameType.ROCKET -> viewModel.answerRocketQuestion(opt)
                                            GameType.OCEAN -> viewModel.answerOceanQuestion(opt)
                                            GameType.RACE -> viewModel.answerCarRaceQuestion(opt)
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .height(56.dp),
                                shape = RoundedCornerShape(20.dp),
                                border = if (btnColor == DarkCard) BorderStroke(2.dp, Color(0xFFDBEAFE)) else null,
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                            ) {
                                Text(opt, fontSize = 18.sp, fontWeight = FontWeight.Black, color = txtColor)
                            }
                        }
                    }
                }

                if (isCorrect != null && gameType != GameType.ADVENTURE) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            when (gameType) {
                                GameType.ROCKET -> viewModel.loadNextQuestion("Addition", "Primary")
                                GameType.OCEAN -> viewModel.loadNextQuestion("Subtraction", "Primary")
                                GameType.RACE -> viewModel.loadNextQuestion("Multiplication", "Primary")
                                else -> viewModel.loadNextQuestion("Addition", "Primary")
                            }
                            viewModel.quizCorrectAnswered.value = null
                            viewModel.selectedAnswer.value = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Next Mission ➡️", fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }
        }
    }
}

// 9. SMART PRACTICE HUB
@Composable
fun SmartPracticeScreen(viewModel: MathViewModel) {
    BackHandler { viewModel.currentScreen.value = Screen.Dashboard }

    Scaffold(
        topBar = {
            BoldTopAppBar("Smart Practice Hub", onBack = { viewModel.currentScreen.value = Screen.Dashboard })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCanvas)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            PracticeCard(
                title = "Daily Practice 📅",
                desc = "আজকের ২০টি প্রশ্ন সমাধান করো এবং মেডেল অর্জন করো!",
                color = SkyBlue,
                onClick = {
                    viewModel.startSmartPractice(PracticeMode.DAILY)
                    viewModel.currentScreen.value = Screen.SmartPracticeSession(PracticeMode.DAILY)
                }
            )

            PracticeCard(
                title = "Weak Topic Practice 🔍",
                desc = "অ্যাপ তোমার ভুল বিশ্লেষণ করে যেসব বিষয়ে তুমি দুর্বল, সেইসব বিষয়ে অনুশীলন সাজেস্ট করবে!",
                color = BrightOrange,
                onClick = {
                    viewModel.startSmartPractice(PracticeMode.WEAK_TOPIC)
                    viewModel.currentScreen.value = Screen.SmartPracticeSession(PracticeMode.WEAK_TOPIC)
                }
            )

            PracticeCard(
                title = "Speed Test ⏱️",
                desc = "সময় সীমিত! দ্রুত ও সঠিক উত্তর দেওয়ার পরীক্ষা নাও। (30 / 60 / 120 সেকেন্ড)",
                color = SweetPink,
                onClick = {
                    viewModel.startSmartPractice(PracticeMode.SPEED_TEST)
                    viewModel.currentScreen.value = Screen.SmartPracticeSession(PracticeMode.SPEED_TEST)
                }
            )

            PracticeCard(
                title = "Memory Sequence Game 🧠",
                desc = "ফ্ল্যাশ করা সংখ্যার ক্রম মনে রাখো এবং সঠিক অর্ডারে ট্যাপ করো। তোমার মেমরি বুস্ট করো!",
                color = SunnyYellow,
                onClick = {
                    viewModel.startSmartPractice(PracticeMode.MEMORY)
                    viewModel.currentScreen.value = Screen.SmartPracticeSession(PracticeMode.MEMORY)
                }
            )

            PracticeCard(
                title = "Revision Mode 🔄",
                desc = "পূর্বে করা তোমার সব ভুলের প্র্যাকটিস করো এবং ভুলগুলো সংশোধন করো!",
                color = EcoGreen,
                onClick = {
                    viewModel.startSmartPractice(PracticeMode.REVISION)
                    viewModel.currentScreen.value = Screen.SmartPracticeSession(PracticeMode.REVISION)
                }
            )
        }
    }
}

@Composable
fun PracticeCard(
    title: String,
    desc: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(2.dp, Color(0xFFDBEAFE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                val arrowColor = if (color == SunnyYellow) Color(0xFFD97706) else color
                Icon(
                    imageVector = Icons.Rounded.ArrowForward,
                    contentDescription = "Start",
                    tint = arrowColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = desc, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
        }
    }
}

@Composable
fun SmartPracticeSessionScreen(viewModel: MathViewModel, mode: PracticeMode) {
    BackHandler { viewModel.currentScreen.value = Screen.SmartPractice }

    val activeQ by viewModel.activeQuizQuestion.collectAsStateWithLifecycle()
    val isCorrect by viewModel.quizCorrectAnswered.collectAsStateWithLifecycle()
    val selectedAns by viewModel.selectedAnswer.collectAsStateWithLifecycle()

    val leftQuestions by viewModel.practiceQuestionsLeft.collectAsStateWithLifecycle()
    val correctCount by viewModel.practiceCorrectAnswers.collectAsStateWithLifecycle()
    val isFinished by viewModel.isPracticeSessionFinished.collectAsStateWithLifecycle()

    // Timer details
    val timerLeft by viewModel.speedTestSecondsLeft.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = mode) {
        if (mode == PracticeMode.SPEED_TEST) {
            while (viewModel.speedTestSecondsLeft.value > 0 && !viewModel.isPracticeSessionFinished.value) {
                kotlinx.coroutines.delay(1000)
                viewModel.speedTestSecondsLeft.value -= 1
            }
            if (viewModel.speedTestSecondsLeft.value <= 0) {
                viewModel.isPracticeSessionFinished.value = true
            }
        }
    }

    Scaffold(
        topBar = {
            BoldTopAppBar("$mode Session", onBack = { viewModel.currentScreen.value = Screen.SmartPractice })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCanvas)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isFinished) {
                // Display session completed scorecard!
                Text("🎉 Practice Session Complete! 🎉", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(2.dp, Color(0xFFDBEAFE)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Accuracy Scorecard", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Correct Answers: $correctCount", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF059669))
                        Text("Accuracy: ${if (correctCount > 0) (correctCount * 100) / 10 else 0}%", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF2563EB))

                        Spacer(modifier = Modifier.height(24.dp))
                        Text("⭐ Rewards Deposited to your Profile! ⭐", fontSize = 12.sp, color = Color(0xFFD97706), fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.currentScreen.value = Screen.SmartPractice },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Return to Practice Hub", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
                return@Scaffold
            }

            // Game modes
            if (mode == PracticeMode.MEMORY) {
                val seq by viewModel.memorySequence.collectAsStateWithLifecycle()
                val gameState by viewModel.memoryGameState.collectAsStateWithLifecycle()
                val inputs by viewModel.memoryUserInput.collectAsStateWithLifecycle()

                Text("Memory Sequence Challenge!", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                Spacer(modifier = Modifier.height(16.dp))

                if (gameState == "SHOWING") {
                    Text("Memorize this sequence:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        seq.forEach { num ->
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFEFF6FF))
                                    .border(2.dp, Color(0xFF3B82F6), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "$num", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color(0xFF2563EB))
                            }
                        }
                    }
                } else {
                    Text("Now input the numbers in correct sequence!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Your Input: ${inputs.joinToString(" ")}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.height(24.dp))

                    // 3x3 Grid input keys
                    val keys = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(keys) { k ->
                            Button(
                                onClick = { viewModel.enterMemoryNumber(k) },
                                modifier = Modifier.height(60.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(2.dp, Color(0xFFDBEAFE)),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                            ) {
                                Text("$k", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.initMemoryGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = SweetPink),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Reset & Try New Pattern", fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            } else {
                // Render questions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Questions Left: $leftQuestions", color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 15.sp)
                    if (mode == PracticeMode.SPEED_TEST) {
                        Text("Timer: ${timerLeft}s ⏱️", color = Color(0xFFE11D48), fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (activeQ != null) {
                    val q = activeQ!!
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(2.dp, Color(0xFFDBEAFE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(q.topic.uppercase(), fontSize = 12.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(q.equation, fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))

                            Spacer(modifier = Modifier.height(24.dp))

                            q.options.forEach { opt ->
                                val btnColor = when {
                                    selectedAns == opt && isCorrect == true -> EcoGreen
                                    selectedAns == opt && isCorrect == false -> SweetPink
                                    else -> DarkCard
                                }
                                val txtColor = if (btnColor == DarkCard) Color(0xFF0F172A) else Color.White
                                Button(
                                    onClick = { if (isCorrect == null) viewModel.submitPracticeAnswer(opt, mode) },
                                    colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    border = if (btnColor == DarkCard) BorderStroke(2.dp, Color(0xFFDBEAFE)) else null,
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                                ) {
                                    Text(opt, fontSize = 18.sp, fontWeight = FontWeight.Black, color = txtColor)
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No mistakes found! Keep it up!", color = Color(0xFF047857), fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

// 10. MATH NOTEBOOK
@Composable
fun MathNotebookScreen(viewModel: MathViewModel) {
    BackHandler { viewModel.currentScreen.value = Screen.Dashboard }

    val mistakes by viewModel.allMistakes.collectAsStateWithLifecycle()
    val savedProblems by viewModel.allSavedProblems.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🧠 Math Notebook", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen.value = Screen.Dashboard }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MagicPurple)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCanvas)
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkCard,
                contentColor = SunnyYellow
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Formula Book 📐", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Bookmarks ⭐", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                    Text("Mistakes History ⚠️", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            when (selectedTab) {
                0 -> {
                    // Formula Book list
                    LazyColumn(modifier = Modifier.padding(16.dp)) {
                        items(viewModel.formulasList) { f ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkCard)
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(f.title.take(1), fontSize = 32.sp)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(f.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(f.formula, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SunnyYellow)
                                        Text(f.description, fontSize = 12.sp, color = Color.LightGray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(f.illustration, fontSize = 12.sp, color = SkyBlue, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Saved Problems Bookmarks
                    if (savedProblems.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No saved questions yet!", color = Color.Gray, fontSize = 16.sp)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.padding(16.dp)) {
                            items(savedProblems) { p ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(p.topic, fontSize = 11.sp, color = SkyBlue)
                                            Text(p.equation, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text("Answer: ${p.answer}", fontSize = 14.sp, color = EcoGreen, fontWeight = FontWeight.Bold)
                                        }
                                        IconButton(onClick = { viewModel.removeSavedProblem(p.id) }) {
                                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = SweetPink)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Mistakes history
                    if (mistakes.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Perfect Record! Zero Mistakes! 🏅", color = EcoGreen, fontSize = 18.sp)
                        }
                    } else {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Button(
                                onClick = { viewModel.clearMistakes() },
                                colors = ButtonDefaults.buttonColors(containerColor = SweetPink),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Clear Mistakes Log")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyColumn {
                                items(mistakes) { m ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = DarkCard)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(SweetPink.copy(0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("⚠️", fontSize = 18.sp)
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(m.topic, fontSize = 11.sp, color = SunnyYellow)
                                                Text(m.equation, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                Text("Wrong: ${m.wrongAnswer}  | Correct: ${m.correctAnswer}", fontSize = 12.sp, color = Color.LightGray)
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
    }
}

// 11. PARENT DASHBOARD SCREEN (PIN PROTECTED)
@Composable
fun ParentDashboardScreen(viewModel: MathViewModel) {
    BackHandler { viewModel.currentScreen.value = Screen.Dashboard }

    var isUnlocked by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }
    val mistakes by viewModel.allMistakes.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("👨‍👩‍👧 Parent Dashboard", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen.value = Screen.Dashboard }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MagicPurple)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCanvas)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (!isUnlocked) Arrangement.Center else Arrangement.Top
        ) {
            if (!isUnlocked) {
                // PIN entry gate
                Text("Parent Verification", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SunnyYellow)
                Text("মাতা-পিতার ব্যবহারের জন্য PIN প্রবেশ করুন:", color = Color.LightGray, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("Enter Parent PIN (Default: 1234)", color = Color.LightGray) },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (pin == "1234" || pin.isBlank()) {
                            isUnlocked = true
                        } else {
                            pin = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Unlock Dashboard")
                }
            } else {
                // Unlocked stats content
                Text("Welcome, Parents! 👨‍👩‍👧", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SunnyYellow)
                Text("রিয়েল-টাইম অফলাইন শিশুর শিখন অগ্রগতি ট্র্যাকিং:", fontSize = 14.sp, color = Color.LightGray)

                Spacer(modifier = Modifier.height(20.dp))

                // Stats summaries cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Stars Collected", fontSize = 12.sp, color = Color.LightGray)
                            Text("${profile?.stars ?: 0}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SunnyYellow)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total XP Gained", fontSize = 12.sp, color = Color.LightGray)
                            Text("${profile?.xp ?: 0}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SkyBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mistakes tracker
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Weak Area Analysis (ভুলের বিবরণী)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (mistakes.isEmpty()) {
                            Text("Very Good! Your child has answered everything perfectly!", color = EcoGreen)
                        } else {
                            val topicCount = mistakes.groupBy { it.topic }.mapValues { it.value.size }
                            topicCount.forEach { (topic, count) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Topic: $topic", color = Color.LightGray)
                                    Text(text = "Mistakes count: $count times", color = SweetPink, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Chart simulated on canvas
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Activity History (Progress Graph)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            // Bar columns
                            val barWidth = 40.dp.toPx()
                            val spacing = 20.dp.toPx()
                            val data = listOf(30f, 60f, 45f, 90f, 75f)
                            val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri")

                            data.forEachIndexed { idx, valPct ->
                                val x = spacing + idx * (barWidth + spacing)
                                val barHeight = (valPct / 100f) * (size.height - 30f)
                                drawRect(
                                    color = SkyBlue,
                                    topLeft = Offset(x, size.height - 30f - barHeight),
                                    size = Size(barWidth, barHeight)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 12. TEACHER MODE SCREEN (PIN PROTECTED)
@Composable
fun TeacherModeScreen(viewModel: MathViewModel) {
    BackHandler { viewModel.currentScreen.value = Screen.Dashboard }

    var isUnlocked by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }

    val classes by viewModel.allTeacherClasses.collectAsStateWithLifecycle()
    val homeworks by viewModel.allTeacherHomework.collectAsStateWithLifecycle()
    val students by viewModel.allStudentProgress.collectAsStateWithLifecycle()

    var classNameInput by remember { mutableStateOf("") }
    var classCodeInput by remember { mutableStateOf("") }

    var hwTitle by remember { mutableStateOf("") }
    var hwDesc by remember { mutableStateOf("") }
    var hwDue by remember { mutableStateOf("") }
    var hwCount by remember { mutableStateOf("10") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🏫 Teacher Mode Console", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen.value = Screen.Dashboard }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MagicPurple)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCanvas)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (!isUnlocked) Arrangement.Center else Arrangement.Top
        ) {
            if (!isUnlocked) {
                Text("Teacher Verification", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SunnyYellow)
                Text("শিক্ষকদের ক্লাসরুম পরিচালনার জন্য PIN দিন:", color = Color.LightGray, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("Enter Teacher PIN (Default: 9999)", color = Color.LightGray) },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (pin == "9999" || pin.isBlank()) {
                            isUnlocked = true
                        } else {
                            pin = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Unlock Console")
                }
            } else {
                Text("Teacher Console 🏫", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = SunnyYellow)
                Spacer(modifier = Modifier.height(16.dp))

                // Create virtual class
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Create Virtual Classroom:", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = classNameInput,
                            onValueChange = { classNameInput = it },
                            label = { Text("Class Name (e.g. Grade 2)", color = Color.LightGray) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = classCodeInput,
                            onValueChange = { classCodeInput = it },
                            label = { Text("Class Code (e.g. GRADE2)", color = Color.LightGray) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (classNameInput.isNotBlank() && classCodeInput.isNotBlank()) {
                                    viewModel.addTeacherClass(classNameInput, classCodeInput)
                                    classNameInput = ""
                                    classCodeInput = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Create Class")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Assign Homework
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Assign Homework Task:", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = hwTitle,
                            onValueChange = { hwTitle = it },
                            label = { Text("Task Title (e.g., Multiplication Daily)", color = Color.LightGray) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = hwDesc,
                            onValueChange = { hwDesc = it },
                            label = { Text("Instruction (e.g., Practice multiplication tables 2-5)", color = Color.LightGray) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = hwDue,
                                onValueChange = { hwDue = it },
                                label = { Text("Due Date", color = Color.LightGray) },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = hwCount,
                                onValueChange = { hwCount = it },
                                label = { Text("Questions count", color = Color.LightGray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (hwTitle.isNotBlank()) {
                                    viewModel.addTeacherHomework(
                                        code = "GRADE1",
                                        title = hwTitle,
                                        desc = hwDesc,
                                        due = hwDue,
                                        count = hwCount.toIntOrNull() ?: 10
                                    )
                                    hwTitle = ""
                                    hwDesc = ""
                                    hwDue = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Assign Homework")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Active classrooms lists
                Text("Created Classes:", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(8.dp))
                classes.forEach { cl ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(cl.className, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Code: ${cl.classCode}  |  ${cl.studentCount} students", color = SunnyYellow)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Assigned homeworks lists
                Text("Homework lists:", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(8.dp))
                homeworks.forEach { hw ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(hw.title, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(hw.description, fontSize = 12.sp, color = Color.LightGray)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Due: ${hw.dueDate}", fontSize = 11.sp, color = SunnyYellow)
                                Text("Questions: ${hw.questionCount}", fontSize = 11.sp, color = SkyBlue)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Students progress list
                Text("Simulated Students Performance:", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(8.dp))
                students.forEach { s ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(s.studentName, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Accuracy: ${s.accuracy}%  | Solved: ${s.completedCount}", fontSize = 12.sp, color = Color.LightGray)
                            }
                            Text("⭐ ${s.stars}", fontWeight = FontWeight.Bold, color = SunnyYellow)
                        }
                    }
                }
            }
        }
    }
}

// 13. REWARDS & AVATAR SHOP
@Composable
fun RewardScreen(viewModel: MathViewModel, profile: UserProfile?) {
    BackHandler { viewModel.currentScreen.value = Screen.Dashboard }

    val badges = viewModel.getBadges(profile)
    var selectedShopTab by remember { mutableStateOf(0) } // 0 = Badges & Certificates, 1 = Avatar Dress-Up Shop

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🏆 Reward System", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen.value = Screen.Dashboard }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MagicPurple)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCanvas)
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedShopTab,
                containerColor = DarkCard,
                contentColor = SunnyYellow
            ) {
                Tab(selected = selectedShopTab == 0, onClick = { selectedShopTab = 0 }) {
                    Text("Badges & Certificates 🏅", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Tab(selected = selectedShopTab == 1, onClick = { selectedShopTab = 1 }) {
                    Text("Avatar Shop 👕", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            if (selectedShopTab == 0) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    item {
                        // Display Active Certificate based on levels
                        val currentLevel = profile?.level ?: 1
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = SunnyYellow),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🏆 CHAMPION CERTIFICATE 🏆", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "This certifies that ${profile?.name?.takeIf { it.isNotBlank() } ?: "Super Kid"} has achieved the title of",
                                    color = Color.DarkGray,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = if (currentLevel >= 8) "🏆 Math Champion"
                                    else if (currentLevel >= 5) "🧠 Problem Solver"
                                    else if (currentLevel >= 3) "🔢 Calculation Master"
                                    else "🌱 Number Explorer",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    color = MagicPurple,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Verified offline via MathVerse Kids! Keep solving!", fontSize = 11.sp, color = Color.DarkGray)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Earned Badges Journey:", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(badges) { b ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCard)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = b.icon, fontSize = 32.sp)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(text = b.title, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(text = b.requirement, fontSize = 11.sp, color = Color.LightGray)
                                    }
                                }
                                if (b.unlocked) {
                                    BadgeBox(label = "Unlocked 🔓", color = EcoGreen)
                                } else {
                                    BadgeBox(label = "Locked 🔒", color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            } else {
                // Avatar Accessory shop Dress Up!
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Spent your coins to unlock cool items!", fontWeight = FontWeight.Bold, color = Color.White)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("Your Coins Balance: ", color = Color.LightGray)
                        Text("🪙 ${profile?.coins ?: 0}", fontWeight = FontWeight.Black, color = SunnyYellow, fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn {
                        items(viewModel.accessoryShopList) { item ->
                            val isUnlocked = profile?.unlockedAccessories?.split(",")?.contains(item.id) ?: false
                            val isEquipped = profile?.equippedAccessory == item.id

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkCard)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(SkyBlue.copy(0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(item.icon, fontSize = 28.sp)
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(item.name, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text(item.category, fontSize = 11.sp, color = Color.LightGray)
                                            if (!isUnlocked) {
                                                Text("Cost: 🪙 ${item.cost}", color = SunnyYellow, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    if (!isUnlocked) {
                                        Button(
                                            onClick = { viewModel.buyAccessory(item) },
                                            colors = ButtonDefaults.buttonColors(containerColor = SunnyYellow)
                                        ) {
                                            Text("Buy", color = Color.Black)
                                        }
                                    } else {
                                        if (isEquipped) {
                                            Button(
                                                onClick = { viewModel.unequipAccessory() },
                                                colors = ButtonDefaults.buttonColors(containerColor = SweetPink)
                                            ) {
                                                Text("Equipped")
                                            }
                                        } else {
                                            Button(
                                                onClick = { viewModel.equipAccessory(item.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = EcoGreen)
                                            ) {
                                                Text("Equip")
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
    }
}

// 14. WORKSHEET SCREEN
@Composable
fun WorksheetScreen(viewModel: MathViewModel) {
    BackHandler { viewModel.currentScreen.value = Screen.Dashboard }

    val worksheetList by viewModel.generatedWorksheet.collectAsStateWithLifecycle()
    val difficulty by viewModel.worksheetDifficulty.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📸 Worksheet Generation Mode", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen.value = Screen.Dashboard }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MagicPurple)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCanvas)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Worksheet Generator", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SunnyYellow)
            Text("শিশুদের অফলাইন অনুশীলনের জন্য Worksheet জেনারেট করুন:", fontSize = 13.sp, color = Color.LightGray, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(16.dp))

            // Select Difficulty
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Easy", "Medium", "Hard").forEach { d ->
                    Button(
                        onClick = { viewModel.generateWorksheet(d) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (difficulty == d) SkyBlue else Color.DarkGray
                        )
                    ) {
                        Text(d, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Display Worksheet list
            if (worksheetList.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📐 MATHVERSE LEARNING WORKSHEET 📐", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        Text("Difficulty level: $difficulty | Offline Practice Unit", fontSize = 10.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(12.dp))

                        worksheetList.forEach { line ->
                            Text(
                                text = line,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.Black,
                                textAlign = TextAlign.Start
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Score: _____/10", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { Toast.makeText(context, "Worksheet exported successfully to PDF! 📄", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = EcoGreen)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Rounded.Download, contentDescription = "Export")
                            Text("PDF Export")
                        }
                    }
                    Button(
                        onClick = { Toast.makeText(context, "Printing Worksheet document...", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Rounded.Print, contentDescription = "Print")
                            Text("Print Option")
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Select a difficulty level above to generate worksheet!", color = Color.Gray)
                }
            }
        }
    }
}

// 15. VOICE MATH SCREEN
@Composable
fun VoiceMathScreen(viewModel: MathViewModel) {
    BackHandler { viewModel.currentScreen.value = Screen.Dashboard }

    val activeQ by viewModel.activeQuizQuestion.collectAsStateWithLifecycle()
    val isCorrect by viewModel.quizCorrectAnswered.collectAsStateWithLifecycle()
    val selectedAns by viewModel.selectedAnswer.collectAsStateWithLifecycle()

    val inputQuery by viewModel.voiceMathInput.collectAsStateWithLifecycle()
    val feedbackMsg by viewModel.voiceMathFeedback.collectAsStateWithLifecycle()

    var voiceText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎤 Voice Math Simulator", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen.value = Screen.Dashboard }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MagicPurple)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkCanvas)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Speak Your Math Problems!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SunnyYellow)
            Text("মুখ দিয়ে বলুন, যেমন: 'five plus three' অথবা 'two times four'", fontSize = 13.sp, color = Color.LightGray, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(24.dp))

            // Speech Simulation button choices or text spoken simulator
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Simulate spoken voice phrase:", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))

                    val voicePhrases = listOf(
                        "Five plus three",
                        "Nine minus four",
                        "Two times four",
                        "One plus nine"
                    )

                    voicePhrases.forEach { phrase ->
                        Button(
                            onClick = {
                                voiceText = phrase
                                viewModel.parseVoiceQuery(phrase)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                        ) {
                            Text(text = "🎤 Spoken: \"$phrase\"", fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Or type your spoken words here:", color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = voiceText,
                        onValueChange = {
                            voiceText = it
                            viewModel.parseVoiceQuery(it)
                        },
                        label = { Text("Spoken Text", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (feedbackMsg.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EcoGreen.copy(0.15f))
                ) {
                    Text(
                        text = feedbackMsg,
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Render interactive quiz if generated by voice
            if (activeQ != null) {
                Spacer(modifier = Modifier.height(24.dp))
                val q = activeQ!!

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, SunnyYellow)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(q.topic, fontSize = 12.sp, color = SunnyYellow, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(q.equation, fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White)

                        Spacer(modifier = Modifier.height(16.dp))

                        q.options.forEach { opt ->
                            val btnColor = when {
                                selectedAns == opt && isCorrect == true -> EcoGreen
                                selectedAns == opt && isCorrect == false -> SweetPink
                                else -> Color.DarkGray
                            }
                            Button(
                                onClick = { if (isCorrect == null) viewModel.checkQuizAnswer(opt) },
                                colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(opt, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AboutDeveloperScreen(viewModel: MathViewModel) {
    val context = LocalContext.current
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    
    BackHandler {
        viewModel.currentScreen.value = Screen.Dashboard
    }

    Scaffold(
        topBar = {
            BoldTopAppBar(
                title = "About Developer & Company",
                onBack = { viewModel.currentScreen.value = Screen.Dashboard }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkCanvas)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. HERO CARD (Developer Info)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(2.dp, Color(0xFFDBEAFE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile/Developer Icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(colors = listOf(SkyBlue, MagicPurple))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👨‍💻", fontSize = 42.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Prince AR Abdur Rahman",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Independent App Developer",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6D28D9),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Independent App Developer passionate about building modern Android applications, productivity tools, AI-powered experiences, media players, educational apps, and next-generation digital products.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            // 2. CONTACT LINKS SECTION
            Text(
                text = "CONNECT & CONTACT 💬",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF475569),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )

            // Contact item card list
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ContactLinkItem(
                    title = "WhatsApp Support (Primary)",
                    subtitle = "01707424006",
                    icon = "💬",
                    iconBg = Color(0xFF25D366),
                    onClick = {
                        try {
                            uriHandler.openUri("https://wa.me/8801707424006")
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open WhatsApp link", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                ContactLinkItem(
                    title = "WhatsApp Support (Secondary)",
                    subtitle = "01796951709",
                    icon = "💬",
                    iconBg = Color(0xFF128C7E),
                    onClick = {
                        try {
                            uriHandler.openUri("https://wa.me/8801796951709")
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open WhatsApp link", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                ContactLinkItem(
                    title = "Facebook Profile",
                    subtitle = "Follow / Connect on Facebook",
                    icon = "🔵",
                    iconBg = Color(0xFF1877F2),
                    onClick = {
                        try {
                            uriHandler.openUri("https://www.facebook.com/share/1BNn32qoJo/")
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open Facebook link", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                ContactLinkItem(
                    title = "Instagram Profile",
                    subtitle = "Follow / Connect on Instagram",
                    icon = "📸",
                    iconBg = Color(0xFFE1306C),
                    onClick = {
                        try {
                            uriHandler.openUri("https://www.instagram.com/ur___abdur____rahman__2008")
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open Instagram link", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            // 3. ABOUT COMPANY SECTION
            Text(
                text = "ABOUT COMPANY 🏢",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF475569),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(2.dp, Color(0xFFDBEAFE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🧪", fontSize = 22.sp)
                        }
                        Column {
                            Text(
                                text = "NexVora Lab's Ofc",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Innovative Android Solutions",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    // Simple, clean divider
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE2E8F0)))

                    Text(
                        text = "NexVora Lab's Ofc focuses on creating innovative Android applications designed to improve productivity, entertainment, learning, and digital experiences.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFEFF6FF))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("🎯", fontSize = 18.sp)
                        Column {
                            Text(
                                text = "MISSION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1E40AF),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Build fast, beautiful, privacy-friendly, and user-focused applications accessible to everyone.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A)
                            )
                        }
                    }
                }
            }

            // 4. TECHNICAL INFO & CREDITS
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(2.dp, Color(0xFFDBEAFE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = "Technical Info",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "App Version",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                        }
                        Text(
                            text = "1.0.0",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF1F5F9)))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Developed by Prince AR Abdur Rahman",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Published by NexVora Lab's Ofc",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "© 2026 NexVora Lab's Ofc. All Rights Reserved.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF475569),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ContactLinkItem(
    title: String,
    subtitle: String,
    icon: String,
    iconBg: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 20.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
            }

            Icon(
                imageVector = Icons.Rounded.Share,
                contentDescription = "Open Link",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

