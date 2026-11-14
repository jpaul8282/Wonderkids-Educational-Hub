package com.example.ui.kids

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Badge
import com.example.data.model.ChildProfile
import com.example.data.model.GameCategory
import com.example.data.model.GameSession
import com.example.data.model.SubjectMastery
import com.example.ui.components.AvatarHelper
import com.example.ui.components.AvatarSelectionDialog
import com.example.ui.theme.ParentNavy
import com.example.ui.theme.PlayfulGreen
import com.example.ui.theme.PrimaryKids
import com.example.ui.theme.StreakFire
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LearningModuleInfo(
    val category: GameCategory,
    val title: String,
    val emoji: String,
    val route: String,
    val badgeCode: String,
    val subjectDesc: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UserProfileScreen(
    child: ChildProfile?,
    allChildren: List<ChildProfile>,
    badges: List<Badge>,
    sessions: List<GameSession>,
    subjectMasteries: List<SubjectMastery>,
    onSelectChild: (Long) -> Unit,
    onNavigateToGame: (String) -> Unit,
    onBack: () -> Unit,
    onUpdateAvatar: (childId: Long, avatarId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var selectedBadgeDetail by remember { mutableStateOf<Badge?>(null) }

    val unlockedBadges = remember(badges) { badges.filter { it.isUnlocked } }
    val unlockedCount = unlockedBadges.size
    val totalStars = child?.totalStars ?: 0

    // Learning Rank Calculation
    val (rankTitle, rankLevel, nextLevelStars, currentLevelFloor) = remember(totalStars) {
        when {
            totalStars >= 250 -> Quad("Grand Sage 🧙‍♂️", 5, 250, 250)
            totalStars >= 150 -> Quad("Master Adventurer ⚔️", 4, 250, 150)
            totalStars >= 70 -> Quad("Galaxy Scholar 🚀", 3, 150, 70)
            totalStars >= 30 -> Quad("Rising Star 🌟", 2, 70, 30)
            else -> Quad("Novice Explorer 🎒", 1, 30, 0)
        }
    }

    val levelProgress = remember(totalStars, rankLevel, nextLevelStars, currentLevelFloor) {
        if (rankLevel >= 5) 1f
        else {
            val range = nextLevelStars - currentLevelFloor
            val progress = (totalStars - currentLevelFloor).toFloat() / range.coerceAtLeast(1)
            progress.coerceIn(0f, 1f)
        }
    }

    val coreModules = listOf(
        LearningModuleInfo(GameCategory.MATH, "Math Galaxy", "🚀", "game_math", "MODULE_MATH_GRAD", "Arithmetic & Numbers"),
        LearningModuleInfo(GameCategory.SPELLING, "Word Explorer", "📚", "game_word", "MODULE_WORD_GRAD", "Vocabulary & Phonics"),
        LearningModuleInfo(GameCategory.MEMORY, "Memory Safari", "🧠", "game_memory", "MODULE_MEMORY_GRAD", "Focus & Animal Pairs"),
        LearningModuleInfo(GameCategory.SCIENCE, "Science Trivia", "🔬", "game_science", "MODULE_SCIENCE_GRAD", "Nature & Space Facts"),
        LearningModuleInfo(GameCategory.SHAPES, "Shape Kingdom", "🎨", "game_shapes", "MODULE_SHAPES_GRAD", "Colors & Geometry")
    )

    val filterOptions = listOf(
        "ALL" to "All Badges (${badges.size})",
        "MODULES" to "Modules 🎓 (${badges.count { it.category == "MODULES" }})",
        "SUBJECTS" to "Subject Mastery ⚡ (${badges.count { it.category in listOf("MATH", "SPELLING", "MEMORY", "SCIENCE", "SHAPES") }})",
        "MISSION" to "Daily Quests 🎯 (${badges.count { it.category == "MISSION" }})",
        "MILESTONES" to "Milestones ⭐ (${badges.count { it.category in listOf("STREAK", "GENERAL") }})"
    )

    val filteredBadges = remember(badges, selectedCategoryFilter) {
        when (selectedCategoryFilter) {
            "MODULES" -> badges.filter { it.category == "MODULES" }
            "SUBJECTS" -> badges.filter { it.category in listOf("MATH", "SPELLING", "MEMORY", "SCIENCE", "SHAPES") }
            "MISSION" -> badges.filter { it.category == "MISSION" }
            "MILESTONES" -> badges.filter { it.category in listOf("STREAK", "GENERAL") }
            else -> badges
        }
    }

    val avatar = child?.let { AvatarHelper.getAvatar(it.avatarId) } ?: AvatarHelper.getAvatar("astronaut")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Student Profile & Badges",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("profile_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAvatarDialog = true },
                        modifier = Modifier.testTag("profile_change_avatar_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Customize Avatar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF386B3B)
                )
            )
        },
        containerColor = Color(0xFFFBFCF8),
        modifier = modifier.testTag("user_profile_screen")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // 1. Profile Header Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFDCE5DB), RoundedCornerShape(26.dp))
                        .testTag("student_profile_hero_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Avatar with click to customize
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(avatar.bgColor)
                                    .border(3.dp, Color(0xFF386B3B), CircleShape)
                                    .clickable { showAvatarDialog = true }
                                    .testTag("profile_avatar_box"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = avatar.emoji, fontSize = 40.sp)
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF386B3B))
                                        .align(Alignment.BottomEnd)
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = "Change",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = child?.name ?: "Learner",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ParentNavy
                                    )
                                )
                                Text(
                                    text = "Age ${child?.age ?: 7} • ${child?.gradeLevel ?: "1st Grade"}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFF555F55),
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFEDF3EB))
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = rankTitle,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF386B3B)
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Level Progress Bar
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Rank Progress (Level $rankLevel)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF414941)
                                    )
                                )
                                Text(
                                    text = if (rankLevel >= 5) "Max Level Reached! 👑"
                                    else "$totalStars / $nextLevelStars Stars",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF7A581E)
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { levelProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF386B3B),
                                trackColor = Color(0xFFE2E8DE)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Grid Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProfileStatPill(
                                icon = "⭐",
                                value = "$totalStars",
                                label = "Stars",
                                containerColor = Color(0xFFFBF1DD),
                                textColor = Color(0xFF7A581E),
                                modifier = Modifier.weight(1f)
                            )
                            ProfileStatPill(
                                icon = "🏆",
                                value = "$unlockedCount/${badges.size}",
                                label = "Badges",
                                containerColor = Color(0xFFEDF3EB),
                                textColor = Color(0xFF386B3B),
                                modifier = Modifier.weight(1f)
                            )
                            ProfileStatPill(
                                icon = "📚",
                                value = "${sessions.size}",
                                label = "Modules",
                                containerColor = Color(0xFFEAF1F8),
                                textColor = Color(0xFF1E527A),
                                modifier = Modifier.weight(1f)
                            )
                            ProfileStatPill(
                                icon = "🔥",
                                value = "${child?.currentStreakDays ?: 1}d",
                                label = "Streak",
                                containerColor = Color(0xFFFBECE8),
                                textColor = StreakFire,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 2. Learning Modules Section
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🎓 Learning Modules & Rewards",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ParentNavy
                                )
                            )
                            Text(
                                text = "Complete modules to earn graduate badges & stars",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF555F55))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        coreModules.forEach { module ->
                            val moduleBadge = badges.find { it.badgeCode == module.badgeCode }
                            val isBadgeUnlocked = moduleBadge?.isUnlocked == true
                            val moduleSessions = sessions.filter { it.gameCategory.equals(module.category.name, ignoreCase = true) }
                            val completedCount = moduleSessions.size
                            val mastery = subjectMasteries.find { it.category == module.category }
                            val accuracy = mastery?.accuracyPercentage ?: if (completedCount > 0) 90 else 0

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isBadgeUnlocked) Color(0xFFF7FAF6) else Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = if (isBadgeUnlocked) Color(0xFF386B3B).copy(alpha = 0.3f) else Color(0xFFDCE5DB),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .testTag("profile_module_card_${module.category.name}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(if (isBadgeUnlocked) Color(0xFFDCF0DF) else Color(0xFFF1F5F9)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = module.emoji, fontSize = 24.sp)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = module.title,
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = ParentNavy
                                                )
                                            )
                                            if (isBadgeUnlocked) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Badge Earned",
                                                    tint = PlayfulGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${module.subjectDesc} • $completedCount completed",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF555F55),
                                                fontSize = 11.sp
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        // Badge Status line
                                        if (isBadgeUnlocked) {
                                            Text(
                                                text = "🏅 Graduate Badge Earned (+15⭐)",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF386B3B),
                                                    fontSize = 10.sp
                                                )
                                            )
                                        } else {
                                            Text(
                                                text = "🔒 Play module to earn Graduate Badge (+15⭐)",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color(0xFF7A581E),
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { onNavigateToGame(module.route) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isBadgeUnlocked) Color(0xFFEDF3EB) else Color(0xFF386B3B),
                                            contentColor = if (isBadgeUnlocked) Color(0xFF386B3B) else Color.White
                                        ),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .height(34.dp)
                                            .testTag("launch_module_${module.category.name}")
                                    ) {
                                        Text(
                                            text = if (completedCount > 0) "Play Again" else "Start 🚀",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Badges & Achievements Section
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏆 Achievement Badges ($unlockedCount / ${badges.size})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ParentNavy
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filterOptions) { (key, label) ->
                            FilterChip(
                                selected = selectedCategoryFilter == key,
                                onClick = { selectedCategoryFilter = key },
                                label = { Text(label, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF386B3B),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = ParentNavy
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedCategoryFilter == key,
                                    borderColor = Color(0xFFDCE5DB),
                                    selectedBorderColor = Color(0xFF386B3B)
                                )
                            )
                        }
                    }
                }
            }

            // Badges List
            items(filteredBadges) { badge ->
                val isUnlocked = badge.isUnlocked
                Card(
                    onClick = { selectedBadgeDetail = badge },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) Color.White else Color(0xFFF7F8F6)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 2.dp else 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (isUnlocked) Color(0xFF386B3B).copy(alpha = 0.25f) else Color(0xFFE2E8DE),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .testTag("badge_card_${badge.badgeCode}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge Icon Box
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isUnlocked) Color(0xFFEDF3EB)
                                    else Color(0xFFE2E8DE)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isUnlocked) {
                                Text(text = badge.iconEmoji, fontSize = 28.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color(0xFF717970),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = badge.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUnlocked) ParentNavy else Color(0xFF717970)
                                    )
                                )
                                if (isUnlocked) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFDCF0DF))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "UNLOCKED ✨",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF386B3B),
                                                fontSize = 9.sp
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = badge.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isUnlocked) Color(0xFF414941) else Color(0xFF717970),
                                    fontSize = 12.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Star Reward Tag
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFBF1DD))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "+${badge.rewardStars} ⭐",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF7A581E),
                                            fontSize = 10.sp
                                        )
                                    )
                                }

                                if (badge.moduleName.isNotBlank()) {
                                    Text(
                                        text = "• ${badge.moduleName}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF555F55),
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Avatar Selection Dialog
    if (showAvatarDialog && child != null) {
        AvatarSelectionDialog(
            currentAvatarId = child.avatarId,
            childName = child.name,
            onAvatarSelected = { newAvatarId ->
                onUpdateAvatar(child.id, newAvatarId)
                showAvatarDialog = false
            },
            onDismiss = { showAvatarDialog = false }
        )
    }

    // Badge Details Modal Dialog
    selectedBadgeDetail?.let { badge ->
        BadgeDetailDialog(
            badge = badge,
            onDismiss = { selectedBadgeDetail = null },
            onLaunchGame = { route ->
                selectedBadgeDetail = null
                onNavigateToGame(route)
            }
        )
    }
}

@Composable
fun ProfileStatPill(
    icon: String,
    value: String,
    label: String,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp)
        ) {
            Text(text = icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = textColor.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
fun BadgeDetailDialog(
    badge: Badge,
    onDismiss: () -> Unit,
    onLaunchGame: (String) -> Unit
) {
    val isUnlocked = badge.isUnlocked
    val formattedDate = remember(badge.unlockedAtTimestamp) {
        badge.unlockedAtTimestamp?.let {
            SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(it))
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (isUnlocked) Color(0xFFEDF3EB) else Color(0xFFE2E8DE))
                        .border(
                            2.dp,
                            if (isUnlocked) Color(0xFF386B3B) else Color.Gray,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUnlocked) {
                        Text(text = badge.iconEmoji, fontSize = 42.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color.Gray,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = badge.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ParentNavy
                    )
                )

                if (badge.moduleName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Module: ${badge.moduleName}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF386B3B)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF414941),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (isUnlocked) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFDCF0DF))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🎉 Unlocked! +${badge.rewardStars} Stars Rewarded" +
                                    (formattedDate?.let { " ($it)" } ?: ""),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF386B3B)
                            )
                        )
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF1DD)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "How to unlock:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7A581E)
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = badge.requirementDescription.ifBlank { badge.description },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF7A581E),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Reward upon completion: +${badge.rewardStars} ⭐",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF386B3B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Got It!", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
