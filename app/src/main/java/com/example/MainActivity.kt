package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.data.*


enum class Screen {
    HOME,
    LEARNING_TREE,
    AI_TUTOR,
    LIBRARY,
    LEARNING_PATH_GENERATOR,
    QUIZ,
    PROFILE,
    LESSON
}

enum class TransitionType {
    NONE,
    PUSH,
    PUSH_BACK,
    SLIDE_UP
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer()
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainAppContainer() {
    val viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val previousScreen by viewModel.previousScreen.collectAsState()

    fun navigateTo(screen: Screen) {
        viewModel.navigateTo(screen)
    }

    val transitionSpec: AnimatedContentTransitionScope<Screen>.() -> ContentTransform = {
        val from = initialState
        val to = targetState
        val type = getTransitionType(from, to)
        when (type) {
            TransitionType.NONE -> {
                ContentTransform(
                    targetContentEnter = fadeIn(animationSpec = snap()),
                    initialContentExit = fadeOut(animationSpec = snap())
                )
            }
            TransitionType.PUSH -> {
                ContentTransform(
                    targetContentEnter = slideInHorizontally(animationSpec = tween(300)) { it } + fadeIn(),
                    initialContentExit = slideOutHorizontally(animationSpec = tween(300)) { -it / 3 } + fadeOut()
                )
            }
            TransitionType.PUSH_BACK -> {
                ContentTransform(
                    targetContentEnter = slideInHorizontally(animationSpec = tween(300)) { -it } + fadeIn(),
                    initialContentExit = slideOutHorizontally(animationSpec = tween(300)) { it } + fadeOut()
                )
            }
            TransitionType.SLIDE_UP -> {
                ContentTransform(
                    targetContentEnter = slideInVertically(animationSpec = tween(350, easing = EaseOutBack)) { it } + fadeIn(),
                    initialContentExit = fadeOut(animationSpec = tween(250))
                )
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = transitionSpec,
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    Screen.HOME -> HomeScreen(viewModel = viewModel, onNavigate = ::navigateTo)
                    Screen.LEARNING_TREE -> LearningTreeScreen(viewModel = viewModel, onNavigate = ::navigateTo)
                    Screen.AI_TUTOR -> AITutorScreen(viewModel = viewModel, onNavigate = ::navigateTo)
                    Screen.LIBRARY -> LibraryScreen(viewModel = viewModel, onNavigate = ::navigateTo)
                    Screen.LEARNING_PATH_GENERATOR -> LearningPathGeneratorScreen(viewModel = viewModel, onNavigate = ::navigateTo)
                    Screen.QUIZ -> QuizScreen(viewModel = viewModel, onNavigate = ::navigateTo)
                    Screen.PROFILE -> ProfileScreen(viewModel = viewModel, onNavigate = ::navigateTo)
                    Screen.LESSON -> LessonScreen(viewModel = viewModel, onNavigate = ::navigateTo)
                }
            }
        }
    }
}


fun getTransitionType(from: Screen, to: Screen): TransitionType {
    return when {
        // Home transitions
        from == Screen.HOME && to == Screen.LEARNING_TREE -> TransitionType.NONE
        from == Screen.HOME && to == Screen.AI_TUTOR -> TransitionType.NONE
        from == Screen.HOME && to == Screen.LIBRARY -> TransitionType.NONE
        from == Screen.HOME && to == Screen.PROFILE -> TransitionType.NONE
        from == Screen.HOME && to == Screen.LEARNING_PATH_GENERATOR -> TransitionType.PUSH
        from == Screen.HOME && to == Screen.LESSON -> TransitionType.PUSH

        // Library transitions
        from == Screen.LIBRARY && to == Screen.HOME -> TransitionType.NONE
        from == Screen.LIBRARY && to == Screen.LEARNING_TREE -> TransitionType.NONE
        from == Screen.LIBRARY && to == Screen.AI_TUTOR -> TransitionType.NONE
        from == Screen.LIBRARY && to == Screen.PROFILE -> TransitionType.NONE

        // Learning Tree transitions
        from == Screen.LEARNING_TREE && to == Screen.HOME -> TransitionType.NONE
        from == Screen.LEARNING_TREE && to == Screen.AI_TUTOR -> TransitionType.NONE
        from == Screen.LEARNING_TREE && to == Screen.LIBRARY -> TransitionType.NONE
        from == Screen.LEARNING_TREE && to == Screen.PROFILE -> TransitionType.NONE
        from == Screen.LEARNING_TREE && to == Screen.LESSON -> TransitionType.PUSH

        // Profile transitions
        from == Screen.PROFILE && to == Screen.HOME -> TransitionType.NONE
        from == Screen.PROFILE && to == Screen.LEARNING_TREE -> TransitionType.NONE
        from == Screen.PROFILE && to == Screen.AI_TUTOR -> TransitionType.NONE
        from == Screen.PROFILE && to == Screen.LIBRARY -> TransitionType.NONE

        // AI Tutor transitions
        from == Screen.AI_TUTOR && to == Screen.HOME -> TransitionType.NONE
        from == Screen.AI_TUTOR && to == Screen.LEARNING_TREE -> TransitionType.NONE
        from == Screen.AI_TUTOR && to == Screen.LIBRARY -> TransitionType.NONE
        from == Screen.AI_TUTOR && to == Screen.PROFILE -> TransitionType.NONE
        from == Screen.AI_TUTOR && to == Screen.QUIZ -> TransitionType.SLIDE_UP

        // Quiz transitions
        from == Screen.QUIZ && to == Screen.LESSON -> TransitionType.PUSH_BACK
        from == Screen.QUIZ && to == Screen.HOME -> TransitionType.PUSH

        // Lesson transitions
        from == Screen.LESSON && to == Screen.HOME -> TransitionType.PUSH_BACK
        from == Screen.LESSON && to == Screen.QUIZ -> TransitionType.PUSH

        // Learning Path Generator transitions
        from == Screen.LEARNING_PATH_GENERATOR && to == Screen.HOME -> TransitionType.NONE
        from == Screen.LEARNING_PATH_GENERATOR && to == Screen.LEARNING_TREE -> TransitionType.NONE
        from == Screen.LEARNING_PATH_GENERATOR && to == Screen.AI_TUTOR -> TransitionType.NONE
        from == Screen.LEARNING_PATH_GENERATOR && to == Screen.LIBRARY -> TransitionType.NONE
        from == Screen.LEARNING_PATH_GENERATOR && to == Screen.PROFILE -> TransitionType.NONE
        from == Screen.LEARNING_PATH_GENERATOR && to == Screen.LESSON -> TransitionType.PUSH

        else -> TransitionType.NONE
    }
}

// ---------------------- SHARED COMPONENTS ----------------------

@Composable
fun TopAppBar(
    title: String = "LearnTree AI",
    showAvatar: Boolean = true,
    viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val streak by viewModel.streakCount.collectAsState()
    val xp by viewModel.xpAmount.collectAsState()

    Row(

        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundColor)
            .border(width = 0.dp, color = Color.Transparent)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (showAvatar) {
                // Playful learning avatar background
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen)
                        .border(2.dp, OutlineGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User Avatar",
                        tint = OnPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Text(
                text = title,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = PrimaryGreenDark,
                letterSpacing = (-0.5).sp
            )
        }

        // Streak & XP Chips
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(9999.dp))
                .background(SlateGray)
                .border(2.dp, SlateGrayHigh, RoundedCornerShape(9999.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = "Streak Icon",
                tint = TertiaryGoldContainer,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = streak.toString(),
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = OnBackgroundColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$xp XP",
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = NeutralGrayDark
            )
        }
    }
}

@Composable
fun BottomNavBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    val items = listOf(
        NavigationItem("Home", Icons.Filled.Home, Screen.HOME),
        NavigationItem("Tree", Icons.Filled.AccountTree, Screen.LEARNING_TREE),
        NavigationItem("Tutor", Icons.Filled.SmartToy, Screen.AI_TUTOR),
        NavigationItem("Library", Icons.Filled.LocalLibrary, Screen.LIBRARY),
        NavigationItem("Profile", Icons.Filled.Person, Screen.PROFILE)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PureWhite)
            .border(
                width = 2.dp,
                color = NeutralGray,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Bottom
    ) {
        items.forEach { item ->
            val isActive = currentScreen == item.screen
            val scaleFactor by animateFloatAsState(targetValue = if (isActive) 1.05f else 1.0f)

            Button(
                onClick = { onNavigate(item.screen) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) PrimaryGreen else Color.Transparent,
                    contentColor = if (isActive) OnPrimary else OnBackgroundColor
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = null,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier
                    .testTag("nav_btn_${item.label}")
                    .weight(1f)
                    .semantics { contentDescription = item.label }
                    .padding(horizontal = 2.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isActive) OnPrimary else NeutralGrayDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = item.label,
                        fontFamily = BeVietnamPro,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp,
                        color = if (isActive) OnPrimary else NeutralGrayDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

data class NavigationItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen
)

// ---------------------- SCREENS ----------------------

// 1. HOME SCREEN
@Composable
fun HomeScreen(
    viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigate: (Screen) -> Unit
) {
    val searchTopicQuery by viewModel.searchTopicQuery.collectAsState()
    val isGeneratingPath by viewModel.isGeneratingPath.collectAsState()
    val myTrees by viewModel.myTrees.collectAsState()
    val streakCount by viewModel.streakCount.collectAsState()

    val activePath = myTrees.firstOrNull()
    val firstLesson = activePath?.modules?.firstOrNull()?.lessons?.firstOrNull()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(viewModel = viewModel)
        
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Greeting Header
            Column {
                Text(
                    text = "Hi there! 👋",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    color = OnBackgroundColor
                )
                Text(
                    text = "What do you want to learn today?",
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    color = NeutralGrayDark
                )
            }

            // Generate Path Search / Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, NeutralGray, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Search bar textfield
                    OutlinedTextField(
                        value = searchTopicQuery,
                        onValueChange = { viewModel.updateSearchTopic(it) },
                        placeholder = {
                            Text(
                                text = "e.g., Learn Python, Learn Guitar...",
                                fontFamily = BeVietnamPro,
                                fontSize = 16.sp,
                                color = NeutralGrayDark
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search icon",
                                tint = NeutralGrayDark
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SlateGray,
                            unfocusedContainerColor = SlateGray,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            focusedTextColor = OnBackgroundColor,
                            unfocusedTextColor = OnBackgroundColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    )

                    // Chips row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Python", "Guitar", "World History").forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SlateGray)
                                    .clickable {
                                        viewModel.updateSearchTopic(tag)
                                        viewModel.generateLearningPath(tag)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontFamily = BeVietnamPro,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = NeutralGrayDark
                                )
                            }
                        }
                    }

                    // Generate Learning Path Button
                    Button(
                        onClick = { viewModel.generateLearningPath(searchTopicQuery) },
                        enabled = !isGeneratingPath && searchTopicQuery.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            disabledContainerColor = PrimaryGreen.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("generate_path_button"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        if (isGeneratingPath) {
                            CircularProgressIndicator(
                                color = OnPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Sparkles"
                                )
                                Text(
                                    text = "GENERATE LEARNING PATH",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = OnPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Bento Grid Dashboard - Continue Learning Card (Push to Lesson)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, NeutralGray, RoundedCornerShape(16.dp))
                    .clickable {
                        if (firstLesson != null) {
                            viewModel.selectLesson(firstLesson)
                        } else {
                            onNavigate(Screen.LESSON)
                        }
                    },
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SecondaryContainerBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (activePath != null) Icons.Default.AutoAwesome else Icons.Default.Terminal,
                            contentDescription = "Subject Icon",
                            tint = SecondaryBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "CONTINUE LEARNING",
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NeutralGrayDark,
                            letterSpacing = 1.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activePath?.topic ?: "Python Basics",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = OnBackgroundColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (activePath != null) "0%" else "60%",
                                fontFamily = BeVietnamPro,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PrimaryGreen
                            )
                        }
                        // Chunky progress bar
                        LinearProgressIndicator(
                            progress = if (activePath != null) 0.0f else 0.60f,
                            color = PrimaryGreen,
                            trackColor = SlateGray,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(9999.dp))
                        )
                    }
                }
            }

            // Bento Grid: Row of Streak & Goal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Streak Box
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(2.dp, NeutralGray, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = TertiaryGoldContainer,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = if (streakCount == 1) "1 Day" else "$streakCount Days",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = OnBackgroundColor
                        )
                        Text(
                            text = "Streak",
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NeutralGrayDark
                        )
                    }
                }

                // Today's Goal Box
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(2.dp, NeutralGray, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Today's Goal",
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NeutralGrayDark
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "15",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = OnBackgroundColor
                            )
                            Text(
                                text = "/ 20 min",
                                fontFamily = BeVietnamPro,
                                fontSize = 16.sp,
                                color = NeutralGrayDark
                            )
                        }
                        // Progress bar sky blue
                        LinearProgressIndicator(
                            progress = 0.75f,
                            color = SecondaryBlueLight,
                            trackColor = SlateGray,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(9999.dp))
                        )
                    }
                }
            }

            // Weekly Progress (Faux Bar Chart)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, NeutralGray, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Weekly Progress",
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = OnBackgroundColor
                        )
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Trending Up icon",
                            tint = NeutralGrayDark
                        )
                    }

                    // Bar Chart Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(128.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val heights = listOf(0.40f, 0.60f, 0.80f, 0.30f, 0.50f, 0.20f, 0.10f)
                        val labels = listOf("M", "T", "W", "T", "F", "S", "S")

                        heights.forEachIndexed { i, h ->
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight(h)
                                        .width(16.dp)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(if (i == 2) PrimaryGreen else SlateGray)
                                )
                                Text(
                                    text = labels[i],
                                    fontFamily = BeVietnamPro,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeutralGrayDark
                                )
                            }
                        }
                    }
                }
            }

            // Recommended Topics Section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Recommended for you",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = OnBackgroundColor
                )

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val topics = listOf(
                        RecommendedTopic("Calculus 101", "Refresh your math skills", Icons.Default.Calculate, TertiaryGoldContainer, OnTertiaryContainer),
                        RecommendedTopic("Digital Art", "Procreate basics", Icons.Default.Brush, ErrorContainer, OnErrorContainer),
                        RecommendedTopic("Chemistry", "Periodic table review", Icons.Default.Science, SecondaryContainerBlue, SecondaryBlue)
                    )

                    topics.forEach { topic ->
                        Card(
                            modifier = Modifier
                                .width(200.dp)
                                .border(2.dp, NeutralGray, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(topic.bgColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = topic.icon,
                                        contentDescription = topic.title,
                                        tint = topic.iconColor
                                    )
                                }
                                Text(
                                    text = topic.title,
                                    fontFamily = BeVietnamPro,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = OnBackgroundColor
                                )
                                Text(
                                    text = topic.subtitle,
                                    fontFamily = BeVietnamPro,
                                    fontSize = 14.sp,
                                    color = NeutralGrayDark
                                )
                            }
                        }
                    }
                }
            }
        }

        BottomNavBar(currentScreen = Screen.HOME, onNavigate = onNavigate)
    }
}

data class RecommendedTopic(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val bgColor: Color,
    val iconColor: Color
)

// 2. LEARNING TREE SCREEN
@Composable
fun LearningTreeScreen(
    viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigate: (Screen) -> Unit
) {
    val myTrees by viewModel.myTrees.collectAsState()
    val activePath = myTrees.firstOrNull()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(viewModel = viewModel)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(SlateGrayLow)
        ) {
            // Background dotted grid representation
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridSpacing = 32.dp.toPx()
                for (x in 0..size.width.toInt() step gridSpacing.toInt()) {
                    for (y in 0..size.height.toInt() step gridSpacing.toInt()) {
                        drawCircle(
                            color = NeutralGray,
                            radius = 2f,
                            center = androidx.compose.ui.geometry.Offset(x.toFloat(), y.toFloat())
                        )
                    }
                }
                
                // Draw connection lines representing learning tree pathways
                if (activePath == null) {
                    val startBasics = androidx.compose.ui.geometry.Offset(size.width / 2, 100.dp.toPx())
                    val endSyntax = androidx.compose.ui.geometry.Offset(size.width / 4, 250.dp.toPx())
                    val endVariables = androidx.compose.ui.geometry.Offset(3 * size.width / 4, 250.dp.toPx())
                    val endFunctions = androidx.compose.ui.geometry.Offset(size.width / 2, 420.dp.toPx())
                    val endClasses = androidx.compose.ui.geometry.Offset(size.width / 2, 580.dp.toPx())

                    drawLine(color = PrimaryGreen, start = startBasics, end = endSyntax, strokeWidth = 6f)
                    drawLine(color = PrimaryGreen, start = startBasics, end = endVariables, strokeWidth = 6f)
                    drawLine(color = PrimaryGreen, start = endSyntax, end = endFunctions, strokeWidth = 6f)
                    drawLine(color = OutlineGray, start = endVariables, end = endFunctions, strokeWidth = 6f)
                    drawLine(color = OutlineGray, start = endFunctions, end = endClasses, strokeWidth = 6f)
                } else {
                    // Vertical linear connection line for dynamic path nodes
                    val startNode = androidx.compose.ui.geometry.Offset(size.width / 2, 100.dp.toPx())
                    val midNode = androidx.compose.ui.geometry.Offset(size.width / 2, 280.dp.toPx())
                    val endNode = androidx.compose.ui.geometry.Offset(size.width / 2, 460.dp.toPx())
                    drawLine(color = PrimaryGreen, start = startNode, end = midNode, strokeWidth = 6f)
                    drawLine(color = OutlineGray, start = midNode, end = endNode, strokeWidth = 6f)
                }
            }

            // Scrollable nodes
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(64.dp)
            ) {
                if (activePath == null) {
                    // Intro Header inside canvas
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(PureWhite)
                            .border(2.dp, NeutralGray, RoundedCornerShape(9999.dp))
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "INTRODUCTION",
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NeutralGrayDark,
                            letterSpacing = 1.sp
                        )
                    }

                    // Node 1: Basics (Mastered)
                    TreeNode(
                        title = "Basics",
                        percentage = "100%",
                        time = "5m",
                        icon = Icons.Default.Star,
                        color = TertiaryGoldContainer,
                        shadowColor = OnTertiaryContainer,
                        onNodeClick = { }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Node 2: Syntax (Completed)
                        TreeNode(
                            title = "Syntax",
                            percentage = "100%",
                            time = "12m",
                            icon = Icons.Default.CheckCircle,
                            color = PrimaryGreen,
                            shadowColor = PrimaryGreenDark,
                            onNodeClick = { }
                        )

                        // Node 3: Variables (Pulsing / Current -> clicks to Lesson)
                        Box(
                            modifier = Modifier
                                .testTag("node_Variables")
                                .semantics { contentDescription = "Variables Node" }
                        ) {
                            TreeNode(
                                title = "Variables",
                                percentage = "50%",
                                time = "15m",
                                icon = Icons.Default.PlayArrow,
                                color = PureWhite,
                                borderColor = PrimaryGreen,
                                shadowColor = PrimaryGreenDark,
                                onNodeClick = { onNavigate(Screen.LESSON) }
                            )
                        }
                    }

                    // Node 4: Functions (Unlocked)
                    TreeNode(
                        title = "Functions",
                        percentage = "0%",
                        time = "20m",
                        icon = Icons.Default.LockOpen,
                        color = PureWhite,
                        borderColor = OutlineGray,
                        shadowColor = OutlineGray,
                        onNodeClick = { }
                    )

                    // Advanced Topics plaque
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(PureWhite)
                            .border(2.dp, NeutralGray, RoundedCornerShape(9999.dp))
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "ADVANCED TOPICS",
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NeutralGrayDark,
                            letterSpacing = 1.sp
                        )
                    }

                    // Node 5: Classes (Locked)
                    TreeNode(
                        title = "Classes",
                        percentage = "Locked",
                        time = "",
                        icon = Icons.Default.Lock,
                        color = SlateGray,
                        borderColor = SlateGrayHigh,
                        shadowColor = SlateGrayDim,
                        onNodeClick = { },
                        opacity = 0.6f
                    )
                } else {
                    // Header for active topic
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(PureWhite)
                            .border(2.dp, NeutralGray, RoundedCornerShape(9999.dp))
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = activePath.topic.uppercase(),
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PrimaryGreenDark,
                            letterSpacing = 1.sp
                        )
                    }

                    // Map flatmapped lessons to dynamic TreeNodes
                    val lessons = activePath.modules.flatMap { it.lessons }
                    lessons.forEachIndexed { index, lesson ->
                        val isFirst = index == 0
                        val nodeTitle = lesson.lessonTitle
                        val nodePercentage = if (isFirst) "0%" else "Locked"
                        val nodeIcon = if (isFirst) Icons.Default.PlayArrow else Icons.Default.Lock
                        val nodeColor = if (isFirst) PureWhite else SlateGray
                        val nodeBorder = if (isFirst) PrimaryGreen else OutlineGray
                        val nodeShadow = if (isFirst) PrimaryGreenDark else SlateGrayDim
                        val nodeOpacity = if (isFirst) 1.0f else 0.6f

                        Box(
                            modifier = Modifier
                                .testTag("node_${nodeTitle.replace(" ", "_")}")
                        ) {
                            TreeNode(
                                title = nodeTitle,
                                percentage = nodePercentage,
                                time = lesson.duration,
                                icon = nodeIcon,
                                color = nodeColor,
                                borderColor = nodeBorder,
                                shadowColor = nodeShadow,
                                opacity = nodeOpacity,
                                onNodeClick = {
                                    if (isFirst) {
                                        viewModel.selectLesson(lesson)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        BottomNavBar(currentScreen = Screen.LEARNING_TREE, onNavigate = onNavigate)
    }
}


@Composable
fun TreeNode(
    title: String,
    percentage: String,
    time: String,
    icon: ImageVector,
    color: Color,
    borderColor: Color = Color.Transparent,
    shadowColor: Color,
    onNodeClick: () -> Unit,
    opacity: Float = 1.0f
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clickable { onNodeClick() }
            .semantics { contentDescription = "$title Node" }
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                    width = if (borderColor != Color.Transparent) 4.dp else 0.dp,
                    color = borderColor,
                    shape = CircleShape
                )
                .padding(bottom = 8.dp), // offset for tactile shadow simulation
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (color == PureWhite) PrimaryGreen else shadowColor,
                modifier = Modifier.size(40.dp)
            )
        }

        // Tag plaque under node
        Card(
            modifier = Modifier
                .width(128.dp)
                .border(2.dp, NeutralGray, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = OnBackgroundColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (percentage.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = percentage,
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = PrimaryGreen
                        )
                        if (time.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Time icon",
                                    tint = NeutralGrayDark,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = time,
                                    fontFamily = BeVietnamPro,
                                    fontSize = 10.sp,
                                    color = NeutralGrayDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 3. AI TUTOR SCREEN
@Composable
fun MessageBubbleContent(text: String) {
    if (text.contains("```")) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val parts = text.split("```")
            parts.forEachIndexed { index, part ->
                if (index % 2 == 1) {
                    // Code block
                    val lines = part.trim().lines()
                    val language = if (lines.isNotEmpty() && !lines.first().contains(" ")) lines.first() else ""
                    val code = if (language.isNotEmpty()) lines.drop(1).joinToString("\n") else part.trim()
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, OutlineGray, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = "Code icon",
                                    tint = SecondaryBlue
                                )
                                Text(
                                    text = if (language.isNotEmpty()) "Code ($language)" else "Code Snippet",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = SecondaryBlue
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DeepBlack)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = code,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 14.sp,
                                    color = PrimaryGreen
                                )
                            }
                        }
                    }
                } else {
                    // Standard text
                    if (part.isNotBlank()) {
                        Text(
                            text = part.trim(),
                            fontFamily = BeVietnamPro,
                            fontSize = 16.sp,
                            color = OnBackgroundColor
                        )
                    }
                }
            }
        }
    } else {
        Text(
            text = text,
            fontFamily = BeVietnamPro,
            fontSize = 16.sp,
            color = OnBackgroundColor
        )
    }
}

@Composable
fun AITutorScreen(
    viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigate: (Screen) -> Unit
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isSendingMessage by viewModel.isSendingMessage.collectAsState()
    var messageInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // Scroll to bottom when message list grows
    LaunchedEffect(messages.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(viewModel = viewModel)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Chat messages list
            messages.forEach { msg ->
                val isAssistant = msg.role == "assistant"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    if (isAssistant) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SecondaryContainerBlue)
                                .border(2.dp, OutlineGray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "AI Robot Icon",
                                tint = SecondaryBlue
                            )
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .border(2.dp, OutlineGray, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp)),
                            colors = CardDefaults.cardColors(containerColor = SlateGrayHigh),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp)
                        ) {
                            Box(modifier = Modifier.padding(16.dp)) {
                                MessageBubbleContent(text = msg.content)
                            }
                        }
                        Spacer(modifier = Modifier.width(36.dp))
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .border(2.dp, PrimaryGreenDark, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp)),
                            colors = CardDefaults.cardColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp)
                        ) {
                            Text(
                                text = msg.content,
                                fontFamily = BeVietnamPro,
                                fontSize = 16.sp,
                                color = OnPrimary,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }

            // Thinking state
            if (isSendingMessage) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SecondaryContainerBlue)
                            .border(2.dp, OutlineGray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "AI Robot Icon",
                            tint = SecondaryBlue
                        )
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(2.dp, OutlineGray, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp)),
                        colors = CardDefaults.cardColors(containerColor = SlateGrayHigh),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = SecondaryBlue,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Tutor is thinking...",
                                fontFamily = BeVietnamPro,
                                fontSize = 14.sp,
                                color = NeutralGrayDark
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(36.dp))
                }
            }

            // Action Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    AITutorChip("Explain differently", Icons.Default.Autorenew),
                    AITutorChip("Give another example", Icons.Default.Lightbulb),
                    AITutorChip("Quiz me", Icons.Default.Quiz),
                    AITutorChip("ELI5", Icons.Default.ChildCare)
                ).forEach { chip ->
                    val isQuizMe = chip.label == "Quiz me"
                    Button(
                        onClick = {
                            if (isQuizMe) {
                                onNavigate(Screen.QUIZ)
                            } else {
                                viewModel.triggerQuickAction(chip.label)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite,
                            contentColor = PrimaryGreenDark
                        ),
                        shape = RoundedCornerShape(9999.dp),
                        border = BorderStroke(2.dp, OutlineGray),
                        modifier = Modifier
                            .testTag(if (isQuizMe) "quiz_me_button" else "chip_${chip.label}"),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = chip.icon,
                                contentDescription = chip.label,
                                tint = PrimaryGreenDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = chip.label,
                                fontFamily = BeVietnamPro,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Real Input field bar representation
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, OutlineGray, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        placeholder = {
                            Text(
                                text = "Ask me anything...",
                                fontFamily = BeVietnamPro,
                                fontSize = 16.sp,
                                color = NeutralGrayDark
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            focusedTextColor = OnBackgroundColor,
                            unfocusedTextColor = OnBackgroundColor
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = {
                            if (messageInput.isNotBlank()) {
                                viewModel.sendChatMessage(messageInput)
                                messageInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(40.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send message",
                            tint = OnPrimary
                        )
                    }
                }
            }
        }

        BottomNavBar(currentScreen = Screen.AI_TUTOR, onNavigate = onNavigate)
    }
}

data class AITutorChip(val label: String, val icon: ImageVector)


// 4. LIBRARY SCREEN
@Composable
fun LibraryScreen(
    viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigate: (Screen) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(viewModel = viewModel)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PureWhite)
                    .border(2.dp, OutlineGray, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon",
                    tint = NeutralGrayDark
                )
                Text(
                    text = "Search your library...",
                    fontFamily = BeVietnamPro,
                    fontSize = 18.sp,
                    color = OutlineGray
                )
            }

            // Collections Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Collections",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = OnBackgroundColor
                )

                val collections = listOf(
                    LibraryCollection("Saved Courses", "14 Active Enrollments", Icons.Default.Bookmark, PrimaryGreen, PrimaryGreenDark),
                    LibraryCollection("Completed Topics", "42 Mastered", Icons.Default.TaskAlt, SecondaryBlueLight, SecondaryBlue),
                    LibraryCollection("Flashcards", "8 Decks to review", Icons.Default.Style, TertiaryGold, OnTertiaryContainer),
                    LibraryCollection("Practice Sessions", "3 Recommended", Icons.Default.FitnessCenter, ErrorContainer, OnErrorContainer)
                )

                collections.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        row.forEach { col ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(2.dp, OutlineGray, RoundedCornerShape(24.dp)),
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(col.bgColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = col.icon,
                                            contentDescription = col.title,
                                            tint = col.iconColor
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = col.title,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = OnBackgroundColor
                                        )
                                        Text(
                                            text = col.subtitle,
                                            fontFamily = BeVietnamPro,
                                            fontSize = 14.sp,
                                            color = NeutralGrayDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recent Activity Section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Recent Activity",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = OnBackgroundColor
                )

                val activities = listOf(
                    LibraryActivity("Introduction to Cellular Biology", "Viewed 2 hours ago", Icons.Default.Book, Icons.Default.ArrowForward),
                    LibraryActivity("Calculus Midterm Review Prep", "Completed yesterday • 85% Score", Icons.Default.Quiz, Icons.Default.ArrowForward),
                    LibraryActivity("World History: The Renaissance", "Paused at 14:20", Icons.Default.Videocam, Icons.Default.PlayArrow)
                )

                activities.forEach { act ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, OutlineGray, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SlateGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = act.icon,
                                        contentDescription = act.title,
                                        tint = NeutralGrayDark
                                    )
                                }
                                Column {
                                    Text(
                                        text = act.title,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = OnBackgroundColor
                                    )
                                    Text(
                                        text = act.subtitle,
                                        fontFamily = BeVietnamPro,
                                        fontSize = 14.sp,
                                        color = NeutralGrayDark
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PureWhite)
                                    .border(2.dp, OutlineGray, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = act.actionIcon,
                                    contentDescription = "Action button",
                                    tint = PrimaryGreen
                                )
                            }
                        }
                    }
                }
            }
        }

        BottomNavBar(currentScreen = Screen.LIBRARY, onNavigate = onNavigate)
    }
}

data class LibraryCollection(val title: String, val subtitle: String, val icon: ImageVector, val bgColor: Color, val iconColor: Color)
data class LibraryActivity(val title: String, val subtitle: String, val icon: ImageVector, val actionIcon: ImageVector)

// 5. LEARNING PATH GENERATOR SCREEN
@Composable
fun LearningPathGeneratorScreen(
    viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigate: (Screen) -> Unit
) {
    val pathState by viewModel.generatedPath.collectAsState()

    // If path is null, use a robust fallback that matches the pre-existing mock values
    val path = pathState ?: LearningPath(
        topic = "Machine Learning",
        estimatedHours = "120 Hours",
        description = "A comprehensive journey from foundational mathematics to advanced predictive modeling.",
        modules = listOf(
            com.example.data.Module(
                moduleNumber = 1,
                moduleTitle = "Mathematics",
                lessons = listOf(
                    Lesson(
                        lessonNumber = 1,
                        lessonTitle = "Linear Algebra Basics",
                        level = "Beginner",
                        duration = "4h",
                        shortDescription = "Vectors, matrices, and transformations essential for understanding ML...",
                        content = "Imagine teaching a dog to fetch. You don't give it a manual on physics; you throw the ball and reward it when it brings it back. Machine Learning (ML) works similarly. Instead of writing rules, we feed the computer Data and let it find the patterns.",
                        aiTip = "Think of traditional programming as giving the computer a recipe. Machine learning is giving the computer a finished cake and asking it to figure out the recipe!",
                        keyConcept = "Algorithms are the math engines that find patterns in the data you provide.",
                        realWorldExample = "Streaming services use ML to recommend movies based on your watch history.",
                        quizQuestion = "Which of these is a supervised learning task?",
                        quizOptions = listOf("Grouping customers by behavior", "Predicting house prices", "Finding hidden patterns in text"),
                        quizCorrectIndex = 1
                    ),
                    Lesson(
                        lessonNumber = 2,
                        lessonTitle = "Calculus for Optimization",
                        level = "Intermediate",
                        duration = "6h",
                        shortDescription = "Derivatives, gradients, and their role in training models.",
                        content = "Optimization is the heart of machine learning. Calculus allows us to measure how a model's error changes as we adjust its parameters, guiding it step-by-step toward the perfect settings.",
                        aiTip = "Gradients are like walking down a foggy hill. You can't see the bottom, but you can feel which direction is down and take a step that way!",
                        keyConcept = "The gradient points in the direction of steepest increase; we move opposite to minimize error.",
                        realWorldExample = "Neural networks use gradient descent to adjust weights and learn complex patterns.",
                        quizQuestion = "What does gradient descent do?",
                        quizOptions = listOf("Maximizes the error rate", "Minimizes the loss function", "Deletes redundant data"),
                        quizCorrectIndex = 1
                    )
                )
            )
        )
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(viewModel = viewModel)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Generator icon",
                        tint = PrimaryGreen
                    )
                    Text(
                        text = "GENERATED PATH",
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PrimaryGreen,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = path.topic,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    color = OnBackgroundColor
                )

                Text(
                    text = "${path.description} Estimated completion: ${path.estimatedHours}.",
                    fontFamily = BeVietnamPro,
                    fontSize = 18.sp,
                    color = NeutralGrayDark
                )
            }

            // Start Journey CTA Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, NeutralGray, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateGray),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(SecondaryContainerBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Psychology icon",
                                tint = SecondaryBlue,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Start Your Journey",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = OnBackgroundColor
                            )
                            val modulesCount = path.modules.size
                            val totalLessonsCount = path.modules.sumOf { it.lessons.size }
                            Text(
                                text = "$modulesCount Modules • $totalLessonsCount Bite-sized lessons",
                                fontFamily = BeVietnamPro,
                                fontSize = 16.sp,
                                color = NeutralGrayDark
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.addGeneratedPathToTree() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .testTag("add_to_tree_button"),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = "ADD TO MY TREE",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = OnPrimary
                        )
                    }
                }
            }

            // Curriculum Modules
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                path.modules.forEach { module ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, NeutralGray, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(TertiaryGoldContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = module.moduleNumber.toString(),
                                            fontFamily = BeVietnamPro,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = OnTertiaryContainer
                                        )
                                    }
                                    Text(
                                        text = module.moduleTitle,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp,
                                        color = OnBackgroundColor
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ExpandMore,
                                    contentDescription = "Expand icon"
                                )
                            }

                            // Module Lessons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                module.lessons.forEach { lesson ->
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(2.dp, NeutralGray, RoundedCornerShape(8.dp)),
                                        colors = CardDefaults.cardColors(containerColor = BackgroundColor),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (lesson.level.lowercase() == "beginner") SecondaryContainerBlue else TertiaryGoldContainer)
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = lesson.level,
                                                        fontFamily = BeVietnamPro,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = if (lesson.level.lowercase() == "beginner") SecondaryBlue else OnTertiaryContainer
                                                    )
                                                }
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Schedule,
                                                        contentDescription = "Schedule icon",
                                                        tint = NeutralGrayDark,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = lesson.duration,
                                                        fontFamily = BeVietnamPro,
                                                        fontSize = 12.sp,
                                                        color = NeutralGrayDark
                                                    )
                                                }
                                            }

                                            Text(
                                                text = lesson.lessonTitle,
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = OnBackgroundColor,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Text(
                                                text = lesson.shortDescription,
                                                fontFamily = BeVietnamPro,
                                                fontSize = 14.sp,
                                                color = NeutralGrayDark,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Button(
                                                onClick = { viewModel.selectLesson(lesson) },
                                                colors = ButtonDefaults.buttonColors(containerColor = SlateGrayHigh, contentColor = OnBackgroundColor),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("start_lesson_${lesson.lessonTitle.lowercase().replace(" ", "_")}"),
                                                contentPadding = PaddingValues(vertical = 12.dp)
                                            ) {
                                                Text(
                                                    text = "Start Lesson",
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
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

        BottomNavBar(currentScreen = Screen.LEARNING_PATH_GENERATOR, onNavigate = onNavigate)
    }
}


// 6. QUIZ SCREEN
@Composable
fun QuizScreen(
    viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigate: (Screen) -> Unit
) {
    val selectedOptionIndex by viewModel.selectedOptionIndex.collectAsState()
    val quizAnswered by viewModel.quizAnswered.collectAsState()
    val quizScoreSuccess by viewModel.quizScoreSuccess.collectAsState()
    val selectedLesson by viewModel.selectedLesson.collectAsState()

    val questionText = selectedLesson?.quizQuestion ?: "Which of these is a supervised learning task?"
    val optionsList = selectedLesson?.quizOptions ?: listOf(
        "Grouping customers by behavior",
        "Predicting house prices",
        "Finding hidden patterns in text"
    )
    val correctIndex = selectedLesson?.quizCorrectIndex ?: 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
    ) {
        // Quiz header with progress and lives (Suppresses standard nav bars as per specs)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { onNavigate(Screen.LESSON) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = NeutralGrayDark),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .size(56.dp)
                    .testTag("quiz_close_button")
            ) {
                Text(
                    text = "close",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeutralGrayDark
                )
            }

            // progress bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(SlateGray)
            ) {
                val progressFraction = if (quizAnswered) 1.0f else 0.5f
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressFraction)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(PrimaryGreen)
                )
            }

            // lives / hearts
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Heart / Lives",
                    tint = ErrorRed,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "4",
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PrimaryGreen
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mascot prompt bubble
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(SecondaryContainerBlue)
                        .border(2.dp, OutlineGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "Robot icon",
                        tint = SecondaryBlue,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(2.dp, OutlineGray, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = questionText,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = OnBackgroundColor,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }

            // Options Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                optionsList.forEachIndexed { idx, optText ->
                    val isSelected = selectedOptionIndex == idx
                    val isCorrectIdx = idx == correctIndex
                    
                    val containerColor = when {
                        quizAnswered && isCorrectIdx -> PrimaryGreen
                        quizAnswered && isSelected && !isCorrectIdx -> ErrorRed
                        isSelected -> SecondaryContainerBlue
                        else -> PureWhite
                    }
                    
                    val contentColor = when {
                        quizAnswered && isCorrectIdx -> OnPrimary
                        quizAnswered && isSelected && !isCorrectIdx -> PureWhite
                        isSelected -> SecondaryBlue
                        else -> OnBackgroundColor
                    }

                    val borderColor = when {
                        quizAnswered && isCorrectIdx -> PrimaryGreenDark
                        quizAnswered && isSelected && !isCorrectIdx -> ErrorRed
                        isSelected -> SecondaryBlue
                        else -> SlateGrayDim
                    }

                    Button(
                        onClick = { viewModel.selectQuizOption(idx) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = containerColor,
                            contentColor = contentColor
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .testTag("quiz_option_$idx"),
                        contentPadding = PaddingValues(20.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected || (quizAnswered && isCorrectIdx)) PureWhite else SlateGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (idx + 1).toString(),
                                    fontFamily = BeVietnamPro,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (quizAnswered && isCorrectIdx) PrimaryGreen else if (isSelected) SecondaryBlue else NeutralGrayDark
                                )
                            }
                            Text(
                                text = optText,
                                fontFamily = BeVietnamPro,
                                fontWeight = if (isSelected || (quizAnswered && isCorrectIdx)) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                            if (quizAnswered && isCorrectIdx) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Correct icon",
                                    tint = OnPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else if (quizAnswered && isSelected && !isCorrectIdx) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = "Incorrect icon",
                                    tint = PureWhite,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action continue or check answer button bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val hasSelected = selectedOptionIndex != null
            val buttonText = if (quizAnswered) "CONTINUE" else "CHECK ANSWER"
            val buttonColor = if (quizAnswered || hasSelected) PrimaryGreen else SlateGrayDim

            Button(
                onClick = {
                    if (quizAnswered) {
                        viewModel.moveToNextLesson()
                    } else if (hasSelected) {
                        viewModel.submitQuizAnswer()
                    }
                },
                enabled = quizAnswered || hasSelected,
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    disabledContainerColor = SlateGrayLow,
                    contentColor = OnPrimary,
                    disabledContentColor = NeutralGrayDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("quiz_continue_button")
            ) {
                Text(
                    text = buttonText,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}


// 7. PROFILE SCREEN
@Composable
fun ProfileScreen(
    viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigate: (Screen) -> Unit
) {
    val streakCount by viewModel.streakCount.collectAsState()
    val xpAmount by viewModel.xpAmount.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(viewModel = viewModel)

        if (currentUser == null) {
            var isSignUpMode by remember { mutableStateOf(false) }
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(SecondaryContainerBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock Icon",
                        tint = SecondaryBlue,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (isSignUpMode) "Create Account" else "Welcome Back",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    color = OnBackgroundColor
                )

                Text(
                    text = if (isSignUpMode) "Sign up to securely save your custom learning paths" else "Sign in to resume your learning progress",
                    fontFamily = BeVietnamPro,
                    fontSize = 14.sp,
                    color = NeutralGrayDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Email input
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("auth_email_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = SlateGrayDim,
                        focusedLabelColor = PrimaryGreen
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password (min. 6 characters)") },
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("auth_password_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = SlateGrayDim,
                        focusedLabelColor = PrimaryGreen
                    )
                )

                if (authError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = authError ?: "",
                        color = ErrorRed,
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // CTA Submit button
                Button(
                    onClick = {
                        if (isSignUpMode) {
                            viewModel.signUp(email, password)
                        } else {
                            viewModel.signIn(email, password)
                        }
                    },
                    enabled = email.isNotBlank() && password.length >= 6 && !isAuthLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("auth_submit_button")
                ) {
                    if (isAuthLoading) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = PureWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = if (isSignUpMode) "SIGN UP" else "SIGN IN",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = OnPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle mode button
                TextButton(
                    onClick = { isSignUpMode = !isSignUpMode },
                    modifier = Modifier.testTag("auth_toggle_button")
                ) {
                    Text(
                        text = if (isSignUpMode) "Already have an account? Sign In" else "New to LearnTree? Create Account",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
            // Profile Header Area
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(128.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(128.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen)
                            .border(4.dp, PrimaryGreenDark, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Avatar",
                            tint = OnPrimary,
                            modifier = Modifier.size(80.dp)
                        )
                    }

                    // Level plaquette
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 12.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(TertiaryGold)
                            .border(2.dp, OnTertiaryContainer, RoundedCornerShape(9999.dp))
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = "Level icon",
                                tint = OnTertiaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            val levelNum = (xpAmount / 100) + 1
                            Text(
                                text = "LEVEL $levelNum",
                                fontFamily = BeVietnamPro,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = OnTertiaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentUser?.email ?: "AI Explorer",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (currentUser?.email != null) 22.sp else 32.sp,
                    color = OnBackgroundColor
                )
                Text(
                    text = "Personalized Learning Journey",
                    fontFamily = BeVietnamPro,
                    fontSize = 18.sp,
                    color = NeutralGrayDark
                )
            }

            // Stats Bento Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val stats = listOf(
                    ProfileStat("$streakCount Days", "Current Streak", Icons.Default.LocalFireDepartment, ErrorContainer, ErrorRed),
                    ProfileStat(xpAmount.toString(), "Total XP", Icons.Default.Bolt, SecondaryContainerBlue, SecondaryBlue),
                    ProfileStat("8", "Badges Earned", Icons.Default.EmojiEvents, TertiaryGoldContainer, TertiaryGold)
                )

                stats.forEach { stat ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(2.dp, NeutralGray, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(stat.bgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = stat.icon,
                                    contentDescription = stat.title,
                                    tint = stat.iconColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = stat.title,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = OnBackgroundColor
                            )
                            Text(
                                text = stat.subtitle,
                                fontFamily = BeVietnamPro,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = NeutralGrayDark,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Weekly Goal Progress (Radial representation)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, NeutralGray, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular Progress Canvas Representation
                    Box(
                        modifier = Modifier.size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = SlateGray,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 16f)
                            )
                            drawArc(
                                color = PrimaryGreen,
                                startAngle = -90f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = 16f)
                            )
                        }
                        Text(
                            text = "75%",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = PrimaryGreenDark
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Weekly Goal Progress",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = OnBackgroundColor
                        )
                        Text(
                            text = "You are almost there! Complete 3 more lessons to hit your target for the week.",
                            fontFamily = BeVietnamPro,
                            fontSize = 16.sp,
                            color = NeutralGrayDark
                        )
                    }
                }
            }

            // Recent Badges Gallery
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Badges",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = OnBackgroundColor
                    )
                    Text(
                        text = "VIEW ALL",
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PrimaryGreen
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val badges = listOf(
                        ProfileBadge("Early Bird", Icons.Default.MilitaryTech, TertiaryGoldContainer),
                        ProfileBadge("Scholar", Icons.Default.School, SecondaryContainerBlue),
                        ProfileBadge("Flawless", Icons.Default.CrueltyFree, PrimaryGreen),
                        ProfileBadge("Wildfire", Icons.Default.Whatshot, ErrorContainer)
                    )

                    badges.forEach { badge ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .border(2.dp, NeutralGray, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(badge.color),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = badge.icon,
                                        contentDescription = badge.name,
                                        tint = PureWhite,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Text(
                                    text = badge.name,
                                    fontFamily = BeVietnamPro,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = OnBackgroundColor,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.signOut() },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("logout_button")
                ) {
                    Text(
                        text = "LOG OUT",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PureWhite
                    )
                }
            }
        }
        }

        BottomNavBar(currentScreen = Screen.PROFILE, onNavigate = onNavigate)
    }
}

data class ProfileStat(val title: String, val subtitle: String, val icon: ImageVector, val bgColor: Color, val iconColor: Color)
data class ProfileBadge(val name: String, val icon: ImageVector, val color: Color)

// 8. LESSON SCREEN
@Composable
fun LessonScreen(
    viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigate: (Screen) -> Unit
) {
    val selectedLesson by viewModel.selectedLesson.collectAsState()
    val isFetchingDetailedLesson by viewModel.isFetchingDetailedLesson.collectAsState()

    val title = selectedLesson?.lessonTitle ?: "Introduction to Machine Learning"
    val description = selectedLesson?.shortDescription ?: "Discover how computers learn from data without being explicitly programmed."
    val content = selectedLesson?.content ?: "Imagine teaching a dog to fetch. You don't give it a manual on physics; you throw the ball and reward it when it brings it back. Machine Learning (ML) works similarly. Instead of writing rules, we feed the computer Data and let it find the patterns."
    val aiTip = selectedLesson?.aiTip ?: "Think of traditional programming as giving the computer a recipe. Machine learning is giving the computer a finished cake and asking it to figure out the recipe!"
    val keyConcept = selectedLesson?.keyConcept ?: "Algorithms are the math engines that find patterns in the data you provide."
    val realWorldExample = selectedLesson?.realWorldExample ?: "Streaming services use ML to recommend movies based on your watch history."
    val level = selectedLesson?.level ?: "Beginner"
    val duration = selectedLesson?.duration ?: "5m"

    if (isFetchingDetailedLesson) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(SecondaryContainerBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Loading Icon",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(64.dp)
                    )
                }
                Text(
                    text = "Personalizing Your Lesson...",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = OnBackgroundColor,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Our AI Tutor is generating fully interactive, step-by-step content and custom quizzes tailored specifically to your learning speed.",
                    fontFamily = BeVietnamPro,
                    fontSize = 16.sp,
                    color = NeutralGrayDark,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
                androidx.compose.material3.CircularProgressIndicator(
                    color = PrimaryGreen,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
        ) {
        // Transactional Header (Suppresses bottom nav)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { onNavigate(Screen.HOME) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = NeutralGrayDark),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .size(40.dp)
                    .semantics { contentDescription = "Close Lesson" }
                    .testTag("lesson_close_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Lesson",
                    tint = NeutralGrayDark
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = level,
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = PrimaryGreen
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Duration Icon",
                            tint = NeutralGrayDark,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = duration,
                            fontFamily = BeVietnamPro,
                            fontSize = 12.sp,
                            color = NeutralGrayDark
                        )
                    }
                }
                // Chunky Progress bar
                LinearProgressIndicator(
                    progress = 0.5f,
                    color = PrimaryGreen,
                    trackColor = SlateGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(9999.dp))
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Lives / Health Icon",
                tint = ErrorRed,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Lesson Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = OnBackgroundColor,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = description,
                    fontFamily = BeVietnamPro,
                    fontSize = 16.sp,
                    color = NeutralGrayDark,
                    textAlign = TextAlign.Center
                )
            }

            // Illustration Placeholder Card
            Card(
                modifier = Modifier
                    .size(240.dp)
                    .border(2.dp, NeutralGray, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Lesson illustration representation",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(80.dp)
                        )
                        Text(
                            text = if (title.length > 18) title.take(15) + "..." else title,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = NeutralGrayDark
                        )
                    }
                }
            }

            // Content text
            Text(
                text = content,
                fontFamily = BeVietnamPro,
                fontSize = 18.sp,
                color = OnBackgroundColor,
                lineHeight = 28.sp
            )

            // AI Tip Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, SecondaryBlue, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SecondaryContainerBlue),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "Robot Icon",
                        tint = SecondaryBlue,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = "AI Tip:",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SecondaryBlue
                        )
                        Text(
                            text = aiTip,
                            fontFamily = BeVietnamPro,
                            fontSize = 16.sp,
                            color = SecondaryBlue
                        )
                    }
                }
            }

            // Key Concept & Real-world Example
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(2.dp, NeutralGray, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Concept Icon",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Key Concept",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = OnBackgroundColor
                        )
                        Text(
                            text = keyConcept,
                            fontFamily = BeVietnamPro,
                            fontSize = 14.sp,
                            color = NeutralGrayDark
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(2.dp, NeutralGray, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = "Example Icon",
                            tint = SecondaryBlue,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Real-world Example",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = OnBackgroundColor
                        )
                        Text(
                            text = realWorldExample,
                            fontFamily = BeVietnamPro,
                            fontSize = 14.sp,
                            color = NeutralGrayDark
                        )
                    }
                }
            }
        }

        // Action continue button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Button(
                onClick = { onNavigate(Screen.QUIZ) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("lesson_continue_button")
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CONTINUE",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = OnPrimary
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Arrow Forward"
                    )
                }
            }
        }
    }
}
}
